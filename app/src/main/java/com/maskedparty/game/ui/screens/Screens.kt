package com.maskedparty.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maskedparty.game.GameViewModel
import com.maskedparty.game.Phase
import com.maskedparty.game.R
import com.maskedparty.game.data.GameRepository
import com.maskedparty.game.ui.components.MaskLogo

/**
 * Single entry composable. Routes to the screen for the current phase and owns
 * the view model so state survives screen transitions.
 */
@Composable
fun RootScreen(
    repository: GameRepository,
    viewModel: GameViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Optional per-turn timer: one tick per second while a turn is active.
    LaunchedEffect(state.phase, state.timerSeconds) {
        if (state.phase == Phase.DESCRIBE && state.timerSeconds > 0) {
            while (true) {
                kotlinx.coroutines.delay(1000)
                viewModel.tickTimer()
                if (viewModel.state.value.turnSecondsLeft <= 0) break
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF241031), Color(0xFF180C22))
                )
            )
    ) {
        when (state.phase) {
            Phase.SETUP -> SetupScreen(viewModel)
            Phase.WORD_REVEAL -> WordRevealScreen(viewModel)
            Phase.DESCRIBE -> DescribeScreen(viewModel)
            Phase.VOTING -> VotingScreen(viewModel)
            Phase.ROUND_RESULT -> RoundResultScreen(viewModel)
            Phase.GAME_OVER -> GameOverScreen(viewModel, repository)
        }
    }
}

/** Shared centered column with top/bottom padding. */
@Composable
fun ScreenColumn(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        content()
    }
}

/** Brand header shown on most screens. */
@Composable
fun BrandHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        MaskLogo(size = 72)
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = stringResource(R.string.tagline),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Round indicator: "Round 2 of 5". */
@Composable
fun RoundBadge(round: Int, total: Int, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.round_of, round, total),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(top = 8.dp)
    )
}
