import test from 'node:test'
import assert from 'node:assert/strict'
import { previousSeasonId } from '../src/composables/seasonDetails.js'

test('derives the comparison season from the backend season ID', () => {
  assert.equal(previousSeasonId('20262027'), '20252026')
  assert.equal(previousSeasonId('20252026'), '20242025')
})

test('missing or invalid metadata from an older API cannot throw or fabricate a season', () => {
  for (const season of [undefined, null, '', 20262027, '2026', 'invalid', '20262028']) {
    assert.equal(previousSeasonId(season), '')
  }
})
