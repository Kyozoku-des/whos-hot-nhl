// Older detail responses have no seasonId. Keep current logs usable, but avoid
// guessing a previous season or requesting one under a mismatched label.
export function previousSeasonId(seasonId) {
  if (typeof seasonId !== 'string' || !/^\d{8}$/.test(seasonId)) return ''
  const startYear = Number(seasonId.slice(0, 4))
  if (Number(seasonId.slice(4)) !== startYear + 1) return ''
  return `${startYear - 1}${startYear}`
}
