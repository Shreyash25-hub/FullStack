package com.placements.backend.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledSyncJobs {

    private final LeetCodeUserSyncService leetCodeUserSyncService;

    public ScheduledSyncJobs(LeetCodeUserSyncService leetCodeUserSyncService) {
        this.leetCodeUserSyncService = leetCodeUserSyncService;
    }

    @Scheduled(cron = "${app.leetcode.sync-cron:0 0 * * * *}")
    public void syncAllUsersProgress() {
        leetCodeUserSyncService.syncAllUsers();
    }
}
