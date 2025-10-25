<template>
  <div class="team-page">
    <div class="container">
      <div v-if="loading" class="loading">Loading...</div>
      <div v-else-if="error" class="error">{{ error }}</div>
      <div v-else class="team-content">
        <div class="team-header">
          <div class="team-info-card">
            <TeamLogo
              :logoUrl="team?.logoUrl"
              :teamCode="team?.teamCode"
              :alt="team?.teamName"
              size="large"
            />
            <div class="team-stats">
              <h1 class="team-name">{{ team?.teamName }}</h1>
              <p class="stat-line">Conference: {{ team?.conferenceName || 'N/A' }}</p>
              <p class="stat-line">Division: {{ team?.divisionName || 'N/A' }}</p>
              <p class="stat-line">Record: {{ formatRecord(team) }}</p>
              <p class="stat-line">Points: {{ team?.points ?? 0 }}</p>
            </div>
          </div>
        </div>

        <div class="section">
          <h2 class="section-title">Season Snapshot</h2>
          <div class="stats-grid">
            <div class="stat-item">
              <span class="stat-label">Games Played</span>
              <span class="stat-value">{{ team?.gamesPlayed ?? 0 }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">Goal Differential</span>
              <span class="stat-value" :class="diffClass(team?.goalDifferential)">
                {{ formatGoalDifferential(team?.goalDifferential) }}
              </span>
            </div>
            <div class="stat-item">
              <span class="stat-label">Point %</span>
              <span class="stat-value">{{ formatPercentage(team?.pointPercentage) }}%</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">Last 10 Win %</span>
              <span class="stat-value">{{ formatPercentage(team?.last10GamesWinPercentage) }}%</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">Current Win Streak</span>
              <span class="stat-value">{{ team?.currentWinStreak ?? 0 }} GP</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">Current Loss Streak</span>
              <span class="stat-value">{{ team?.currentLossStreak ?? 0 }} GP</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">Hot Status</span>
              <span class="stat-value" :class="{ positive: team?.hot, negative: team?.cold }">
                {{ formatStreakStatus(team) }}
              </span>
            </div>
            <div class="stat-item" v-if="team?.nextOpponentCode">
              <span class="stat-label">Next Game</span>
              <span class="stat-value">{{ formatNextGame(team) }}</span>
            </div>
          </div>
        </div>

        <div class="section">
          <h2 class="section-title">Team Statistics</h2>
          <div class="stats-grid">
            <div class="stat-item">
              <span class="stat-label">Goals For</span>
              <span class="stat-value">{{ team?.goalsFor ?? 0 }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">Goals Against</span>
              <span class="stat-value">{{ team?.goalsAgainst ?? 0 }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">Overtime Losses</span>
              <span class="stat-value">{{ team?.overtimeLosses ?? 0 }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">Last Updated</span>
              <span class="stat-value">{{ formatDate(team?.lastUpdated) }}</span>
            </div>
          </div>
        </div>

        <div class="section">
          <h2 class="section-title">Win/Loss Progression</h2>
          <TeamGameLogGraph
            :current-season-data="teamGameLogs"
            :previous-season-data="previousSeasonTeamGameLogs"
          />
        </div>

        <div class="section">
          <h2 class="section-title">Roster</h2>
          <p class="placeholder-text">Team roster would be displayed here</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useTeamStats } from '../composables/useApi'
import TeamLogo from '../components/TeamLogo.vue'
import TeamGameLogGraph from '../components/TeamGameLogGraph.vue'

const route = useRoute()
const { loading, error, getTeamDetails, getTeamGameLog } = useTeamStats()

const team = ref(null)
const teamGameLogs = ref([])
const previousSeasonTeamGameLogs = ref([])

const formatRecord = (teamData) => {
  if (!teamData) return '0-0-0'
  const wins = teamData.wins ?? 0
  const losses = teamData.losses ?? 0
  const ot = teamData.overtimeLosses ?? 0
  return `${wins}-${losses}-${ot}`
}

const formatGoalDifferential = (diff) => {
  if (diff == null) return '0'
  return diff > 0 ? `+${diff}` : diff.toString()
}

const diffClass = (diff) => {
  if (diff == null || diff === 0) return ''
  return diff > 0 ? 'positive' : 'negative'
}

const formatPercentage = (value) => {
  if (value == null) return '0.0'
  return (Number(value) * 100).toFixed(1)
}

const formatDate = (dateString) => {
  if (!dateString) return 'N/A'
  const date = new Date(dateString)
  if (Number.isNaN(date.getTime())) {
    return dateString
  }
  return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })
}

const formatNextGame = (teamData) => {
  if (!teamData || !teamData.nextOpponentCode) return 'TBD'
  const location = teamData.nextGameIsHome ? 'vs' : '@'
  const date = teamData.nextGameDate ? formatDate(teamData.nextGameDate) : ''
  return `${location} ${teamData.nextOpponentCode}${date ? ` • ${date}` : ''}`
}

const formatStreakStatus = (teamData) => {
  if (!teamData) return 'Neutral'
  if (teamData.hot) return 'Hot'
  if (teamData.cold) return 'Cold'
  if (teamData.pointStreak) return 'Point Streak'
  return 'Neutral'
}

// Calculate previous season ID
const calculatePreviousSeason = () => {
  const currentYear = new Date().getFullYear()
  const currentMonth = new Date().getMonth() + 1

  let seasonStartYear
  if (currentMonth >= 10) {
    seasonStartYear = currentYear
  } else {
    seasonStartYear = currentYear - 1
  }

  const previousStartYear = seasonStartYear - 1
  const previousEndYear = seasonStartYear

  return `${previousStartYear}${previousEndYear}`
}

onMounted(async () => {
  const teamId = route.params.id

  const teamData = await getTeamDetails(teamId)
  if (teamData) {
    team.value = teamData
  }

  // Fetch current season game log
  const gameLogData = await getTeamGameLog(teamId)
  if (gameLogData) {
    teamGameLogs.value = gameLogData
  }

  // Fetch previous season game log
  const previousSeason = calculatePreviousSeason()
  const previousSeasonData = await getTeamGameLog(teamId, previousSeason)
  if (previousSeasonData) {
    previousSeasonTeamGameLogs.value = previousSeasonData
  }
})
</script>

<style scoped>
.team-page {
  width: 100%;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 2rem;
}

.team-content {
  display: grid;
  gap: 2rem;
}

.team-header {
  display: flex;
  gap: 2rem;
}

.team-info-card {
  background-color: var(--color-bg-card);
  border-radius: 8px;
  padding: 2rem;
  display: flex;
  gap: 2rem;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}


.team-stats {
  flex: 1;
}

.team-name {
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 1rem;
}

.stat-line {
  font-size: 1.1rem;
  margin: 0.5rem 0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  background-color: rgba(255, 255, 255, 0.08);
  border-radius: 6px;
  padding: 1rem;
}

.stat-label {
  font-size: 0.85rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--color-text-secondary);
}

.stat-value {
  font-size: 1.3rem;
  font-weight: 700;
}

.stat-value.positive {
  color: #4ade80;
}

.stat-value.negative {
  color: #f87171;
}

.section {
  background-color: var(--color-bg-card);
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.section-title {
  font-size: 1.5rem;
  font-weight: 700;
  margin-bottom: 1rem;
  text-align: center;
}

.placeholder-text {
  text-align: center;
  padding: 2rem;
  color: var(--color-text-secondary);
}

.loading,
.error {
  text-align: center;
  padding: 3rem;
  font-size: 1.2rem;
}

.error {
  color: #d32f2f;
}

@media (max-width: 768px) {
  .team-info-card {
    flex-direction: column;
    text-align: center;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
