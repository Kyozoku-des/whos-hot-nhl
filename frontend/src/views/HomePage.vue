<template>
  <div class="home-page">
    <div class="header">
      <div class="brand-block">
        <h1 class="title">WHOS HOT NHL</h1>
        <CurrentSeasonDisplay />
      </div>

      <div class="search-area">
        <div class="search-input-wrapper" :class="{ active: showSuggestions }">
          <input
            v-model="searchQuery"
            type="search"
            class="search-input"
            placeholder="Search players or teams"
            @keydown.enter.prevent="applyFilterImmediately"
            @keydown.esc.prevent="closeSuggestions"
            @focus="handleFocus"
            @blur="handleBlur"
          />
          <button
            v-if="searchQuery"
            class="clear-input"
            type="button"
            aria-label="Clear search"
            @mousedown.prevent
            @click="clearQuery"
          >
            ✕
          </button>
        </div>

        <div v-if="showSuggestions" class="search-suggestions" @mousedown.prevent>
          <div v-if="searchLoading" class="suggestion-state">Searching…</div>
          <div v-else-if="searchError" class="suggestion-state suggestion-error">{{ searchError }}</div>
          <template v-else>
            <div v-if="suggestions.length === 0" class="suggestion-state suggestion-empty">No matches found</div>
            <ul v-else class="suggestion-list">
              <li
                v-for="option in suggestions"
                :key="`${option.type}-${option.id}`"
                class="suggestion-item"
                @click="selectSuggestion(option)"
              >
                <span class="suggestion-label">{{ option.label }}</span>
                <span class="suggestion-meta">{{ formatSuggestionMeta(option) }}</span>
              </li>
            </ul>
          </template>
        </div>
      </div>
    </div>

    <div class="content-container">
      <div v-if="hasActiveFilter" class="filter-banner">
        <span>Filtering dashboard for “<strong>{{ activeFilter }}</strong>”.</span>
        <button type="button" class="filter-clear" @click="clearFilter">Clear filter</button>
      </div>

      <div class="cards-grid">
        <ExpandableCard title="Player standings">
          <TopPointsTable :search-term="activeFilter" />
        </ExpandableCard>

        <ExpandableCard title="Point streaks">
          <PointStreaksTable :search-term="activeFilter" />
        </ExpandableCard>

        <ExpandableCard title="Whos hot">
          <HottestPlayersTable :search-term="activeFilter" />
        </ExpandableCard>

        <ExpandableCard title="Team standings">
          <TeamStandingsTable :search-term="activeFilter" />
        </ExpandableCard>

        <ExpandableCard title="Win streaks">
          <TeamWinStreaksTable :search-term="activeFilter" />
        </ExpandableCard>

        <ExpandableCard title="Last 10 games">
          <TeamHotTable :search-term="activeFilter" />
        </ExpandableCard>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import ExpandableCard from '../components/ExpandableCard.vue'
import TopPointsTable from '../components/TopPointsTable.vue'
import PointStreaksTable from '../components/PointStreaksTable.vue'
import HottestPlayersTable from '../components/HottestPlayersTable.vue'
import TeamStandingsTable from '../components/TeamStandingsTable.vue'
import TeamWinStreaksTable from '../components/TeamWinStreaksTable.vue'
import TeamHotTable from '../components/TeamHotTable.vue'
import CurrentSeasonDisplay from '../components/CurrentSeasonDisplay.vue'
import { useSearch } from '../composables/useApi'

const router = useRouter()

const searchQuery = ref('')
const activeFilter = ref('')
const suggestions = ref([])
const isFocused = ref(false)
const { loading: searchLoading, error: searchError, search: runSearch } = useSearch()

let suggestionDebounceId = null
let filterDebounceId = null
let blurTimeoutId = null

watch(searchQuery, (newValue) => {
  if (suggestionDebounceId) {
    clearTimeout(suggestionDebounceId)
  }

  const term = newValue.trim()

  if (term.length < 2) {
    suggestions.value = []
    return
  }

  const pendingTerm = term

  suggestionDebounceId = setTimeout(async () => {
    const results = await runSearch(pendingTerm, 8)
    if (searchQuery.value.trim() === pendingTerm) {
      suggestions.value = results ?? []
    }
  }, 250)
})

watch(searchQuery, (newValue) => {
  if (filterDebounceId) {
    clearTimeout(filterDebounceId)
  }

  const trimmed = newValue.trim()

  if (trimmed.length === 0) {
    activeFilter.value = ''
    return
  }

  filterDebounceId = setTimeout(() => {
    activeFilter.value = trimmed
  }, 1000)
})

const showSuggestions = computed(() => {
  return (
    isFocused.value &&
    searchQuery.value.trim().length >= 2 &&
    (searchLoading.value || suggestions.value.length > 0 || !!searchError.value)
  )
})

const hasActiveFilter = computed(() => activeFilter.value.trim().length > 0)

const handleFocus = () => {
  if (blurTimeoutId) {
    clearTimeout(blurTimeoutId)
  }
  isFocused.value = true
}

const handleBlur = () => {
  blurTimeoutId = setTimeout(() => {
    isFocused.value = false
  }, 150)
}

const closeSuggestions = () => {
  if (blurTimeoutId) {
    clearTimeout(blurTimeoutId)
  }
  isFocused.value = false
  suggestions.value = []
}

