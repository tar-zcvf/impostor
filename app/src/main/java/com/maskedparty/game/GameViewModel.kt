package com.maskedparty.game

import androidx.lifecycle.ViewModel
import com.maskedparty.game.data.RoundWords
import com.maskedparty.game.data.WordBank
import com.maskedparty.game.data.WordCategory

import com.maskedparty.game.game.Match
import com.maskedparty.game.game.RoundOutcome
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

/**
 * Drives the whole pass-and-play match:
 *
 *   SETUP -> WORD_REVEAL (each player secretly sees their own word)
 *         -> DESCRIBE (one speaking turn at a time, optional timer)
 *         -> VOTING (each player secretly votes for the suspected imposter)
 *         -> ROUND_RESULT -> next round -> GAME_OVER
 *
 * All game state lives in [UiState] so every screen is a pure function of it.
 */
class GameViewModel : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    // ---------------------------------------------------------------- setup --

    fun updatePlayerName(name: String) {
        _state.update { it.copy(playerName = name, nameError = null) }
    }

    fun addPlayer(): Boolean {
        val s = _state.value
        val name = s.playerName.trim()
        if (name.isEmpty()) {
            _state.update { it.copy(nameError = "Enter a name first") }
            return false
        }
        if (s.players.size >= MAX_PLAYERS) {
            _state.update { it.copy(nameError = "Up to $MAX_PLAYERS players") }
            return false
        }
        if (s.players.any { it.equals(name, ignoreCase = true) }) {
            _state.update { it.copy(nameError = "That name is already in use") }
            return false
        }
        _state.update { it.copy(players = it.players + name, playerName = "", nameError = null) }
        return true
    }

    fun removePlayerAt(index: Int) {
        _state.update {
            if (index in it.players.indices)
                it.copy(players = it.players.filterIndexed { i, _ -> i != index })
            else it
        }
    }

    fun setCategory(category: WordCategory?) {
        _state.update { it.copy(category = category) }
    }

    fun setRounds(rounds: Int) {
        _state.update { it.copy(rounds = rounds.coerceIn(MIN_ROUNDS, MAX_ROUNDS)) }
    }

    fun setTimerSeconds(seconds: Int) {
        _state.update { it.copy(timerSeconds = seconds.coerceIn(0, MAX_TIMER_SECONDS)) }
    }

    /** Validates input and starts the match, or flags an error. */
    fun startMatch() {
        val current = _state.value
        if (current.players.size < MIN_PLAYERS) {
            _state.update { it.copy(nameError = "Add at least $MIN_PLAYERS players") }
            return
        }
        val match = try {
            Match(players = current.players, totalRounds = current.rounds, random = Random(System.nanoTime()))
        } catch (e: IllegalArgumentException) {
            _state.update { it.copy(nameError = e.message ?: "Invalid setup") }
            return
        }
        val words = WordBank.drawRound(current.category)
        if (words == null) {
            _state.update { it.copy(nameError = "No words available for that category") }
            return
        }
        _state.update {
            it.copy(
                match = match,
                roundWords = words,
                roundNumber = 1,
                phase = Phase.WORD_REVEAL,
                revealIndex = 0,
                describeIndex = 0,
                votes = emptyMap(),
                votingIndex = 0,
                turnSecondsLeft = it.timerSeconds,
                timeUp = false,
                nameError = null
            )
        }
    }

    // ------------------------------------------------------------ word reveal --

    /** Move to the next player's secret word. */
    fun nextReveal() {
        _state.update { s ->
            val match = s.match ?: return@update s
            val next = s.revealIndex + 1
            if (next >= match.players.size) {
                s.copy(phase = Phase.DESCRIBE, describeIndex = 0, turnSecondsLeft = s.timerSeconds)
            } else {
                s.copy(revealIndex = next)
            }
        }
    }

    // ---------------------------------------------------------------- describe --

    fun nextTurn() {
        _state.update { s ->
            if (s.phase != Phase.DESCRIBE) return@update s
            val match = s.match ?: return@update s
            val next = s.describeIndex + 1
            if (next >= match.players.size) {
                s.copy(phase = Phase.VOTING, votingIndex = 0)
            } else {
                s.copy(describeIndex = next, turnSecondsLeft = s.timerSeconds)
            }
        }
    }

    /** Ticks the turn timer; auto-advances the turn when it hits zero. */
    fun tickTimer() {
        _state.update { s ->
            if (s.phase != Phase.DESCRIBE || s.timerSeconds <= 0) return@update s
            val remaining = s.turnSecondsLeft - 1
            if (remaining <= 0) s.copy(turnSecondsLeft = 0)
            else s.copy(turnSecondsLeft = remaining)
        }
    }

    // ------------------------------------------------------------------ voting --

    fun castVote(target: String) {
        _state.update { s ->
            if (s.phase != Phase.VOTING) return@update s
            val match = s.match ?: return@update s
            val voter = match.players[s.votingIndex]
            val votes = s.votes + (voter to target)
            if (votes.size == match.players.size) {
                s.copy(votes = votes)
            } else {
                s.copy(votes = votes, votingIndex = s.votingIndex + 1)
            }
        }
    }

    fun finishRound() {
        val s = _state.value
        val match = s.match ?: return
        if (s.phase != Phase.VOTING || s.votes.size != match.players.size) return
        val words = s.roundWords ?: return
        val outcome = match.resolveRound(s.votes, words, s.roundNumber)
        _state.update {
            it.copy(phase = Phase.ROUND_RESULT, roundOutcome = outcome)
        }
    }

    // --------------------------------------------------------------- next round --

    fun nextRound() {
        val s = _state.value
        val match = s.match ?: return
        if (s.roundNumber >= match.totalRounds) {
            _state.update { it.copy(phase = Phase.GAME_OVER) }
            return
        }
        val words = WordBank.drawRound(s.category) ?: return
        _state.update {
            it.copy(
                phase = Phase.WORD_REVEAL,
                roundWords = words,
                roundNumber = it.roundNumber + 1,
                revealIndex = 0,
                describeIndex = 0,
                votes = emptyMap(),
                votingIndex = 0,
                turnSecondsLeft = it.timerSeconds,
                timeUp = false,
                roundOutcome = null
            )
        }
    }

    fun endGame() {
        _state.update { it.copy(phase = Phase.GAME_OVER) }
    }

    fun goHome() {
        _state.update { UiState() }
    }

    companion object {
        const val MIN_PLAYERS = 3
        const val MAX_PLAYERS = 8
        const val MIN_ROUNDS = 1
        const val MAX_ROUNDS = 10
        const val MAX_TIMER_SECONDS = 120
    }
}

