package com.maskedparty.game

import com.maskedparty.game.data.WordCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameViewModelTest {

    private fun vm() = GameViewModel()

    @Test
    fun `adding a player validates empty and duplicate names`() {
        val v = vm()
        v.updatePlayerName("")
        assertFalse(v.addPlayer())
        v.updatePlayerName("  ")
        assertFalse(v.addPlayer())

        v.updatePlayerName("Ari")
        assertTrue(v.addPlayer())

        v.updatePlayerName("ari") // duplicate, case-insensitive
        assertFalse(v.addPlayer())
        assertEquals(listOf("You", "Ari"), v.state.value.players)
    }

    @Test
    fun `removing a player works and stays in bounds`() {
        val v = vm()
        v.updatePlayerName("Ari"); v.addPlayer()
        v.updatePlayerName("Bo"); v.addPlayer()
        assertEquals(3, v.state.value.players.size)
        v.removePlayerAt(0)
        assertEquals(2, v.state.value.players.size)
        v.removePlayerAt(99) // out of bounds -> no-op
        assertEquals(2, v.state.value.players.size)
    }

    @Test
    fun `cannot start a match with fewer than three players`() {
        val v = vm()
        v.goHome()
        v.updatePlayerName("Solo"); v.addPlayer() // now 2 players total
        v.startMatch()
        assertEquals(Phase.SETUP, v.state.value.phase)
        assertNotNull(v.state.value.nameError)
    }

    @Test
    fun `starting a match enters the word reveal phase with words drawn`() {
        val v = vm()
        v.updatePlayerName("Ari"); v.addPlayer()
        v.updatePlayerName("Bo"); v.addPlayer()
        v.setRounds(2)
        v.startMatch()
        val s = v.state.value
        assertEquals(Phase.WORD_REVEAL, s.phase)
        assertNotNull(s.match)
        assertNotNull(s.roundWords)
        assertEquals(1, s.roundNumber)
        assertEquals(2, s.match!!.totalRounds)
        assertTrue(s.match!!.players.contains(s.match.imposterName))
    }

    @Test
    fun `round reveal walks every player then enters describe`() {
        val v = vm()
        v.updatePlayerName("Ari"); v.addPlayer()
        v.updatePlayerName("Bo"); v.addPlayer()
        v.startMatch()
        val n = v.state.value.match!!.players.size
        repeat(n) { v.nextReveal() }
        assertEquals(Phase.DESCRIBE, v.state.value.phase)
        v.nextReveal() // no-op in describe phase
        assertEquals(Phase.DESCRIBE, v.state.value.phase)
    }

    @Test
    fun `describe turns walk players then enter voting`() {
        val v = vm()
        v.updatePlayerName("Ari"); v.addPlayer()
        v.updatePlayerName("Bo"); v.addPlayer()
        v.startMatch()
        val n = v.state.value.match!!.players.size
        repeat(n) { v.nextReveal() } // -> DESCRIBE
        repeat(n) { v.nextTurn() }   // -> VOTING
        assertEquals(Phase.VOTING, v.state.value.phase)
    }

    @Test
    fun `voting collects one vote per voter then finishes the round`() {
        val v = vm()
        v.updatePlayerName("Ari"); v.addPlayer()
        v.updatePlayerName("Bo"); v.addPlayer()
        v.startMatch()
        val players = v.state.value.match!!.players
        val n = players.size
        repeat(n) { v.nextReveal() }
        repeat(n) { v.nextTurn() }
        // Each voter votes for a different (non-self) target.
        for (i in players.indices) {
            val target = players[(i + 1) % n]
            v.castVote(target)
        }
        assertEquals(n, v.state.value.votes.size)
        v.finishRound()
        assertEquals(Phase.ROUND_RESULT, v.state.value.phase)
        assertNotNull(v.state.value.roundOutcome)
    }

    @Test
    fun `timer ticks down and clamps at zero`() {
        val v = vm()
        v.updatePlayerName("Ari"); v.addPlayer()
        v.updatePlayerName("Bo"); v.addPlayer()
        v.setTimerSeconds(3)
        v.startMatch()
        val n = v.state.value.match!!.players.size
        repeat(n) { v.nextReveal() }
        assertEquals(3, v.state.value.turnSecondsLeft)
        v.tickTimer()
        assertEquals(2, v.state.value.turnSecondsLeft)
        v.tickTimer()
        v.tickTimer()
        v.tickTimer()
        assertEquals(0, v.state.value.turnSecondsLeft)
        v.tickTimer() // stays at 0
        assertEquals(0, v.state.value.turnSecondsLeft)
    }

    @Test
    fun `untimed describe does not tick`() {
        val v = vm()
        v.updatePlayerName("Ari"); v.addPlayer()
        v.updatePlayerName("Bo"); v.addPlayer()
        v.setTimerSeconds(0)
        v.startMatch()
        val n = v.state.value.match!!.players.size
        repeat(n) { v.nextReveal() }
        v.tickTimer()
        assertEquals(0, v.state.value.turnSecondsLeft)
    }

    @Test
    fun `next round advances the round number and clears votes`() {
        val v = vm()
        v.updatePlayerName("Ari"); v.addPlayer()
        v.updatePlayerName("Bo"); v.addPlayer()
        v.setRounds(2)
        v.startMatch()
        val players = v.state.value.match!!.players
        val n = players.size
        repeat(n) { v.nextReveal() }
        repeat(n) { v.nextTurn() }
        for (i in players.indices) v.castVote(players[(i + 1) % n])
        v.finishRound()
        v.nextRound()
        assertEquals(2, v.state.value.roundNumber)
        assertEquals(Phase.WORD_REVEAL, v.state.value.phase)
        assertTrue(v.state.value.votes.isEmpty())
        assertNotNull(v.state.value.roundWords)
    }

    @Test
    fun `after the last round the match ends`() {
        val v = vm()
        v.updatePlayerName("Ari"); v.addPlayer()
        v.updatePlayerName("Bo"); v.addPlayer()
        v.setRounds(1)
        v.startMatch()
        val players = v.state.value.match!!.players
        val n = players.size
        repeat(n) { v.nextReveal() }
        repeat(n) { v.nextTurn() }
        for (i in players.indices) v.castVote(players[(i + 1) % n])
        v.finishRound()
        v.nextRound()
        assertEquals(Phase.GAME_OVER, v.state.value.phase)
        assertTrue(v.state.value.isLastRound)
    }

    @Test
    fun `category selection is stored in state`() {
        val v = vm()
        v.setCategory(WordCategory.ANIMALS)
        assertEquals(WordCategory.ANIMALS, v.state.value.category)
        v.setCategory(null)
        assertEquals(null, v.state.value.category)
    }

    @Test
    fun `rounds and timer are clamped to allowed ranges`() {
        val v = vm()
        v.setRounds(999)
        assertEquals(GameViewModel.MAX_ROUNDS, v.state.value.rounds)
        v.setRounds(-1)
        assertEquals(GameViewModel.MIN_ROUNDS, v.state.value.rounds)
        v.setTimerSeconds(9999)
        assertEquals(GameViewModel.MAX_TIMER_SECONDS, v.state.value.timerSeconds)
        v.setTimerSeconds(-5)
        assertEquals(0, v.state.value.timerSeconds)
    }
}