const applyFilterImmediately = () => {
  if (filterDebounceId) {
    clearTimeout(filterDebounceId)
  }
  activeFilter.value = searchQuery.value.trim()
  closeSuggestions()
}

const clearQuery = () => {
  searchQuery.value = ''
  suggestions.value = []
  activeFilter.value = ''
}

const clearFilter = () => {
  activeFilter.value = ''
  clearQuery()
}

const selectSuggestion = (option) => {
  closeSuggestions()
  searchQuery.value = ''

  if (option.type === 'player') {
    router.push(`/player/${option.id}`)
    return
  }

  if (option.type === 'team') {
    router.push(`/team/${option.id}`)
  }
}

const formatSuggestionMeta = (option) => {
  const base = option.type === 'player' ? 'Player' : option.type === 'team' ? 'Team' : ''
  if (!option.subLabel) {
    return base
  }
  return `${base} • ${option.subLabel}`
}

onBeforeUnmount(() => {
  if (suggestionDebounceId) {
    clearTimeout(suggestionDebounceId)
  }
  if (filterDebounceId) {
    clearTimeout(filterDebounceId)
  }
  if (blurTimeoutId) {
    clearTimeout(blurTimeoutId)
  }
})
</script>

<style scoped>
.home-page {
  width: 100%;
  min-height: 100vh;
  background-color: var(--color-bg-primary);
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 1.5rem;
  border-bottom: var(--color-border-thick) solid var(--color-border);
  gap: 2rem;
  background-color: var(--color-bg-card);
  flex-wrap: wrap;
}

.title {
  font-size: 2rem;
  font-weight: bold;
  color: var(--color-text-primary);
  letter-spacing: 3px;
  text-transform: uppercase;
  margin: 0;
  padding-right: 2rem;
  border-right: var(--color-border-thick) solid var(--color-border);
  flex-shrink: 0;
}

.brand-block {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  flex: 1;
  flex-wrap: wrap;
}

.brand-block :deep(.current-season-display) {
  margin-left: auto;
}

.search-area {
  position: relative;
  flex: 1;
  max-width: 480px;
  min-width: 260px;
}

.search-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  background-color: rgba(0, 0, 0, 0.2);
  border: 2px solid var(--color-border);
  border-radius: 999px;
  padding: 0.25rem 0.75rem;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.search-input-wrapper.active {
  border-color: var(--color-text-secondary);
  box-shadow: 0 0 0 4px rgba(255, 170, 0, 0.15);
}

.search-input {
  flex: 1;
  border: none;
  background: transparent;
  color: var(--color-text-secondary);
  font-size: 1rem;
  padding: 0.35rem 0.25rem;
  outline: none;
}

.search-input::placeholder {
  color: rgba(255, 255, 255, 0.45);
}

.clear-input {
  background: none;
  border: none;
  color: var(--color-text-secondary);
  font-size: 1rem;
  cursor: pointer;
  padding: 0.25rem;
  line-height: 1;
}

.search-suggestions {
  position: absolute;
  top: calc(100% + 0.5rem);
  left: 0;
  right: 0;
  background-color: var(--color-bg-card);
  border: 2px solid var(--color-border);
  border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.45);
  z-index: 20;
  max-height: 360px;
  overflow-y: auto;
  padding: 0.5rem 0;
}

.suggestion-state {
  padding: 0.75rem 1rem;
  color: var(--color-text-secondary);
  font-size: 0.9rem;
}

.suggestion-error {
  color: #f87171;
}

.suggestion-empty {
  font-style: italic;
}

.suggestion-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.suggestion-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.6rem 1rem;
  gap: 1rem;
  cursor: pointer;
  transition: background-color 0.15s ease;
}

.suggestion-item:hover {
  background-color: rgba(255, 255, 255, 0.08);
}

.suggestion-label {
  color: var(--color-text-primary);
  font-weight: 600;
}

.suggestion-meta {
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.85rem;
}

.filter-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  background-color: rgba(255, 255, 255, 0.08);
  border: 2px solid var(--color-border);
  border-radius: 10px;
  padding: 0.85rem 1.25rem;
  color: var(--color-text-secondary);
  margin-bottom: 1.5rem;
}

.filter-banner strong {
  color: var(--color-text-primary);
}

.filter-clear {
  background: none;
  border: 2px solid var(--color-border);
  border-radius: 999px;
  color: var(--color-text-secondary);
  padding: 0.3rem 0.9rem;
  cursor: pointer;
  transition: background-color 0.2s ease, color 0.2s ease;
}

.filter-clear:hover {
  background-color: var(--color-text-secondary);
  color: var(--color-bg-primary);
}

.content-container {
  padding: 4rem 2rem 2rem 2rem;
}

.cards-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  grid-template-rows: repeat(2, 1fr);
  gap: 1.5rem;
  max-width: 1600px;
  margin: 0 auto;
  height: calc(100vh - 200px);
  min-height: 700px;
}

@media (max-width: 1200px) {
  .cards-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1.5rem;
    padding: 1.5rem;
  }

  .title {
    font-size: 1.8rem;
  }

  .brand-block {
    width: 100%;
  }

  .search-area {
    width: 100%;
    max-width: none;
  }

  .content-container {
    padding: 1.5rem;
  }

  .cards-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
}
</style>
