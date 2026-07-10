package com.placements.backend.controller;

import com.placements.backend.dto.ScheduleDtos.ItemProgressUpdateRequest;
import com.placements.backend.dto.ScheduleDtos.ScheduleItemDto;
import com.placements.backend.entity.CompletionSource;
import com.placements.backend.entity.ScheduleItem;
import com.placements.backend.entity.User;
import com.placements.backend.exception.ResourceNotFoundException;
import com.placements.backend.repository.ScheduleItemRepository;
import com.placements.backend.security.AppUserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/items")
public class ScheduleItemController {

    private final ScheduleItemRepository scheduleItemRepository;

    public ScheduleItemController(ScheduleItemRepository scheduleItemRepository) {
        this.scheduleItemRepository = scheduleItemRepository;
    }

    @PatchMapping("/{id}/progress")
    public ResponseEntity<ScheduleItemDto> updateItemProgress(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable Long id,
            @RequestBody ItemProgressUpdateRequest request
    ) {
        User user = principal.getUser();
        ScheduleItem item = scheduleItemRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Schedule item not found with id: " + id));

        item.setCompleted(request.isCompleted());
        if (request.isCompleted()) {
            CompletionSource source = CompletionSource.MANUAL;
            if (request.getCompletedVia() != null) {
                try {
                    source = CompletionSource.valueOf(request.getCompletedVia());
                } catch (IllegalArgumentException e) {
                    // default to MANUAL
                }
            }
            item.setCompletedVia(source);
            item.setCompletedAt(LocalDateTime.now());
        } else {
            item.setCompletedVia(null);
            item.setCompletedAt(null);
        }

        item = scheduleItemRepository.save(item);

        ScheduleItemDto dto = ScheduleItemDto.builder()
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
                .build();

        return ResponseEntity.ok(dto);
    }
}
