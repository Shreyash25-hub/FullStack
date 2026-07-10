package com.placements.backend.repository;

import com.placements.backend.entity.YouTubeVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface YouTubeVideoRepository extends JpaRepository<YouTubeVideo, Long> {
    Optional<YouTubeVideo> findByVideoId(String videoId);
    boolean existsByVideoId(String videoId);
    List<YouTubeVideo> findByMatchedTopicKeyOrderByPlaylistPositionAsc(String matchedTopicKey);
    List<YouTubeVideo> findAllByOrderByPlaylistPositionAsc();
}
