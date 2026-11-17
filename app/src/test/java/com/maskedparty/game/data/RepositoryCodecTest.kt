package com.maskedparty.game.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RepositoryCodecTest {

    @Test
    fun `encode and parse round-trip a player list`() {
        val names = listOf("Ari", "Bo", "Céline", "Zoë")
        val encoded = GameRepository.encodeList(names)
        assertEquals(names, GameRepository.parseList(encoded))
    }

    @Test
    fun `sanitize trims and replaces the separator so names cannot break the format`() {
        val dirty = "  Bo ¦ Ari  "
        val safe = GameRepository.sanitize(dirty)
        assertTrue("separator must be removed: $safe", !safe.contains("¦"))
        // Round-trip: a dirty name cannot inject extra list entries.
        val parsed = GameRepository.parseList(GameRepository.encodeList(listOf(dirty)))
        assertEquals(1, parsed.size)
    }

    @Test
    fun `empty or blank names fall back to Player`() {
        assertEquals("Player", GameRepository.sanitize("   "))
    }

    @Test
    fun `parseList handles null and empty`() {
        assertTrue(GameRepository.parseList(null).isEmpty())
        assertTrue(GameRepository.parseList("").isEmpty())
    }

    @Test
    fun `score encode and parse round-trip and ignore malformed entries`() {
        val scores = mapOf("Ari" to 3, "Bo" to 1)
        val encoded = GameRepository.encodeScores(scores)
        assertEquals(scores, GameRepository.parseScores(encoded))

        // Malformed entries are dropped, valid ones kept.
        val mixed = GameRepository.encodeScores(mapOf("Ari" to 5)) + "¦garbage" + "¦Bo=notanumber"
        val parsed = GameRepository.parseScores(mixed)
        assertEquals(5, parsed["Ari"])
        assertTrue(parsed["Bo"] == null)
    }

    @Test
    fun `parseScores handles null`() {
        assertTrue(GameRepository.parseScores(null).isEmpty())
    }
}
