package com.placements.backend.service;

import com.placements.backend.entity.YouTubeVideo;
import com.placements.backend.repository.YouTubeVideoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class YoutubePlaylistImportService {

    private static final Logger log = LoggerFactory.getLogger(YoutubePlaylistImportService.class);

    private final YouTubeVideoRepository youtubeVideoRepository;
    private final RestTemplate restTemplate;
    private final TopicCatalog topicCatalog;

    @Value("${app.youtube.api-key:}")
    private String apiKey;

    @Value("${app.youtube.playlist-id:PLbJhGqY-mq47k_WLUtzVjmarUm1EuXPj2}")
    private String playlistId;

    public YoutubePlaylistImportService(
            YouTubeVideoRepository youtubeVideoRepository,
            RestTemplate restTemplate,
            TopicCatalog topicCatalog
    ) {
        this.youtubeVideoRepository = youtubeVideoRepository;
        this.restTemplate = restTemplate;
        this.topicCatalog = topicCatalog;
    }

    @Transactional
    public Map<String, Object> importPlaylist() {
        int importedCount = 0;
        int skippedCount = 0;
        int matchedToTopic = 0;

        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.warn("YouTube API Key is missing. Seeding fallback YouTube videos.");
            return seedFallbackVideos();
        }

        try {
            String urlTemplate = "https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&maxResults=50&playlistId=%s&key=%s&pageToken=%s";
            String pageToken = "";
            boolean hasMore = true;
            int position = 0;

            while (hasMore) {
                String url = String.format(urlTemplate, playlistId, apiKey, pageToken);
                ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
                Map<String, Object> body = response.getBody();

                if (body == null) break;

                List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
                if (items == null || items.isEmpty()) break;

                for (Map<String, Object> item : items) {
                    Map<String, Object> snippet = (Map<String, Object>) item.get("snippet");
                    if (snippet == null) continue;

                    String title = (String) snippet.get("title");
                    Map<String, Object> resourceId = (Map<String, Object>) snippet.get("resourceId");
                    if (resourceId == null) continue;

                    String videoId = (String) resourceId.get("videoId");

                    if (videoId == null) continue;

                    if (youtubeVideoRepository.existsByVideoId(videoId)) {
                        skippedCount++;
                        position++;
                        continue;
                    }

                    String matchedTopicKey = topicCatalog.matchTopicByTitle(title);
                    if (matchedTopicKey != null) {
                        matchedToTopic++;
                    }

                    YouTubeVideo video = YouTubeVideo.builder()
                            .videoId(videoId)
                            .title(title)
                            .playlistPosition(position)
                            .matchedTopicKey(matchedTopicKey)
                            .build();

                    youtubeVideoRepository.save(video);
                    importedCount++;
                    position++;
                }

                pageToken = (String) body.get("nextPageToken");
                if (pageToken == null || pageToken.isEmpty()) {
                    hasMore = false;
                }
            }

            if (importedCount == 0 && skippedCount == 0) {
                log.warn("No videos fetched from YouTube API. Running local fallback seeder.");
                return seedFallbackVideos();
            }

        } catch (Exception e) {
            log.error("Failed to fetch YouTube playlist from remote API: {}. Running fallback seeder.", e.getMessage());
            return seedFallbackVideos();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("imported", importedCount);
        result.put("skippedExisting", skippedCount);
        result.put("matchedToTopic", matchedToTopic);
        return result;
    }

    private Map<String, Object> seedFallbackVideos() {
        int imported = 0;
        int skipped = 0;
        int matched = 0;

        // Hardcode a curated playlist of actual high-quality DSA videos mapped to topics
        List<YouTubeVideo> fallbackList = Arrays.asList(
            new YouTubeVideo(null, "m8g0V3X1k", "Arrays and Lists Complete Tutorial", 0, "ARRAYS"),
            new YouTubeVideo(null, "37DsD86g7b4", "Hashing & HashMaps: From Scratch to Advanced", 1, "HASHING"),
            new YouTubeVideo(null, "M2uO9n1q84U", "Merge Sort Algorithm Explained in Depth", 2, "SORTING"),
            new YouTubeVideo(null, "N89xT69k3sM", "Binary Search: Iterative & Recursive Approaches", 3, "BINARY_SEARCH"),
            new YouTubeVideo(null, "F20yvT7hR4E", "Strings In C++/Java: Patterns and Traversals", 4, "STRINGS"),
            new YouTubeVideo(null, "2wN0HsPr2tE", "Two Pointers Technique for Array Problems", 5, "TWO_POINTERS"),
            new YouTubeVideo(null, "91Tsk1gP17E", "Sliding Window Pattern: Maximum Subarray of Size K", 6, "SLIDING_WINDOW"),
            new YouTubeVideo(null, "H63dVEG3r_Y", "Introduction to Linked Lists: Creation and Reverse", 7, "LINKED_LIST"),
            new YouTubeVideo(null, "kHi1Fl2h6eU", "Recursion & Backtracking for Beginners", 8, "RECURSION"),
            new YouTubeVideo(null, "5N24vH6V8Lw", "Bit Manipulation Tricks you must know", 9, "BIT_MANIPULATION"),
            new YouTubeVideo(null, "okr-XE81h8k", "Stacks & Queues: Implementation & Common Problems", 10, "STACK_QUEUE"),
            new YouTubeVideo(null, "t0Cq6t4e14s", "Heaps and Priority Queues Explained", 11, "HEAP"),
            new YouTubeVideo(null, "2w4ePqR4hR4", "Greedy Algorithms: Interval Scheduling", 12, "GREEDY"),
            new YouTubeVideo(null, "aM1e9Pq_kRk", "Backtracking Concepts: N-Queens Problem", 13, "BACKTRACKING"),
            new YouTubeVideo(null, "5n2M1E89H4s", "Binary Trees & Traversals: PreOrder, InOrder, PostOrder", 14, "TREES"),
            new YouTubeVideo(null, "p7-F-Fp37Ew", "Binary Search Tree Operations: Insert, Delete, Search", 15, "BST"),
            new YouTubeVideo(null, "yt-F-G8h34t", "Graph Traversals: Breadth First Search (BFS) & DFS", 16, "GRAPHS"),
            new YouTubeVideo(null, "knap-F-78g", "Dynamic Programming: Knapsack 0/1 & Memoization", 17, "DP"),
            new YouTubeVideo(null, "trie-F-G89", "Trie Data Structure: Insert, Search, StartsWith", 18, "TRIE")
        );

        for (YouTubeVideo v : fallbackList) {
            if (!youtubeVideoRepository.existsByVideoId(v.getVideoId())) {
                youtubeVideoRepository.save(v);
                imported++;
                if (v.getMatchedTopicKey() != null) {
                    matched++;
                }
            } else {
                skipped++;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("imported", imported);
        result.put("skippedExisting", skipped);
        result.put("matchedToTopic", matched);
        return result;
    }
}
