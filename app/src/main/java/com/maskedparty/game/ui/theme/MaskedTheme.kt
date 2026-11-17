package com.maskedparty.game.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val MaskBgTop = Color(0xFF2E1442)
val MaskBgBottom = Color(0xFF58226E)
val MaskSurface = Color(0xFF1C0C22)
val MaskSurfaceVariant = Color(0xFF2A1638)
val MaskGold = Color(0xFFF2C55C)
val MaskGoldDark = Color(0xFFC4963C)
val MaskCream = Color(0xFFFAF6EE)
val MaskDark = Color(0xFF180C22)
val MaskDanger = Color(0xFFE26D5C)
val MaskSuccess = Color(0xFF7FC8A9)
val MaskOnSurface = Color(0xFFF4EDEA)
val MaskOnSurfaceVariant = Color(0xFFCBBBD4)
val MaskPrimaryContainer = Color(0xFF5A3E14)
val MaskOnPrimaryContainer = Color(0xFFFBE6B0)
val MaskOutline = Color(0xFF8A7A99)
val MaskScrim = Color(0xB3180C22)

private val MaskColors = darkColorScheme(
    primary = MaskGold,
    onPrimary = MaskDark,
    primaryContainer = MaskPrimaryContainer,
    onPrimaryContainer = MaskOnPrimaryContainer,
    secondary = MaskOnSurfaceVariant,
    background = MaskSurface,
    surface = MaskSurface,
    surfaceVariant = MaskSurfaceVariant,
    onBackground = MaskOnSurface,
    onSurface = MaskOnSurface,
    onSurfaceVariant = MaskOnSurfaceVariant,
    outline = MaskOutline,
    error = MaskDanger
)

private val MaskShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp)
)

private val MaskTypography = Typography(
    displaySmall = TextStyle(fontWeight = FontWeight.Black, fontSize = 44.sp, letterSpacing = (-0.5).sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, letterSpacing = 0.5.sp)
)

@Composable
fun MaskedTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MaskColors,
        shapes = MaskShapes,
        typography = MaskTypography,
        content = content
    )
}
