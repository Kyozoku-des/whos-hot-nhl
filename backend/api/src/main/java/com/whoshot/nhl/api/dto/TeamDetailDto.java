package com.whoshot.nhl.api.dto;

import java.util.List;

/**
 * DTO representing detailed team information including roster.
 * Includes season metadata, point pace, and a roster list.
 */
public record TeamDetailDto(
        String teamCode,
        String teamName,
        String logoUrl,
        Integer gamesPlayed,
        Integer wins,
        Integer losses,
        Integer overtimeLosses,
        Integer points,
        Double pointPercentage,
        Integer goalsFor,
        Integer goalsAgainst,
        Integer goalDifferential,
        String conferenceName,
        String divisionName,
        Integer currentWinStreak,
        Integer currentLossStreak,
        Double last10GamesPointPercentage,
        Double last10GamesPPG,
        List<RosterPlayerDto> roster,
        String seasonId,
        Integer seasonGames,
        Double projectedPoints
) {}
