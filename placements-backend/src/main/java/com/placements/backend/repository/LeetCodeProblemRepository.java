package com.placements.backend.repository;

import com.placements.backend.entity.LeetCodeProblem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LeetCodeProblemRepository extends JpaRepository<LeetCodeProblem, Long> {

    @Query("SELECT lp FROM LeetCodeProblem lp WHERE lp.primaryTag = :tag " +
           "ORDER BY CASE lp.difficulty " +
           "  WHEN 'Easy' THEN 1 " +
           "  WHEN 'Medium' THEN 2 " +
           "  WHEN 'Hard' THEN 3 " +
           "  ELSE 4 END ASC, lp.id ASC")
    List<LeetCodeProblem> findByTagOrderedByDifficulty(@Param("tag") String tag);

    boolean existsBySlug(String slug);
}
