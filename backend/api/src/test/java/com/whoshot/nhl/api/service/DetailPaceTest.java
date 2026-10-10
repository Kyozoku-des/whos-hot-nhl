package com.whoshot.nhl.api.service;

import com.whoshot.nhl.domain.entity.CurrentSeason;
import com.whoshot.nhl.domain.entity.Player;
import com.whoshot.nhl.domain.entity.Team;
import com.whoshot.nhl.domain.repository.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DetailPaceTest {
    @Test
    void playerDetailUsesActiveSeasonAndRawTotalsRatherThanRoundedPpg() {
        var players = mock(PlayerRepository.class);
        var seasons = mock(CurrentSeasonRepository.class);
        var active = new CurrentSeason();
        active.setSeasonId("20262027");
        when(seasons.findByIsActiveTrue()).thenReturn(Optional.of(active));
        var player = Player.builder().id(new Player.PlayerId(1L, "20262027"))
                .gamesPlayed(3).points(1).pointsPerGame(0.33).build();
        when(players.findById(player.getId())).thenReturn(Optional.of(player));

        var resolver = new SeasonResolver(seasons);
        var teams = mock(TeamRepository.class);
        when(teams.existsBySeasonIdAndGamesPlayedLessThan("20262027", 84)).thenReturn(true);
        var detail = new PlayerService(players, mock(GameLogRepository.class), resolver, new SeasonPace(teams, resolver))
                .getPlayerDetail(1L, null);

        assertThat(detail.seasonId()).isEqualTo("20262027");
        assertThat(detail.seasonGames()).isEqualTo(84);
        assertThat(detail.projectedPoints()).isEqualTo(28.0);
    }

    @Test
    void teamDetailUsesRequestedSeason() {
        var teams = mock(TeamRepository.class);
        var players = mock(PlayerRepository.class);
        var team = new Team();
        team.setTeamCode("TOR");
        team.setSeasonId("20252026");
        team.setGamesPlayed(5);
        team.setPoints(6);
        when(teams.findByTeamCodeAndSeasonId("TOR", "20252026")).thenReturn(Optional.of(team));
        when(players.findByTeamCodeAndIdSeasonId("TOR", "20252026")).thenReturn(List.of());

        var seasons = mock(CurrentSeasonRepository.class);
        var active = new CurrentSeason();
        active.setSeasonId("20252026");
        when(seasons.findByIsActiveTrue()).thenReturn(Optional.of(active));
        when(teams.existsBySeasonIdAndGamesPlayedLessThan("20252026", 82)).thenReturn(true);
        var resolver = new SeasonResolver(seasons);
        var detail = new TeamService(teams, resolver,
                mock(TeamGameRepository.class), players, new SeasonPace(teams, resolver)).getTeamDetail("TOR", "20252026");

        assertThat(detail.seasonId()).isEqualTo("20252026");
        assertThat(detail.seasonGames()).isEqualTo(82);
        assertThat(detail.projectedPoints()).isCloseTo(98.4, within(0.000001));
    }

    @Test
    void playerWithSeventyGamesHasNoProjectionAfterTheRegularSeasonEnds() {
        var players = mock(PlayerRepository.class);
        var teams = mock(TeamRepository.class);
        var resolver = mock(SeasonResolver.class);
        when(resolver.resolve(null)).thenReturn("20252026");
        var player = Player.builder().id(new Player.PlayerId(1L, "20252026"))
                .gamesPlayed(70).points(90).build();
        when(players.findById(player.getId())).thenReturn(Optional.of(player));
        // No team has regular-season games remaining, despite this still being the active season.
        when(teams.existsBySeasonIdAndGamesPlayedLessThan("20252026", 82)).thenReturn(false);

        var detail = new PlayerService(players, mock(GameLogRepository.class), resolver, new SeasonPace(teams, resolver))
                .getPlayerDetail(1L, null);

        assertThat(detail.projectedPoints()).isNull();
        assertThat(detail.gamesPlayed()).isEqualTo(70);
    }

    @Test
    void historicalSeasonWithIncompletePlayerStatsHasNoProjection() {
        var players = mock(PlayerRepository.class);
        var teams = mock(TeamRepository.class);
        var resolver = mock(SeasonResolver.class);
        when(resolver.resolve(null)).thenReturn("20262027");
        when(resolver.resolve("20252026")).thenReturn("20252026");
        var player = Player.builder().id(new Player.PlayerId(1L, "20252026"))
                .gamesPlayed(70).points(90).build();
        when(players.findById(player.getId())).thenReturn(Optional.of(player));
        when(teams.existsBySeasonIdAndGamesPlayedLessThan("20252026", 82)).thenReturn(true);

        var detail = new PlayerService(players, mock(GameLogRepository.class), resolver, new SeasonPace(teams, resolver))
                .getPlayerDetail(1L, "20252026");

        assertThat(detail.seasonGames()).isEqualTo(82);
        assertThat(detail.projectedPoints()).isNull();
    }
}
