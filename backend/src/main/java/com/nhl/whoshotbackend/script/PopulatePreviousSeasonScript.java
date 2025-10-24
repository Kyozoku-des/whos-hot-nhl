package com.nhl.whoshotbackend.script;

import com.nhl.whoshotbackend.service.DataIntegrationService;
import com.nhl.whoshotbackend.service.NhlApiService;
import com.nhl.whoshotbackend.service.StatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Script to populate previous season data.
 * This should be run once to fetch and store historical data.
 *
 * Usage:
 * mvn spring-boot:run -Dspring-boot.run.arguments="--populate-previous-season=true"
 *
 * Or add to application.properties:
 * populate-previous-season=true
 */
@Component
@Slf4j
@ConditionalOnProperty(name = "populate-previous-season", havingValue = "true")
public class PopulatePreviousSeasonScript implements CommandLineRunner {

    private final DataIntegrationService dataIntegrationService;
    private final StatisticsService statisticsService;
    private final NhlApiService nhlApiService;

    public PopulatePreviousSeasonScript(
            DataIntegrationService dataIntegrationService,
            StatisticsService statisticsService,
            NhlApiService nhlApiService) {
        this.dataIntegrationService = dataIntegrationService;
        this.statisticsService = statisticsService;
        this.nhlApiService = nhlApiService;
    }

    @Override
    public void run(String... args) {
        String currentSeason = nhlApiService.getCurrentSeason();
        String previousSeason = calculatePreviousSeason(currentSeason);

        log.info("=== Starting previous season data population ===");
        log.info("Current season: {}", currentSeason);
        log.info("Previous season: {}", previousSeason);

        try {
            // Sync previous season data
            log.info("Fetching standings for season {}...", previousSeason);
            dataIntegrationService.syncStandings(previousSeason);

            log.info("Fetching player stats for season {}...", previousSeason);
            dataIntegrationService.syncPlayerStats(previousSeason);

            // Calculate hot ratings and streaks for previous season
            log.info("Calculating statistics for season {}...", previousSeason);
            statisticsService.calculateHotRatings(previousSeason);

            log.info("=== Previous season data population completed successfully ===");
            log.info("Previous season {} data is now available in the database", previousSeason);
        } catch (Exception e) {
            log.error("Error during previous season data population", e);
            throw new RuntimeException("Failed to populate previous season data", e);
        }
    }

    /**
     * Calculate the previous season ID from the current season ID.
     * Season format: YYYYYYYY (e.g., 20252026)
     */
    private String calculatePreviousSeason(String currentSeason) {
        if (currentSeason == null || currentSeason.length() != 8) {
            throw new IllegalArgumentException("Invalid season format: " + currentSeason);
        }

        int startYear = Integer.parseInt(currentSeason.substring(0, 4));
        int endYear = Integer.parseInt(currentSeason.substring(4, 8));

        int previousStartYear = startYear - 1;
        int previousEndYear = endYear - 1;

        return String.format("%d%d", previousStartYear, previousEndYear);
    }
}
