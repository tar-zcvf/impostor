package com.maskedparty.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * The "masked eye" logo drawn with a vertical gradient plate + a gold eye ring.
 * Used in the header, splash, and game-over screens.
 */
@Composable
fun MaskLogo(
    size: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF2E1442), Color(0xFF58226E))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Eye: a gold ring with a dark pupil.
        Box(
            modifier = Modifier
                .size((size * 0.52).dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .padding((size * 0.16).dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size((size * 0.22).dp)
                    .clip(CircleShape)
                    .background(Color(0xFF180C22))
            )
        }
    }
}

/**
 * A compact score chip, e.g. "You · 2 pts".
 */
@Composable
fun ScoreChip(
    name: String,
    score: Int,
    modifier: Modifier = Modifier,
    highlight: Boolean = false
) {
    Surface(
        modifier = modifier,
        color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (highlight) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(name, style = MaterialTheme.typography.labelLarge)
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
            Text("$score", style = MaterialTheme.typography.labelLarge)
        }
    }
}

/**
 * Helper for RowScope: space-between layout with a trailing score.
 */
@Composable
fun RowScope.ScoreTrailing(name: String, score: Int, highlight: Boolean = false) {
    Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(name, style = MaterialTheme.typography.bodyLarge)
        ScoreChip(name, score, highlight = highlight)
    }
}
