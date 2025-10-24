package com.nhl.whoshotbackend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Scheduled service for periodic NHL data synchronization.
 * Runs at configured intervals to keep data fresh without manual intervention.
 */
@Service
@Slf4j
public class DataSyncScheduler {

    private final DataIntegrationService dataIntegrationService;
    private final StatisticsService statisticsService;
    private final NhlApiService nhlApiService;

    public DataSyncScheduler(
            DataIntegrationService dataIntegrationService,
            StatisticsService statisticsService,
            NhlApiService nhlApiService) {
        this.dataIntegrationService = dataIntegrationService;
        this.statisticsService = statisticsService;
        this.nhlApiService = nhlApiService;
    }

    /**
     * Scheduled job to sync data for current season.
     * Runs every 6 hours (configurable via cron expression).
     * Default: 0 0 (star)/6 * * * (at minute 0, hour 0, 6, 12, 18)
     */
    @Scheduled(cron = "${nhl.sync.cron:0 0 */6 * * *}")
    public void syncCurrentSeasonData() {
        String currentSeason = nhlApiService.getCurrentSeason();
        log.info("=== Starting scheduled data synchronization for season {} ===", currentSeason);

        try {
            // Sync all data from NHL API for current season
            dataIntegrationService.syncStandings(currentSeason);
            dataIntegrationService.syncPlayerStats(currentSeason);

            // Calculate hot ratings and streaks for current season
            statisticsService.calculateHotRatings(currentSeason);

            log.info("=== Scheduled data synchronization completed successfully for season {} ===", currentSeason);
        } catch (Exception e) {
            log.error("Error during scheduled data synchronization for season {}", currentSeason, e);
        }
    }
}
