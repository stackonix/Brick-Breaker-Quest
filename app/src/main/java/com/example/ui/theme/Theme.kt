package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.domain.model.ThemeMode

/**
 * Extended semantic color tokens tailored for Mindfeud's tactile logic-game interface
 * (accuracy mint, soft coral feedback, streak gold, speed cyan, rank violet, and 3D arcade ledges).
 */
@Immutable
data class MindfeudPalette(
    val isDark: Boolean,
    val cyan: Color,
    val cyanText: Color,
    val cyanContainer: Color,
    val violet: Color,
    val violetText: Color,
    val violetContainer: Color,
    val mint: Color,
    val mintText: Color,
    val mintContainer: Color,
    val coral: Color,
    val coralText: Color,
    val coralContainer: Color,
    val gold: Color,
    val goldText: Color,
    val goldContainer: Color,
    val cardSurface: Color,
    val elevatedSurface: Color,
    val borderSubtle: Color,
    val cardLip: Color,
    val primaryActionBg: Color,
    val primaryActionTop: Color,
    val primaryActionLip: Color,
    val primaryActionFg: Color
)

val WhiteGamePalette = MindfeudPalette(
    isDark = false,
    cyan = ArcadeCyan,
    cyanText = ArcadeCyanDeep,
    cyanContainer = LightCyanContainer,
    violet = RoyalViolet,
    violetText = RoyalViolet,
    violetContainer = LightVioletContainer,
    mint = ArcadeMint,
    mintText = ArcadeMintDeep,
    mintContainer = LightMintContainer,
    coral = ArcadeCoral,
    coralText = ArcadeCoralDeep,
    coralContainer = LightCoralContainer,
    gold = ArcadeGold,
    goldText = ArcadeGoldDeep,
    goldContainer = LightGoldContainer,
    cardSurface = WhiteCardSurface,
    elevatedSurface = WhiteElevatedSurface,
    borderSubtle = WhiteCardBorder,
    cardLip = WhiteCardLip,
    primaryActionBg = SunsetOrangeStart,
    primaryActionTop = SunsetOrangeEnd,
    primaryActionLip = SunsetOrangeLip,
    primaryActionFg = Color.White
)

private val DarkMindfeudPalette = MindfeudPalette(
    isDark = true,
    cyan = ElectricCyan,
    cyanText = ElectricCyan,
    cyanContainer = ElectricCyanContainer,
    violet = NeonViolet,
    violetText = NeonViolet,
    violetContainer = NeonVioletContainer,
    mint = MintGreen,
    mintText = MintGreen,
    mintContainer = MintContainerDark,
    coral = SoftCoral,
    coralText = SoftCoral,
    coralContainer = CoralContainerDark,
    gold = StreakGold,
    goldText = StreakGold,
    goldContainer = GoldContainerDark,
    cardSurface = DeepNavyCard,
    elevatedSurface = DeepNavyElevated,
    borderSubtle = DeepNavyOutline,
    cardLip = DeepNavyLip,
    primaryActionBg = ElectricCyan,
    primaryActionTop = Color(0xFF4BE4FA),
    primaryActionLip = ElectricCyanLip,
    primaryActionFg = MidnightBg
)

val LocalMindfeudColors = staticCompositionLocalOf { WhiteGamePalette }

/**
 * Clean, high-contrast Material 3 Light ColorScheme designed for a crisp white logic game.
 */
val MindfeudLightColorScheme = lightColorScheme(
    primary = RoyalViolet,
    onPrimary = Color.White,
    primaryContainer = LightVioletContainer,
    onPrimaryContainer = InkPrimaryText,
    inversePrimary = NeonViolet,

    secondary = ArcadeCyanDeep,
    onSecondary = Color.White,
    secondaryContainer = LightCyanContainer,
    onSecondaryContainer = InkPrimaryText,

    tertiary = ArcadeGoldDeep,
    onTertiary = Color.White,
    tertiaryContainer = LightGoldContainer,
    onTertiaryContainer = InkPrimaryText,

    background = WhiteCanvasTop,
    onBackground = InkPrimaryText,

    surface = WhiteCardSurface,
    onSurface = InkPrimaryText,
    surfaceVariant = WhiteElevatedSurface,
    onSurfaceVariant = InkSecondaryText,
    surfaceTint = RoyalViolet,

    surfaceBright = WhiteCardSurface,
    surfaceDim = WhiteCanvasBottom,
    surfaceContainerLowest = WhiteCardSurface,
    surfaceContainerLow = WhiteCanvasMid,
    surfaceContainer = WhiteElevatedSurface,
    surfaceContainerHigh = Color(0xFFE7ECFF),
    surfaceContainerHighest = Color(0xFFDFE5FC),

    inverseSurface = InkPrimaryText,
    inverseOnSurface = WhiteCardSurface,

    outline = WhiteCardBorder,
    outlineVariant = Color(0xFFE3E8FA),
    scrim = Color(0x6614172F),

    error = ArcadeCoralDeep,
    onError = Color.White,
    errorContainer = LightCoralContainer,
    onErrorContainer = ArcadeCoralDeep
)

private val MindfeudDarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = MidnightBg,
    primaryContainer = ElectricCyanContainer,
    onPrimaryContainer = GhostWhiteText,
    secondary = NeonViolet,
    onSecondary = MidnightBg,
    secondaryContainer = NeonVioletContainer,
    onSecondaryContainer = GhostWhiteText,
    tertiary = StreakGold,
    onTertiary = MidnightBg,
    tertiaryContainer = GoldContainerDark,
    onTertiaryContainer = GhostWhiteText,
    background = MidnightBg,
    onBackground = GhostWhiteText,
    surface = DeepNavyCard,
    onSurface = GhostWhiteText,
    surfaceVariant = DeepNavyElevated,
    onSurfaceVariant = SoftLavenderText,
    outline = DeepNavyOutline,
    outlineVariant = Color(0xFF282E54),
    error = SoftCoral,
    onError = MidnightBg,
    errorContainer = CoralContainerDark,
    onErrorContainer = SoftCoral
)

val MindfeudShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

object MindfeudThemeDefaults {
    val colors: MindfeudPalette
        @Composable
        @ReadOnlyComposable
        get() = LocalMindfeudColors.current
}

/**
 * Mindfeud Material 3 Design System theme. Defaults to the crisp White Logic Game theme.
 */
@Composable
fun MindfeudTheme(
    themeMode: ThemeMode = ThemeMode.LIGHT,
    content: @Composable () -> Unit
) {
    val useDark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val colorScheme = if (useDark) MindfeudDarkColorScheme else MindfeudLightColorScheme
    val extendedColors = if (useDark) DarkMindfeudPalette else WhiteGamePalette

    CompositionLocalProvider(LocalMindfeudColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MindfeudTypography,
            shapes = MindfeudShapes,
            content = content
        )
    }
}
