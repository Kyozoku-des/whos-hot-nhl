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

        var detail = new PlayerService(players, mock(GameLogRepository.class), new SeasonResolver(seasons))
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

        var detail = new TeamService(teams, new SeasonResolver(mock(CurrentSeasonRepository.class)),
                mock(TeamGameRepository.class), players).getTeamDetail("TOR", "20252026");

        assertThat(detail.seasonId()).isEqualTo("20252026");
        assertThat(detail.seasonGames()).isEqualTo(82);
        assertThat(detail.projectedPoints()).isCloseTo(98.4, within(0.000001));
    }
}
