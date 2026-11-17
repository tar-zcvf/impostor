package com.maskedparty.game.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maskedparty.game.GameViewModel
import com.maskedparty.game.R
import com.maskedparty.game.data.WordCategory
import com.maskedparty.game.ui.components.MaskLogo

/**
 * Setup screen: player list, category picker, rounds, timer, how-to-play.
 */
@Composable
fun SetupScreen(viewModel: GameViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ScreenColumn {
        BrandHeader()

        Text(
            text = stringResource(R.string.players_count, state.players.size),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Player rows.
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            state.players.forEachIndexed { index, name ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(name, style = MaterialTheme.typography.bodyLarge)
                    if (state.players.size > 1) {
                        TextButton(onClick = { viewModel.removePlayerAt(index) }) {
                            Text(stringResource(R.string.remove_player))
                        }
                    }
                }
            }
        }

        // Add player.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = state.playerName,
                onValueChange = viewModel::updatePlayerName,
                label = { Text(stringResource(R.string.name_hint)) },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Button(onClick = { viewModel.addPlayer() }) {
                Text(stringResource(R.string.add))
            }
        }
        if (state.nameError != null) {
            Text(
                text = state.nameError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        // Category chips.
        Text(
            text = stringResource(R.string.category_label),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            CategoryChip(state.category == null, stringResource(R.string.category_all)) {
                viewModel.setCategory(null)
            }
            WordCategory.all().forEach { category ->
                CategoryChip(state.category == category, category.displayName) {
                    viewModel.setCategory(category)
                }
            }
        }

        // Rounds.
        SettingRow(
            label = stringResource(R.string.rounds_label),
            value = state.rounds.toString()
        ) { delta -> viewModel.setRounds(state.rounds + delta) }

        // Timer.
        SettingRow(
            label = stringResource(R.string.timer_label),
            value = if (state.timerSeconds == 0)
                stringResource(R.string.no_timer)
            else "${state.timerSeconds} ${stringResource(R.string.seconds)}"
        ) { delta ->
            viewModel.setTimerSeconds(
                (state.timerSeconds + delta * 5).coerceIn(0, GameViewModel.MAX_TIMER_SECONDS)
            )
        }

        // How to play.
        FilledTonalButton(onClick = { }, modifier = Modifier.padding(top = 16.dp)) {
            Text(stringResource(R.string.how_to_play))
        }

        Button(
            onClick = viewModel::startMatch,
            enabled = state.players.size >= GameViewModel.MIN_PLAYERS,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            Text(stringResource(R.string.start_game), style = MaterialTheme.typography.titleLarge)
        }

        Text(
            text = stringResource(R.string.privacy_note),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
fun CategoryChip(selected: Boolean, label: String, onClick: () -> Unit) {
    val color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        color = color,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.small,
        onClick = onClick,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp))
    }
}

@Composable
fun SettingRow(label: String, value: String, onDelta: (Int) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { onDelta(-1) }) { Text("−") }
            Text(value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            TextButton(onClick = { onDelta(1) }) { Text("+") }
        }
    }
}
