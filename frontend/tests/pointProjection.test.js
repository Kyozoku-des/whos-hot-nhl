import test from 'node:test'
import assert from 'node:assert/strict'
import { projectionDataset } from '../src/composables/pointProjection.js'

const points = [2, 0, 1, 2, 1]
const details = { points: 6, gamesPlayed: 5, seasonGames: 84, projectedPoints: 100.8 }

test('keeps actual games separate and extends from game 5 to the backend endpoint', () => {
  const dataset = projectionDataset(points, details, true)
  assert.deepEqual(dataset.data.slice(0, 4), [null, null, null, null])
  assert.equal(dataset.data[4], 6)
  assert.equal(dataset.data.length, 84)
  assert.equal(dataset.data[83], 100.8)
  assert.ok(Math.abs(dataset.data[5] - 7.2) < 1e-10)
  assert.equal(dataset.tension, 0)
  assert.deepEqual(points, [2, 0, 1, 2, 1])
  assert.match(dataset.label, /100\.8/)
})

test('uses a supplied backend endpoint instead of recalculating pace', () => {
  const dataset = projectionDataset(points, { ...details, projectedPoints: 123.4 }, true, true)
  assert.equal(dataset.data[83], 123.4)
  assert.match(dataset.label, /84-game pace/)
})

test('hides projections in per-game mode, for empty logs, or missing totals', () => {
  assert.equal(projectionDataset(points, details, false), null)
  assert.equal(projectionDataset([], details, true), null)
  assert.equal(projectionDataset(points, {}, true), null)
  assert.equal(projectionDataset(points, { ...details, projectedPoints: null }, true), null)
})

test('hides projections for completed games and inconsistent detail/log snapshots', () => {
  assert.equal(projectionDataset(points, { ...details, seasonGames: 5 }, true), null)
  assert.equal(projectionDataset(points, { ...details, gamesPlayed: 6 }, true), null)
  assert.equal(projectionDataset(points, { ...details, points: 7 }, true), null)
})

test('renders a zero-point pace and a shorter historical season', () => {
  assert.equal(projectionDataset([0], { points: 0, gamesPlayed: 1, seasonGames: 84, projectedPoints: 0 }, true).data[83], 0)
  const dataset = projectionDataset(points, { ...details, seasonGames: 82, projectedPoints: 98.4 }, true)
  assert.equal(dataset.data.length, 82)
  assert.equal(dataset.data[81], 98.4)
})
