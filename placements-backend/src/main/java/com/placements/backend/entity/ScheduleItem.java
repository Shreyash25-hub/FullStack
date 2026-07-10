package com.placements.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "schedule_items",
    indexes = {
        @Index(name = "idx_schedule_item_date", columnList = "schedule_id, item_date"),
        @Index(name = "idx_leetcode_slug", columnList = "leetcode_slug")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "schedule")
public class ScheduleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_id", nullable = false)
    @JsonIgnore
    private Schedule schedule;

    @Column(name = "item_date", nullable = false)
    private LocalDate itemDate;

    @Column(name = "day_number", nullable = false)
    private Integer dayNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemType type;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(name = "topic_key", nullable = false, length = 100)
    private String topicKey;

    @Column(name = "leetcode_slug", length = 200)
    private String leetcodeSlug;

    @Column(name = "resource_url", length = 500)
    private String resourceUrl;

    @Column(name = "youtube_video_id", length = 50)
    private String youtubeVideoId;

    @Column(nullable = false)
    @Builder.Default
    private boolean completed = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "completed_via")
    private CompletionSource completedVia;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
