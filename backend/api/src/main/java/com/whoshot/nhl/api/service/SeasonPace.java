package com.whoshot.nhl.api.service;

/** Season length and full-participation point pace, calculated from unrounded totals. */
final class SeasonPace {
    private SeasonPace() {}

    static Integer seasonGames(String seasonId) {
        if (seasonId == null || !seasonId.matches("\\d{8}")) return null;
        int start = Integer.parseInt(seasonId.substring(0, 4));
        int end = Integer.parseInt(seasonId.substring(4));
        if (end != start + 1 || start < 1995 || start == 2004 || start == 2019) return null;
        // 2019-20 ended early, with different game counts per team; no fixed length.
        if (start == 2012) return 48;
        if (start == 2020) return 56;
        return start >= 2026 ? 84 : 82;
    }

    static Double projectedPoints(Integer points, Integer gamesPlayed, Integer seasonGames) {
        if (points == null || points < 0 || gamesPlayed == null || gamesPlayed <= 0
                || seasonGames == null || gamesPlayed >= seasonGames) return null;
        return (double) points / gamesPlayed * seasonGames;
    }
}
