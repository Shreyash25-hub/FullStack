package com.placements.backend.controller;

import com.placements.backend.dto.ScheduleDtos.*;
import com.placements.backend.entity.Schedule;
import com.placements.backend.entity.User;
import com.placements.backend.exception.ResourceNotFoundException;
import com.placements.backend.repository.ScheduleRepository;
import com.placements.backend.security.AppUserPrincipal;
import com.placements.backend.service.ScheduleGeneratorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleGeneratorService scheduleGeneratorService;

    public ScheduleController(
            ScheduleRepository scheduleRepository,
            ScheduleGeneratorService scheduleGeneratorService
    ) {
        this.scheduleRepository = scheduleRepository;
        this.scheduleGeneratorService = scheduleGeneratorService;
    }

    @PostMapping
    public ResponseEntity<ScheduleResponse> createSchedule(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @Valid @RequestBody ScheduleCreateRequest request
    ) {
        User user = principal.getUser();
        ScheduleResponse response = scheduleGeneratorService.generateSchedule(user, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ScheduleResponse>> getSchedules(
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        User user = principal.getUser();
        List<Schedule> schedules = scheduleRepository.findAllByUserOrderByCreatedAtDesc(user);
        List<ScheduleResponse> response = schedules.stream()
                .map(scheduleGeneratorService::mapToResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScheduleResponse> getScheduleById(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable Long id
    ) {
        User user = principal.getUser();
        Schedule schedule = scheduleRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + id));

        return ResponseEntity.ok(scheduleGeneratorService.mapToResponse(schedule));
    }
}
