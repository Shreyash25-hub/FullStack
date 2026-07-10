package com.placements.backend.repository;

import com.placements.backend.entity.Schedule;
import com.placements.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findAllByUserOrderByCreatedAtDesc(User user);
    Optional<Schedule> findByIdAndUser(Long id, User user);
}
