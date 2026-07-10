package com.placements.backend.service;

import com.placements.backend.dto.ScheduleDtos.*;
import com.placements.backend.entity.*;
import com.placements.backend.exception.BadRequestException;
import com.placements.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class ScheduleGeneratorService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleItemRepository scheduleItemRepository;
    private final LeetCodeProblemRepository leetCodeProblemRepository;
    private final YouTubeVideoRepository youtubeVideoRepository;
    private final TopicCatalog topicCatalog;
    private final NonDsaTopicContent nonDsaTopicContent;

    public ScheduleGeneratorService(
            ScheduleRepository scheduleRepository,
            ScheduleItemRepository scheduleItemRepository,
            LeetCodeProblemRepository leetCodeProblemRepository,
            YouTubeVideoRepository youtubeVideoRepository,
            TopicCatalog topicCatalog,
            NonDsaTopicContent nonDsaTopicContent
    ) {
        this.scheduleRepository = scheduleRepository;
        this.scheduleItemRepository = scheduleItemRepository;
        this.leetCodeProblemRepository = leetCodeProblemRepository;
        this.youtubeVideoRepository = youtubeVideoRepository;
        this.topicCatalog = topicCatalog;
        this.nonDsaTopicContent = nonDsaTopicContent;
    }

    @Transactional
    public ScheduleResponse generateSchedule(User user, ScheduleCreateRequest request) {
        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();

        if (startDate == null || endDate == null) {
            throw new BadRequestException("Start date and end date are required");
        }

        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("endDate must be on or after startDate");
        }

        long numDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (numDays < 1 || numDays > 730) { // 2 years = 730 days
            throw new BadRequestException("Schedule duration must be between 1 day and 2 years");
        }

        List<String> dsaTopics = new ArrayList<>();
        List<String> subjectTopics = new ArrayList<>();

        for (String topic : request.getTopics()) {
            if (topicCatalog.isDsaTopic(topic)) {
                dsaTopics.add(topic);
            } else if (nonDsaTopicContent.hasSubject(topic)) {
                subjectTopics.add(topic);
            }
        }

        if (dsaTopics.isEmpty() && subjectTopics.isEmpty()) {
            throw new BadRequestException("No valid topics selected");
        }

        Schedule schedule = Schedule.builder()
                .user(user)
                .startDate(startDate)
                .endDate(endDate)
                .selectedTopics(String.join(",", request.getTopics()))
                .build();

        schedule = scheduleRepository.save(schedule);

        List<ScheduleItem> items = new ArrayList<>();
        Set<String> seenDsaTopics = new HashSet<>();
        Set<String> localAssignedSlugs = new HashSet<>();

        for (int dayIndex = 0; dayIndex < numDays; dayIndex++) {
            LocalDate currentDate = startDate.plusDays(dayIndex);
            int dayNumber = dayIndex + 1;

            // 1. Process DSA Topic for this day
            if (!dsaTopics.isEmpty()) {
                String dsaTopic = dsaTopics.get(dayIndex % dsaTopics.size());
                String tagSlug = topicCatalog.getLeetCodeTag(dsaTopic);

                // Fetch problems sorted by difficulty (Easy -> Medium -> Hard)
                List<LeetCodeProblem> problems = leetCodeProblemRepository.findByTagOrderedByDifficulty(tagSlug);
                List<String> userAssignedSlugs = scheduleItemRepository.findSlugsByUserIdAndTopicKey(user.getId(), dsaTopic);

                LeetCodeProblem selectedProblem = null;
                for (LeetCodeProblem p : problems) {
                    if (!userAssignedSlugs.contains(p.getSlug()) && !localAssignedSlugs.contains(p.getSlug())) {
                        selectedProblem = p;
                        break;
                    }
                }

                // If all are already assigned, fall back to cycle
                if (selectedProblem == null && !problems.isEmpty()) {
                    selectedProblem = problems.get(dayIndex % problems.size());
                }

                if (selectedProblem != null) {
                    localAssignedSlugs.add(selectedProblem.getSlug());

                    // If it is the first appearance of this topic key, attach matching video
                    if (!seenDsaTopics.contains(dsaTopic)) {
                        seenDsaTopics.add(dsaTopic);

                        YouTubeVideo matchedVideo = findMatchingVideo(dsaTopic);
                        if (matchedVideo != null) {
                            ScheduleItem videoItem = ScheduleItem.builder()
                                    .schedule(schedule)
                                    .itemDate(currentDate)
                                    .dayNumber(dayNumber)
                                    .type(ItemType.VIDEO)
                                    .title("Watch: " + matchedVideo.getTitle())
                                    .topicKey(dsaTopic)
                                    .youtubeVideoId(matchedVideo.getVideoId())
                                    .resourceUrl("https://www.youtube.com/watch?v=" + matchedVideo.getVideoId())
                                    .completed(false)
                                    .build();
                            items.add(videoItem);
                        }
                    }

                    // Create DSA item
                    ScheduleItem dsaItem = ScheduleItem.builder()
                            .schedule(schedule)
                            .itemDate(currentDate)
                            .dayNumber(dayNumber)
                            .type(ItemType.DSA_QUESTION)
                            .title(selectedProblem.getTitle())
                            .topicKey(dsaTopic)
                            .leetcodeSlug(selectedProblem.getSlug())
                            .resourceUrl("https://leetcode.com/problems/" + selectedProblem.getSlug() + "/")
                            .completed(false)
                            .build();
                    items.add(dsaItem);
                }
            }

            // 2. Process CS Subject for this day
            if (!subjectTopics.isEmpty()) {
                String subjectTopic = subjectTopics.get(dayIndex % subjectTopics.size());
                String taskTitle = nonDsaTopicContent.getTaskForSubject(subjectTopic, dayIndex);

                ScheduleItem subjectItem = ScheduleItem.builder()
                        .schedule(schedule)
                        .itemDate(currentDate)
                        .dayNumber(dayNumber)
                        .type(ItemType.TASK)
                        .title(taskTitle)
                        .topicKey(subjectTopic)
                        .completed(false)
                        .build();
                items.add(subjectItem);
            }
        }

        if (!items.isEmpty()) {
            scheduleItemRepository.saveAll(items);
            schedule.setItems(items);
        }

        return mapToResponse(schedule);
    }

    private YouTubeVideo findMatchingVideo(String dsaTopic) {
        // Query by matched topic key
        List<YouTubeVideo> videos = youtubeVideoRepository.findByMatchedTopicKeyOrderByPlaylistPositionAsc(dsaTopic);
        if (!videos.isEmpty()) {
            return videos.get(0);
        }

        // Try exact keyword title checks
        List<YouTubeVideo> allVideos = youtubeVideoRepository.findAllByOrderByPlaylistPositionAsc();
        String tagSlug = topicCatalog.getLeetCodeTag(dsaTopic);
        for (YouTubeVideo v : allVideos) {
            String titleLower = v.getTitle().toLowerCase();
            if (titleLower.contains(dsaTopic.toLowerCase()) || 
                (tagSlug != null && titleLower.contains(tagSlug.toLowerCase()))) {
                return v;
            }
        }

        // Fallback to first video
        return allVideos.isEmpty() ? null : allVideos.get(0);
    }

    public ScheduleResponse mapToResponse(Schedule schedule) {
        List<ScheduleItemDto> itemDtos = schedule.getItems().stream()
                .map(item -> ScheduleItemDto.builder()
                        .id(item.getId())
                        .itemDate(item.getItemDate())
                        .dayNumber(item.getDayNumber())
                        .type(item.getType())
                        .title(item.getTitle())
                        .topicKey(item.getTopicKey())
                        .leetcodeSlug(item.getLeetcodeSlug())
                        .resourceUrl(item.getResourceUrl())
                        .youtubeVideoId(item.getYoutubeVideoId())
                        .completed(item.isCompleted())
                        .completedVia(item.getCompletedVia())
                        .completedAt(item.getCompletedAt())
                        .build())
                .toList();

        List<String> topicsList = Arrays.stream(schedule.getSelectedTopics().split(","))
                .filter(s -> !s.isEmpty())
                .toList();

        return ScheduleResponse.builder()
                .id(schedule.getId())
                .startDate(schedule.getStartDate())
                .endDate(schedule.getEndDate())
                .topics(topicsList)
                .createdAt(schedule.getCreatedAt())
                .items(itemDtos)
                .build();
    }
}
