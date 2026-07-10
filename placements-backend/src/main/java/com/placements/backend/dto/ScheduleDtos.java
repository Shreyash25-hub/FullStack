package com.placements.backend.dto;

import com.placements.backend.entity.CompletionSource;
import com.placements.backend.entity.ItemType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ScheduleDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleCreateRequest {
        @NotNull(message = "Start date is required")
        private LocalDate startDate;

        @NotNull(message = "End date is required")
        private LocalDate endDate;

        @NotEmpty(message = "At least one topic must be selected")
        private List<String> topics;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScheduleItemDto {
        private Long id;
        private LocalDate itemDate;
        private Integer dayNumber;
        private ItemType type;
        private String title;
        private String topicKey;
        private String resourceUrl;
        private String leetcodeSlug;
        private String youtubeVideoId;
        private boolean completed;
        private CompletionSource completedVia;
        private LocalDateTime completedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScheduleResponse {
        private Long id;
        private LocalDate startDate;
        private LocalDate endDate;
        private List<String> topics;
        private LocalDateTime createdAt;
        private List<ScheduleItemDto> items;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemProgressUpdateRequest {
        private boolean completed;
        private String completedVia;
    }
}
