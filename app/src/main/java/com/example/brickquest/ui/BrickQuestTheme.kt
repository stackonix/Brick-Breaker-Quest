package com.example.brickquest.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MindfeudTypography

/**
 * Centralized neon cosmic arcade palette matching Bricks Breaker Quest.
 */
object BrickQuestColors {
    val CosmicTop = Color(0xFF0A1D2C)
    val CosmicMid = Color(0xFF060B18)
    val CosmicBottom = Color(0xFF190A24)

    val CardSurface = Color(0xFF0E172A)
    val CardSurfaceElevated = Color(0xFF15223D)
    val CardBorder = Color(0xFF28426B)

    val NeonCyan = Color(0xFF38BDF8)
    val NeonCyanBright = Color(0xFF7DD3FC)
    val NeonMagenta = Color(0xFFE879F9)
    val NeonViolet = Color(0xFFA855F7)
    val NeonGreen = Color(0xFF4ADE80)
    val NeonGold = Color(0xFFFACC15)
    val NeonOrange = Color(0xFFFF7A1A)
    val CrimsonGlow = Color(0xFFE11D48)
    val CrimsonDeep = Color(0xFF881337)
    val CrimsonBorder = Color(0xFFFF4D6D)

    val TextPrimary = Color(0xFFF8FAFC)
    val TextSecondary = Color(0xFF94A3B8)
    val TextMuted = Color(0xFF64748B)

    val CosmicBackdropBrush: Brush = Brush.verticalGradient(
        colors = listOf(CosmicTop, CosmicMid, CosmicMid, CosmicBottom)
    )

    val ArenaBorderBrush: Brush = Brush.verticalGradient(
        colors = listOf(NeonCyan, NeonViolet, NeonMagenta)
    )

    val PlayButtonBrush: Brush = Brush.horizontalGradient(
        colors = listOf(Color(0xFF06B6D4), Color(0xFF3B82F6), Color(0xFF9333EA))
    )

    val EndlessButtonBrush: Brush = Brush.horizontalGradient(
        colors = listOf(Color(0xFFF97316), Color(0xFFEC4899))
    )
}

private val BrickQuestColorScheme = darkColorScheme(
    primary = BrickQuestColors.NeonCyan,
    onPrimary = Color(0xFF04111F),
    primaryContainer = Color(0xFF102A43),
    onPrimaryContainer = BrickQuestColors.NeonCyanBright,
    secondary = BrickQuestColors.NeonMagenta,
    onSecondary = Color(0xFF1D0424),
    secondaryContainer = Color(0xFF2D1142),
    onSecondaryContainer = Color(0xFFF5D0FE),
    tertiary = BrickQuestColors.NeonGold,
    onTertiary = Color(0xFF1E1302),
    tertiaryContainer = Color(0xFF3B2806),
    onTertiaryContainer = Color(0xFFFEF08A),
    background = BrickQuestColors.CosmicMid,
    onBackground = BrickQuestColors.TextPrimary,
    surface = BrickQuestColors.CardSurface,
    onSurface = BrickQuestColors.TextPrimary,
    surfaceVariant = BrickQuestColors.CardSurfaceElevated,
    onSurfaceVariant = BrickQuestColors.TextSecondary,
    outline = BrickQuestColors.CardBorder,
    error = BrickQuestColors.CrimsonBorder,
    onError = Color.White
)

private val BrickQuestShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

@Composable
fun BrickQuestTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BrickQuestColorScheme,
        typography = MindfeudTypography,
        shapes = BrickQuestShapes,
        content = content
    )
}
