package com.maskedparty.game.game

import kotlin.random.Random

/**
 * The one imposter for the whole match. Chosen once at match start and held
 * constant so their score accumulates across rounds — a deliberate difference
 * from the original game, which re-drew the imposter every round.
 */
class Match(
    val players: List<String>,
    val totalRounds: Int,
    private val random: Random = Random.Default
) {
    init {
        require(players.size >= 3) { "A match needs at least 3 players, got ${players.size}" }
        require(players.distinct().size == players.size) { "Player names must be unique" }
        require(totalRounds >= 1) { "A match needs at least 1 round" }
    }

    private val imposterIndex: Int = random.nextInt(players.size)
    val imposterName: String = players[imposterIndex]

    private val imposterScore = IntArray(players.size)
    private val teamScore = IntArray(players.size)

    val imposterScoreByName: Map<String, Int>
        get() = players.mapIndexed { i, name -> name to imposterScore[i] }.toMap()

    val teamScoreByName: Map<String, Int>
        get() = players.mapIndexed { i, name -> name to teamScore[i] }.toMap()

    fun isImposter(name: String): Boolean = name == imposterName

    /**
     * Resolves a round given the votes.
     *
     * @return a [RoundOutcome] describing who won and where the points went.
     */
    fun resolveRound(votes: Map<String, String>, round: com.maskedparty.game.data.RoundWords, roundNumber: Int): RoundOutcome {
        val tally = votes.values.groupingBy { it }.eachCount()
        val (ejectedName, count) = tally.entries.maxByOrNull { it.value }
            ?: error("At least one vote is required")

        val imposterCaught = ejectedName == imposterName
        if (imposterCaught) {
            teamScore.forEachIndexed { i, _ -> teamScore[i] += 1 }
        } else {
            imposterScore[imposterIndex] += 1
        }
        return RoundOutcome(
            roundNumber = roundNumber,
            category = round.category,
            sharedWord = round.sharedWord,
            imposterWord = round.imposterWord,
            imposterName = imposterName,
            imposterCaught = imposterCaught,
            voteTally = tally,
            ejected = ejectedName,
            ejections = count
        )
    }
}

/** Immutable description of a finished round, used for the results screen. */
data class RoundOutcome(
    val roundNumber: Int,
    val category: com.maskedparty.game.data.WordCategory,
    val sharedWord: String,
    val imposterWord: String,
    val imposterName: String,
    val imposterCaught: Boolean,
    val voteTally: Map<String, Int>,
    val ejected: String,
    val ejections: Int
)
