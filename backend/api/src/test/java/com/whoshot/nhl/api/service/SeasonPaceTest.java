package com.whoshot.nhl.api.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class SeasonPaceTest {
    @Test
    void usesTheRequestedSeasonsGameCount() {
        assertThat(SeasonPace.seasonGames("20262027")).isEqualTo(84);
        assertThat(SeasonPace.seasonGames("20272028")).isEqualTo(84);
        assertThat(SeasonPace.seasonGames("20252026")).isEqualTo(82);
        assertThat(SeasonPace.seasonGames("20202021")).isEqualTo(56);
        assertThat(SeasonPace.seasonGames("20122013")).isEqualTo(48);
    }

    @Test
    void doesNotGuessUnknownOrUnevenSeasonLengths() {
        for (String season : new String[] {null, "", "invalid", "20262028", "20192020", "20042005", "19941995"}) {
            assertThat(SeasonPace.seasonGames(season)).isNull();
        }
    }

    @Test
    void projectsFromUnroundedTotals() {
        assertThat(SeasonPace.projectedPoints(6, 5, 84)).isCloseTo(100.8, within(0.000001));
        assertThat(SeasonPace.projectedPoints(1, 3, 84)).isEqualTo(28.0);
        assertThat(SeasonPace.projectedPoints(6, 5, 82)).isCloseTo(98.4, within(0.000001));
        assertThat(SeasonPace.projectedPoints(0, 5, 84)).isEqualTo(0.0);
    }

    @Test
    void omitsProjectionWhenNoGamesRemainOrInputsAreMissing() {
        assertThat(SeasonPace.projectedPoints(6, 0, 84)).isNull();
        assertThat(SeasonPace.projectedPoints(6, null, 84)).isNull();
        assertThat(SeasonPace.projectedPoints(null, 5, 84)).isNull();
        assertThat(SeasonPace.projectedPoints(6, 5, null)).isNull();
        assertThat(SeasonPace.projectedPoints(100, 84, 84)).isNull();
        assertThat(SeasonPace.projectedPoints(100, 85, 84)).isNull();
    }
}
