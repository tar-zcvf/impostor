package com.maskedparty.game.game

import com.maskedparty.game.data.RoundWords
import com.maskedparty.game.data.WordCategory
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchTest {

    private fun roundWords() = RoundWords(WordCategory.FOOD, "Sushi", "Ramen")

    @Test
    fun `match requires at least three players`() {
        expectThrow<IllegalArgumentException> { Match(listOf("A", "B"), totalRounds = 1) }
    }

    @Test
    fun `match requires unique player names`() {
        expectThrow<IllegalArgumentException> { Match(listOf("A", "A", "B"), totalRounds = 1) }
    }

    @Test
    fun `match requires at least one round`() {
        expectThrow<IllegalArgumentException> { Match(listOf("A", "B", "C"), totalRounds = 0) }
    }

    @Test
    fun `exactly one imposter, always a player in the match`() {
        repeat(100) { seed ->
            val match = Match(listOf("A", "B", "C", "D"), totalRounds = 2, random = Random(seed.toLong()))
            assertTrue(match.players.contains(match.imposterName))
            assertEquals(1, match.players.count { match.isImposter(it) })
        }
    }

    @Test
    fun `catching the imposter awards every player one team point and the imposter nothing`() {
        val match = Match(listOf("A", "B", "C"), totalRounds = 1)
        val imposter = match.imposterName
        val votes = match.players.associateWith { imposter }
        val outcome = match.resolveRound(votes, roundWords(), roundNumber = 1)
        assertTrue(outcome.imposterCaught)
        match.players.forEach { name ->
            assertEquals("team points for $name", 1, match.teamScoreByName[name])
            assertEquals("imposter points for $name", 0, match.imposterScoreByName[name])
        }
    }

    @Test
    fun `if the imposter escapes, only the imposter scores one point`() {
        val match = Match(listOf("A", "B", "C"), totalRounds = 1)
        val imposter = match.imposterName
        val other = match.players.first { it != imposter }
        val votes = match.players.associateWith { other }
        val outcome = match.resolveRound(votes, roundWords(), roundNumber = 1)
        assertFalse(outcome.imposterCaught)
        assertEquals(1, match.imposterScoreByName[imposter])
        match.players.filter { it != imposter }.forEach { name ->
            assertEquals(0, match.imposterScoreByName[name])
            assertEquals(0, match.teamScoreByName[name])
        }
    }

    @Test
    fun `round outcome reports the ejected player and the vote count`() {
        val match = Match(listOf("A", "B", "C"), totalRounds = 1)
        val imposter = match.imposterName
        val other = match.players.first { it != imposter }
        val votes = mapOf("A" to other, "B" to other, "C" to imposter)
        val outcome = match.resolveRound(votes, roundWords(), roundNumber = 3)
        assertEquals(3, outcome.roundNumber)
        assertEquals(other, outcome.ejected)
        assertEquals(2, outcome.ejections)
        assertEquals(mapOf(other to 2, imposter to 1), outcome.voteTally)
    }

    @Test
    fun `an empty vote map is rejected`() {
        val match = Match(listOf("A", "B", "C"), totalRounds = 1)
        expectThrow<IllegalStateException> { match.resolveRound(emptyMap(), roundWords(), roundNumber = 1) }
    }

    @Test
    fun `imposter identity is stable across rounds`() {
        val match = Match(listOf("A", "B", "C", "D"), totalRounds = 5)
        val first = match.imposterName
        repeat(5) { i ->
            match.resolveRound(match.players.associateWith { first }, roundWords(), roundNumber = i + 1)
            assertEquals(first, match.imposterName)
        }
    }

    private inline fun <reified T : Throwable> expectThrow(block: () -> Unit) {
        try {
            block()
        } catch (t: Throwable) {
            if (t is T) return
            throw AssertionError("Expected ${T::class.java.name} but got ${t::class.java.name}", t)
        }
        throw AssertionError("Expected ${T::class.java.name} but nothing was thrown")
    }
}
