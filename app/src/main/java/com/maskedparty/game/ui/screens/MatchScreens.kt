package com.maskedparty.game.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maskedparty.game.GameViewModel
import com.maskedparty.game.R
import com.maskedparty.game.game.Match
import com.maskedparty.game.ui.components.MaskLogo
import com.maskedparty.game.ui.theme.MaskSuccess

/**
 * Each player secretly looks at their own word, then passes the device.
 */
@Composable
fun WordRevealScreen(viewModel: GameViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val match = state.match ?: return
    val words = state.roundWords ?: return
    val player = state.currentRevealPlayer
    val isImposter = match.isImposter(player)
    val myWord = if (isImposter) words.imposterWord else words.sharedWord

    ScreenColumn {
        RoundBadge(state.roundNumber, match.totalRounds)
        Text(
            text = "Pass the device to…",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = player,
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 12.dp)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp)
            ) {
                MaskLogo(64)
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.your_word),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = myWord,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = stringResource(R.string.category_reveal, words.category.displayName),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
        Button(onClick = viewModel::nextReveal, modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
            Text(
                if (state.revealIndex == match.players.size - 1) "Everyone ready? Start describing"
                else "Next player",
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

/**
 * One speaking turn at a time; optional countdown.
 */
@Composable
fun DescribeScreen(viewModel: GameViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val match = state.match ?: return

    ScreenColumn {
        RoundBadge(state.roundNumber, match.totalRounds)
        Text(
            text = stringResource(R.string.describe_turn, state.currentPlayer),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (state.timerSeconds > 0) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 24.dp)
            ) {
                CircularProgressIndicator(
                    progress = {
                        (state.turnSecondsLeft.toFloat() / state.timerSeconds.toFloat())
                    },
                    modifier = Modifier.size(140.dp),
                    strokeWidth = 8.dp
                )
                Text(
                    text = state.turnSecondsLeft.toString(),
                    style = MaterialTheme.typography.displaySmall,
                    color = if (state.turnSecondsLeft <= 5) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary
                )
            }
        } else {
            Spacer(Modifier.height(32.dp))
            Text(
                text = stringResource(R.string.no_timer),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Button(onClick = viewModel::nextTurn, modifier = Modifier.fillMaxWidth().padding(top = 32.dp)) {
            Text(
                if (state.describeIndex == match.players.size - 1) "Go to voting"
                else "Next player",
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

/**
 * Secret voting: each player privately picks the suspected imposter.
 */
@Composable
fun VotingScreen(viewModel: GameViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val match = state.match ?: return

    ScreenColumn {
        RoundBadge(state.roundNumber, match.totalRounds)
        Text(
            text = stringResource(R.string.voting_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Voting: ${state.currentVoter} (keep it secret!)",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(Modifier.height(20.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            match.players.filter { it != state.currentVoter }.forEach { candidate ->
                val votesFor = state.votes.count { it.value == candidate }
                OutlinedButton(
                    onClick = { viewModel.castVote(candidate) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Text(candidate, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        // Progress: X of N voted
        Text(
            text = "${state.votes.size} of ${match.players.size} voted",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp)
        )
        Button(
            onClick = viewModel::finishRound,
            enabled = state.votes.size == match.players.size,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            Text(stringResource(R.string.results), style = MaterialTheme.typography.titleLarge)
        }
    }
}

/**
 * End-of-round reveal.
 */
@Composable
fun RoundResultScreen(viewModel: GameViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val match = state.match ?: return
    val outcome = state.roundOutcome ?: return

    ScreenColumn {
        RoundBadge(state.roundNumber, match.totalRounds)
        MaskLogo(80)
        if (outcome.imposterCaught) {
            Text(
                text = stringResource(R.string.imposter_revealed, outcome.imposterName),
                style = MaterialTheme.typography.headlineMedium,
                color = MaskSuccess,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = stringResource(R.string.team_wins),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        } else {
            Text(
                text = stringResource(R.string.imposter_escaped, outcome.imposterName),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = stringResource(R.string.imposter_wins),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Word reveal
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
                .padding(horizontal = 8.dp),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    stringResource(R.string.category_reveal, outcome.category.displayName),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Team word: ${outcome.sharedWord}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Imposter word: ${outcome.imposterWord}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Vote tally
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text(
                text = "Votes:",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        val sortedTally = outcome.voteTally.entries.sortedByDescending { it.value }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            sortedTally.forEach { (name, count) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(name, style = MaterialTheme.typography.bodyLarge)
                    Text("$count", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Button(
            onClick = viewModel::nextRound,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp)
        ) {
            Text(
                if (state.isLastRound) stringResource(R.string.final_scores)
                else stringResource(R.string.next_round),
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

/**
 * Final scoreboard.
 */
@Composable
fun GameOverScreen(viewModel: GameViewModel, repository: com.maskedparty.game.data.GameRepository) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val match = state.match ?: return

    // Persist best totals once when the game over screen is shown.
    LaunchedEffect(match) {
        val totals = match.players.associateWith { name ->
            (match.imposterScoreByName[name] ?: 0) + (match.teamScoreByName[name] ?: 0)
        }
        repository.recordRoundTotals(totals)
        repository.saveRecentPlayers(match.players)
    }

    ScreenColumn {
        BrandHeader()
        Text(
            text = stringResource(R.string.final_scores),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Compute totals: imposter points + team points, ranked highest first.
        val totals: List<Pair<String, Int>> = match.players
            .map { name -> name to ((match.imposterScoreByName[name] ?: 0) + (match.teamScoreByName[name] ?: 0)) }
            .sortedByDescending { it.second }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            totals.forEachIndexed { index, pair ->
                val name = pair.first
                val score = pair.second
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = if (index == 0) "👑 $name" else "$name",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (match.isImposter(name)) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "${score} ${stringResource(R.string.points)}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Button(
            onClick = viewModel::goHome,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp)
        ) {
            Text(stringResource(R.string.play_again), style = MaterialTheme.typography.titleLarge)
        }
        OutlinedButton(onClick = { }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Text(stringResource(R.string.back_to_menu))
        }
    }
}
