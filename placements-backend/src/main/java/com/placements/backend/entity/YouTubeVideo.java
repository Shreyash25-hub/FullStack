package com.placements.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "youtube_videos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YouTubeVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "video_id", nullable = false, unique = true, length = 50)
    private String videoId;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(name = "playlist_position", nullable = false)
    private Integer playlistPosition;

    @Column(name = "matched_topic_key", length = 100)
    private String matchedTopicKey;
}
