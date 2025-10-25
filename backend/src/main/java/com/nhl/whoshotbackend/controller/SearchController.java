package com.nhl.whoshotbackend.controller;

import com.nhl.whoshotbackend.dto.SearchResultDTO;
import com.nhl.whoshotbackend.service.NhlApiService;
import com.nhl.whoshotbackend.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller that exposes search endpoints for players and teams.
 */
@RestController
@RequestMapping("/api/search")
@Tag(name = "Search", description = "Search players and teams")
@Slf4j
@CrossOrigin(origins = "*")
public class SearchController {

    private final StatisticsService statisticsService;
    private final NhlApiService nhlApiService;

    public SearchController(StatisticsService statisticsService, NhlApiService nhlApiService) {
        this.statisticsService = statisticsService;
        this.nhlApiService = nhlApiService;
    }

    @GetMapping
    @Operation(summary = "Search for players or teams", description = "Returns matching players and teams for the provided query")
    public ResponseEntity<List<SearchResultDTO>> search(
            @RequestParam String query,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String season) {
        String actualSeason = season != null ? season : nhlApiService.getCurrentSeason();
        int effectiveLimit = limit != null ? limit : 10;
        log.info("GET /api/search?query={}&season={}&limit={}", query, actualSeason, effectiveLimit);
        List<SearchResultDTO> results = statisticsService.search(actualSeason, query, effectiveLimit);
        return ResponseEntity.ok(results);
    }
}
