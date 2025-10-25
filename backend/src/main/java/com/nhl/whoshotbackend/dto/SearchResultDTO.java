package com.nhl.whoshotbackend.dto;

/**
 * DTO representing a generic search result entry that can be a player or a team.
 */
public record SearchResultDTO(
        String type,
        String id,
        String label,
        String subLabel
) {
}
