package com.nhl.whoshotbackend.config;

import com.nhl.whoshotbackend.service.DataIntegrationService;
import com.nhl.whoshotbackend.service.NhlApiService;
import com.nhl.whoshotbackend.service.StatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Initializes application on startup.
 * Data synchronization is handled by scheduled jobs (see DataSyncScheduler).
 * Previous season data should be populated using the PopulatePreviousSeasonScript.
 */
@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final NhlApiService nhlApiService;

    public DataInitializer(NhlApiService nhlApiService) {
        this.nhlApiService = nhlApiService;
    }

    @Override
    public void run(String... args) {
        String currentSeason = nhlApiService.getCurrentSeason();
        log.info("=== Application started successfully ===");
        log.info("Current NHL season: {}", currentSeason);
        log.info("Data synchronization is handled by scheduled jobs");
        log.info("To populate previous season data, run: PopulatePreviousSeasonScript");
    }
}
