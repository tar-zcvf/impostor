package com.maskedparty.game.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first

/**
 * Small offline persistence layer backed by Jetpack DataStore.
 *
 * Stores the most recently used player list and per-player best total scores.
 * Deliberately minimal: no accounts, no network, no analytics.
 */
class GameRepository(private val context: Context) {

    private val dataStore: DataStore<Preferences> = context.dataStore

    fun recentPlayers(): Flow<List<String>> =
        dataStore.data
            .map { it[playersKey] }
            .map { parseList(it) }

    suspend fun saveRecentPlayers(players: List<String>) {
        dataStore.edit { it[playersKey] = encodeList(players) }
    }

    fun bestScores(): Flow<Map<String, Int>> =
        dataStore.data
            .map { it[bestScoresKey] }
            .map { parseScores(it) }

    suspend fun bestScoresOnce(): Map<String, Int> = bestScores().first()

    /** Records a new best total score per player if it improves on the stored value. */
    suspend fun recordRoundTotals(totals: Map<String, Int>) {
        val current = parseScores(dataStore.data.first()[bestScoresKey])
        val merged = current.toMutableMap()
        totals.forEach { (name, total) ->
            val safe = sanitize(name)
            if (total > (merged[safe] ?: 0)) merged[safe] = total
        }
        dataStore.edit { it[bestScoresKey] = encodeScores(merged) }
    }

    companion object {
        private val playersKey = stringPreferencesKey("last_players")
        private val bestScoresKey = stringPreferencesKey("best_scores")

        private const val SEP = "¦"

        internal fun sanitize(name: String): String =
            name.trim().replace(SEP, " ").ifEmpty { "Player" }

        internal fun encodeList(names: List<String>): String =
            names.map(::sanitize).joinToString(SEP)

        internal fun parseList(raw: String?): List<String> =
            raw?.split(SEP)
                ?.map { it.trim() }
                ?.filter { it.isNotEmpty() }
                ?: emptyList()

        internal fun encodeScores(scores: Map<String, Int>): String =
            scores.entries.joinToString(SEP) { "${it.key}=${it.value}" }

        internal fun parseScores(raw: String?): Map<String, Int> =
            raw?.split(SEP)
                ?.mapNotNull { entry ->
                    val idx = entry.lastIndexOf('=')
                    if (idx <= 0) return@mapNotNull null
                    val name = entry.substring(0, idx).trim()
                    val value = entry.substring(idx + 1).toIntOrNull() ?: return@mapNotNull null
                    if (name.isEmpty()) null else name to value
                }
                ?.toMap()
                ?: emptyMap()
    }
}

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "masked_prefs")
