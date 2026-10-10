// Render the backend's endpoint; this only interpolates chart coordinates.
// Require matching totals so a partial/stale game log cannot imply a false pace.
export function projectionDataset(points, details, cumulative, player = false) {
  const games = points.length
  const { seasonGames, projectedPoints, gamesPlayed } = details || {}
  const total = points.reduce((sum, value) => sum + value, 0)
  if (!cumulative || games === 0 || games !== gamesPlayed || total !== details?.points ||
      !Number.isFinite(projectedPoints) || !Number.isInteger(seasonGames) || games >= seasonGames) {
    return null
  }

  const data = Array.from({ length: seasonGames }, (_, index) => {
    const game = index + 1
    return game < games ? null : total + (projectedPoints - total) * (game - games) / (seasonGames - games)
  })
  return {
    label: player
      ? `${seasonGames}-game pace: ${projectedPoints.toFixed(1)} points`
      : `On pace for ${projectedPoints.toFixed(1)} points`,
    data,
    projected: true,
    borderColor: '#38BDF8',
    borderDash: [8, 5],
    borderWidth: 2,
    tension: 0,
    pointRadius: 0,
    pointHoverRadius: 4
  }
}
