package com.placements.backend.controller;

import com.placements.backend.service.LeetCodeProblemImportService;
import com.placements.backend.service.YoutubePlaylistImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/import")
public class AdminImportController {

    private final LeetCodeProblemImportService leetCodeProblemImportService;
    private final YoutubePlaylistImportService youtubePlaylistImportService;

    public AdminImportController(
            LeetCodeProblemImportService leetCodeProblemImportService,
            YoutubePlaylistImportService youtubePlaylistImportService
    ) {
        this.leetCodeProblemImportService = leetCodeProblemImportService;
        this.youtubePlaylistImportService = youtubePlaylistImportService;
    }

    @PostMapping("/leetcode-catalog")
    public ResponseEntity<Map<String, Integer>> importLeetcodeCatalog() {
        return ResponseEntity.ok(leetCodeProblemImportService.importCatalog());
    }

    @PostMapping("/youtube-playlist")
    public ResponseEntity<Map<String, Object>> importYoutubePlaylist() {
        return ResponseEntity.ok(youtubePlaylistImportService.importPlaylist());
    }
}
