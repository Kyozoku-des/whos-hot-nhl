<template>
  <div class="player-list">
    <div v-if="loading" class="loading">Loading...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else-if="players.length === 0" class="empty">No players found</div>
    <div v-else class="players-grid">
      <div
        v-for="(player, index) in players"
        :key="player.playerId"
        class="player-item"
        :class="{ 'hidden-item': index >= 5 }"
        @click="goToPlayer(player.playerId)"
      >
<<<<<<< HEAD
        <span class="player-info">
          <span class="player-name">{{ player.firstName }} {{ player.lastName }}</span>
          <span class="player-stats">G: {{ player.goals }} | A: {{ player.assists }} | P: {{ player.points }} | GP: {{ player.gamesPlayed }}</span>
        </span>
        <span class="player-icons">
          <img v-if="player.hot" src="../assets/flame.png" alt="Hot" class="status-icon" title="Hot streak (PPG > 1.5)" />
          <img v-if="player.cold" src="../assets/snowflake.png" alt="Cold" class="status-icon" title="Cold streak (PPG < 0.2)" />
          <img v-if="player.pointStreak" src="../assets/graph.png" alt="Point Streak" class="status-icon" title="5+ game point streak" />
=======
        <span class="player-name">{{ player.firstName }} {{ player.lastName }}</span>
        <span class="player-stats">
          <span class="stat-item">G: {{ player.goals }}</span>
          <span class="stat-item">A: {{ player.assists }}</span>
          <span class="stat-item">P: {{ player.points }}</span>
          <span class="stat-item">GP: {{ player.gamesPlayed }}</span>
>>>>>>> dev
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, inject, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { usePlayerStats } from '../composables/useApi'

const router = useRouter()
const { loading, error, getTopScorers } = usePlayerStats()
const players = ref([])
const selectedSeason = inject('selectedSeason')

const loadData = async () => {
  const data = await getTopScorers(50, selectedSeason.value)
  if (data) {
    players.value = data // Load top 50 players
  }
}

onMounted(() => {
  loadData()
})

watch(selectedSeason, () => {
  loadData()
})

const goToPlayer = (playerId) => {
  router.push(`/player/${playerId}`)
}
</script>

<style scoped>
.player-list {
  width: 100%;
}

.players-grid {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  padding-right: 1.5rem;
}

.player-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1rem;
  background-color: rgba(255, 255, 255, 0.05);
  border: 2px solid var(--color-border);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
}

/* Hide items beyond 5 when not expanded */
:global(.expandable-card:not(.expanded)) .player-item.hidden-item {
  display: none;
}

.player-item:hover {
  background-color: rgba(255, 255, 255, 0.1);
  transform: translateX(5px);
  z-index: 10;
}

.player-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.player-name {
  color: var(--color-text-secondary);
  font-size: 1rem;
  font-weight: bold;
  flex: 1;
}

.player-stats {
<<<<<<< HEAD
  color: var(--color-text-primary);
  font-size: 0.85rem;
}

.player-icons {
=======
>>>>>>> dev
  display: flex;
  gap: 1rem;
  align-items: center;
}

.stat-item {
  color: var(--color-text-primary);
  font-size: 0.9rem;
  font-weight: normal;
  white-space: nowrap;
}

.loading,
.error,
.empty {
  text-align: center;
  padding: 2rem;
  color: var(--color-text-secondary);
}
</style>
