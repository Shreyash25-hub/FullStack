package com.placements.backend.controller;

import com.placements.backend.dto.LeetcodeSyncResponse;
import com.placements.backend.dto.UserProfileUpdateRequest;
import com.placements.backend.entity.User;
import com.placements.backend.repository.UserRepository;
import com.placements.backend.security.AppUserPrincipal;
import com.placements.backend.service.LeetCodeUserSyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final LeetCodeUserSyncService leetCodeUserSyncService;

    public UserController(UserRepository userRepository, LeetCodeUserSyncService leetCodeUserSyncService) {
        this.userRepository = userRepository;
        this.leetCodeUserSyncService = leetCodeUserSyncService;
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getProfile(@AuthenticationPrincipal AppUserPrincipal principal) {
        User user = principal.getUser();
        // Reload from database to ensure fresh state
        user = userRepository.findById(user.getId()).orElse(user);

        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("email", user.getEmail());
        response.put("leetcodeUsername", user.getLeetcodeUsername());
        response.put("lastLeetcodeSyncAt", user.getLastLeetcodeSyncAt());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/me")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @RequestBody UserProfileUpdateRequest request
    ) {
        User user = principal.getUser();
        user = userRepository.findById(user.getId()).orElse(user);
        user.setLeetcodeUsername(request.getLeetcodeUsername());
        user = userRepository.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("leetcodeUsername", user.getLeetcodeUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/me/leetcode-sync")
    public ResponseEntity<LeetcodeSyncResponse> syncLeetcode(
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        User user = principal.getUser();
        user = userRepository.findById(user.getId()).orElse(user);

        int count = leetCodeUserSyncService.syncUserProgress(user);
        return ResponseEntity.ok(new LeetcodeSyncResponse(count));
    }
}