/** Top-level navigation phase. */
enum class Phase { SETUP, WORD_REVEAL, DESCRIBE, VOTING, ROUND_RESULT, GAME_OVER }

/**
 * Complete, immutable UI state. Every screen reads this and renders.
 */
data class UiState(
    // setup
    val playerName: String = "",
    val players: List<String> = listOf("You"),
    val nameError: String? = null,
    val category: WordCategory? = null,
    val rounds: Int = 3,
    val timerSeconds: Int = 20,

    // match
    val match: Match? = null,
    val roundWords: RoundWords? = null,
    val roundNumber: Int = 0,
    val phase: Phase = Phase.SETUP,

    // word reveal
    val revealIndex: Int = 0,

    // describe
    val describeIndex: Int = 0,
    val turnSecondsLeft: Int = 0,
    val timeUp: Boolean = false,

    // voting
    val votes: Map<String, String> = emptyMap(),
    val votingIndex: Int = 0,
    val roundOutcome: RoundOutcome? = null
) {
    val currentPlayer: String get() = match?.players?.getOrNull(describeIndex) ?: ""
    val currentVoter: String get() = match?.players?.getOrNull(votingIndex) ?: ""
    val currentRevealPlayer: String get() = match?.players?.getOrNull(revealIndex) ?: ""
    val isLastRound: Boolean get() = roundNumber >= (match?.totalRounds ?: 0)
}
