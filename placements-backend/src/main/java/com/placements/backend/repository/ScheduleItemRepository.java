package com.placements.backend.repository;

import com.placements.backend.entity.ScheduleItem;
import com.placements.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleItemRepository extends JpaRepository<ScheduleItem, Long> {

    @Query("SELECT si FROM ScheduleItem si JOIN si.schedule s JOIN s.user u " +
           "WHERE u.id = :userId AND si.type = com.placements.backend.entity.ItemType.DSA_QUESTION " +
           "AND si.completed = false AND si.leetcodeSlug IN :solvedSlugs")
    List<ScheduleItem> findUncompletedMatchingSlugs(
            @Param("userId") Long userId,
            @Param("solvedSlugs") List<String> solvedSlugs
    );

    @Query("SELECT si.leetcodeSlug FROM ScheduleItem si JOIN si.schedule s JOIN s.user u " +
           "WHERE u.id = :userId AND si.topicKey = :topicKey AND si.leetcodeSlug IS NOT NULL")
    List<String> findSlugsByUserIdAndTopicKey(
            @Param("userId") Long userId,
            @Param("topicKey") String topicKey
    );

    @Query("SELECT si FROM ScheduleItem si JOIN si.schedule s JOIN s.user u " +
           "WHERE si.id = :itemId AND u.id = :userId")
    Optional<ScheduleItem> findByIdAndUserId(
            @Param("itemId") Long itemId,
            @Param("userId") Long userId
    );
}
