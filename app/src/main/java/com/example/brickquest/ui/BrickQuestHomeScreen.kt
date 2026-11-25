package com.example.brickquest.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brickquest.data.BallSkin
import com.example.brickquest.data.BrickQuestProgress
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val NEON_PEARL_OUTER = Color(0x45A855F7)
private val NEON_PEARL_MID = Color(0x88E879F9)
private val NEON_PEARL_RING = Color(0xFFF5D0FE)

private val STARLIGHT_OUTER = Color(0x400284C7)
private val STARLIGHT_MID = Color(0x7838BDF8)
private val STARLIGHT_FILL = Color(0xFF7DD3FC)
private val STARLIGHT_STROKE = Color(0xFF38BDF8)

private val SOLAR_OUTER = Color(0x45EF4444)
private val SOLAR_MID = Color(0x88FF7A1A)
private val SOLAR_RING = Color(0xFFFACC15)
private val SOLAR_CORE = Color(0xFFFFFBEB)

private val MINT_OUTER = Color(0x4006B6D4)
private val MINT_MID = Color(0x8534D399)
private val MINT_RING = Color(0xFF6EE7B7)

/**
 * Ultra-fast ZERO-ALLOCATION Canvas renderer for all 4 ball skins
 * (replaces expensive per-ball RadialGradient shader allocations with concentric neon halos).
 */
fun DrawScope.drawBallSkinGraphic(
    skin: BallSkin,
    center: Offset,
    radius: Float,
    rotationRad: Float = 0f,
    reusableStarPath: Path? = null
) {
    when (skin) {
        BallSkin.NEON_PEARL -> {
            drawCircle(color = NEON_PEARL_OUTER, radius = radius * 1.95f, center = center)
            drawCircle(color = NEON_PEARL_MID, radius = radius * 1.38f, center = center)
            drawCircle(color = NEON_PEARL_RING, radius = radius * 1.02f, center = center)
            drawCircle(color = Color.White, radius = radius * 0.72f, center = center)
        }

        BallSkin.STARLIGHT -> {
            drawCircle(color = STARLIGHT_OUTER, radius = radius * 2.0f, center = center)
            drawCircle(color = STARLIGHT_MID, radius = radius * 1.35f, center = center)
            val path = reusableStarPath ?: Path()
            fillStarPath(path, center, outerR = radius * 1.30f, innerR = radius * 0.58f, rotationRad = rotationRad)
            drawPath(path = path, color = STARLIGHT_FILL)
            drawPath(path = path, color = STARLIGHT_STROKE, style = Stroke(width = radius * 0.22f))
            drawCircle(color = Color.White, radius = radius * 0.54f, center = center)
        }

        BallSkin.SOLAR_FIRE -> {
            drawCircle(color = SOLAR_OUTER, radius = radius * 1.95f, center = center)
            drawCircle(color = SOLAR_MID, radius = radius * 1.38f, center = center)
            drawCircle(color = SOLAR_RING, radius = radius * 1.02f, center = center)
            drawCircle(color = SOLAR_CORE, radius = radius * 0.66f, center = center)
        }

        BallSkin.CYBER_MINT -> {
            drawCircle(color = MINT_OUTER, radius = radius * 1.95f, center = center)
            drawCircle(color = MINT_MID, radius = radius * 1.38f, center = center)
            drawCircle(color = MINT_RING, radius = radius * 1.02f, center = center)
            drawCircle(color = Color.White, radius = radius * 0.68f, center = center)
        }
    }
}

private fun fillStarPath(
    path: Path,
    center: Offset,
    outerR: Float,
    innerR: Float,
    rotationRad: Float
) {
    path.reset()
    val step = (PI / 5.0).toFloat()
    val start = rotationRad - (PI / 2.0).toFloat()
    for (i in 0 until 10) {
        val r = if (i % 2 == 0) outerR else innerR
        val angle = start + i * step
        val x = center.x + cos(angle) * r
        val y = center.y + sin(angle) * r
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
}

@Composable
fun BrickQuestSplashScreen() {
    val infinite = rememberInfiniteTransition(label = "splash")
    val pulse by infinite.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrickQuestColors.CosmicBackdropBrush)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Neon Emblem
            Box(
                modifier = Modifier.size(116.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    drawRoundRect(
                        brush = BrickQuestColors.ArenaBorderBrush,
                        topLeft = Offset(8f, 8f),
                        size = Size(w - 16f, h - 16f),
                        cornerRadius = CornerRadius(28f, 28f),
                        style = Stroke(width = 7f)
                    )
                    // 3 Mini neon bricks
                    val colors = listOf(Color(0xFFEF4444), Color(0xFF22D3EE), Color(0xFFFACC15))
                    colors.forEachIndexed { idx, col ->
                        val bx = w * (0.20f + idx * 0.22f)
                        val by = h * 0.26f
                        val bs = w * 0.18f
                        drawRoundRect(
                            color = col.copy(alpha = 0.35f),
                            topLeft = Offset(bx, by),
                            size = Size(bs, bs),
                            cornerRadius = CornerRadius(8f, 8f)
                        )
                        drawRoundRect(
                            color = col,
                            topLeft = Offset(bx, by),
                            size = Size(bs, bs),
                            cornerRadius = CornerRadius(8f, 8f),
                            style = Stroke(width = 3.5f)
                        )
                    }
                    // Neon green laser streak
                    drawLine(
                        color = BrickQuestColors.NeonGreen.copy(alpha = 0.45f),
                        start = Offset(w * 0.16f, h * 0.54f),
                        end = Offset(w * 0.84f, h * 0.54f),
                        strokeWidth = 10f * pulse,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Color.White,
                        start = Offset(w * 0.16f, h * 0.54f),
                        end = Offset(w * 0.84f, h * 0.54f),
                        strokeWidth = 3f,
                        cap = StrokeCap.Round
                    )
                    // Glowing ball stream
                    drawBallSkinGraphic(
                        skin = BallSkin.NEON_PEARL,
                        center = Offset(w * 0.38f, h * 0.74f),
                        radius = w * 0.065f
                    )
                    drawBallSkinGraphic(
                        skin = BallSkin.STARLIGHT,
                        center = Offset(w * 0.62f, h * 0.68f),
                        radius = w * 0.085f
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "BRICK BREAKER",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.5.sp
                    ),
                    color = BrickQuestColors.TextPrimary
                )
                Text(
                    text = "QUEST",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 5.sp
                    ),
                    color = BrickQuestColors.NeonCyan
                )
            }
        }
    }
}

@Composable
fun BrickQuestHomeScreen(
    progress: BrickQuestProgress,
    onStartGame: () -> Unit,
    onPlayCurrentStage: () -> Unit,
    onStartEndless: () -> Unit,
    onSelectSkin: (BallSkin) -> Unit,
    onToggleSound: () -> Unit,
    onToggleHaptics: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrickQuestColors.CosmicBackdropBrush)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Utility HUD Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stars & Endless Best Pills
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = BrickQuestColors.CardSurfaceElevated,
                        border = BorderStroke(1.2.dp, BrickQuestColors.NeonGold.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Total Stars",
                                tint = BrickQuestColors.NeonGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "${progress.totalStars}/${progress.maxStars}",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = BrickQuestColors.TextPrimary
                            )
                        }
                    }

                    if (progress.endlessBestScore > 0) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = BrickQuestColors.CardSurfaceElevated,
                            border = BorderStroke(1.2.dp, BrickQuestColors.NeonCyan.copy(alpha = 0.55f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.EmojiEvents,
                                    contentDescription = "Best Score",
                                    tint = BrickQuestColors.NeonCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "${progress.endlessBestScore}",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = BrickQuestColors.TextPrimary
                                )
                            }
                        }
                    }
                }

                // Sound & Haptics Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    NeonCircleIconToggle(
                        active = progress.soundEnabled,
                        icon = if (progress.soundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                        contentDescription = "Toggle Sound",
                        tag = "home_sound_toggle",
                        onClick = onToggleSound
                    )
                    NeonCircleIconToggle(
                        active = progress.hapticsEnabled,
                        icon = Icons.Filled.Vibration,
                        contentDescription = "Toggle Haptics",
                        tag = "home_haptics_toggle",
                        onClick = onToggleHaptics
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Game Logo Header
            Text(
                text = "BRICK BREAKER",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                ),
                color = BrickQuestColors.TextPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = "QUEST",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 6.sp
                ),
                color = BrickQuestColors.NeonCyan,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Live Animated Neon Arena Showcase Card (Exact Bricks Breaker Quest vibe!)
            LiveNeonShowcaseBanner(
                selectedSkin = progress.selectedSkin,
                highestStage = progress.highestUnlockedStage,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(178.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // PRIMARY HUGE 3D GLOWING "START GAME" BUTTON -> OPENS STAGES / LEVELS
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF1D4ED8))
                    .padding(bottom = 5.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(BrickQuestColors.PlayButtonBrush)
                    .border(1.8.dp, Color.White.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
                    .clickable(onClick = onStartGame)
                    .minimumInteractiveComponentSize()
                    .testTag("home_start_game_button")
                    .padding(vertical = 18.dp, horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "START GAME",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "30 Neon Quest Stages  •  Stage ${progress.highestUnlockedStage} Unlocked",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Secondary Action Row: Quick Continue Stage + Endless Survival Mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Quick Play Stage X
                Surface(
                    onClick = onPlayCurrentStage,
                    shape = RoundedCornerShape(18.dp),
                    color = BrickQuestColors.CardSurfaceElevated,
                    border = BorderStroke(1.5.dp, BrickQuestColors.NeonCyan.copy(alpha = 0.7f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(74.dp)
                        .testTag("home_continue_stage_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(BrickQuestColors.NeonCyan.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.GridView,
                                contentDescription = null,
                                tint = BrickQuestColors.NeonCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "STAGE ${progress.highestUnlockedStage}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = BrickQuestColors.TextPrimary,
                                maxLines = 1
                            )
                            Text(
                                text = "Quick Play",
                                style = MaterialTheme.typography.labelSmall,
                                color = BrickQuestColors.NeonCyanBright
                            )
                        }
                    }
                }

                // Endless Survival Mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(74.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(BrickQuestColors.EndlessButtonBrush)
                        .border(1.5.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                        .clickable(onClick = onStartEndless)
                        .testTag("home_endless_button")
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AllInclusive,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "ENDLESS",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = Color.White,
                                maxLines = 1
                            )
                            Text(
                                text = "High Score Run",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Power-Up Arsenal Preview Strip (Matches Screenshot 1 ⚡ and 🔺!)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = BrickQuestColors.CardSurface,
                border = BorderStroke(1.2.dp, BrickQuestColors.CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PowerUpInventoryBadge(
                        title = "Row Lightning",
                        count = progress.lightningCount,
                        isLightning = true
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(34.dp)
                            .background(BrickQuestColors.CardBorder)
                    )
                    PowerUpInventoryBadge(
                        title = "+25 Ball Storm",
                        count = progress.extraBallsCount,
                        isLightning = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Ball Skins Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BALL SKINS",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp
                    ),
                    color = BrickQuestColors.TextSecondary
                )
                Text(
                    text = "Earn ★ to unlock",
                    style = MaterialTheme.typography.labelSmall,
                    color = BrickQuestColors.NeonGold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(end = 4.dp)
            ) {
                items(BallSkin.entries) { skin ->
                    val unlocked = progress.isSkinUnlocked(skin)
                    val selected = progress.selectedSkin == skin
                    BallSkinCard(
                        skin = skin,
                        unlocked = unlocked,
                        selected = selected,
                        onClick = { onSelectSkin(skin) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun NeonCircleIconToggle(
    active: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    tag: String,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = if (active) BrickQuestColors.CardSurfaceElevated else BrickQuestColors.CardSurface,
        border = BorderStroke(
            width = 1.4.dp,
            color = if (active) BrickQuestColors.NeonCyan else BrickQuestColors.CardBorder
        ),
        modifier = Modifier.size(44.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .testTag(tag)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (active) BrickQuestColors.NeonCyanBright else BrickQuestColors.TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun PowerUpInventoryBadge(
    title: String,
    count: Int,
    isLightning: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(BrickQuestColors.CrimsonGlow, BrickQuestColors.CrimsonDeep)
                    )
                )
                .border(1.5.dp, BrickQuestColors.CrimsonBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isLightning) {
                Icon(
                    imageVector = Icons.Filled.Bolt,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Canvas(modifier = Modifier.size(22.dp)) {
                    val r = size.minDimension * 0.14f
                    val rows = listOf(1, 2, 3)
                    rows.forEachIndexed { rowIdx, countInRow ->
                        val cy = size.height * (0.24f + rowIdx * 0.25f)
                        val startX = size.width / 2f - ((countInRow - 1) * r * 2.2f) / 2f
                        for (i in 0 until countInRow) {
                            drawCircle(
                                color = Color.White,
                                radius = r,
                                center = Offset(startX + i * r * 2.2f, cy)
                            )
                        }
                    }
                }
            }
        }
        Column {
            Text(
                text = "x $count",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                color = BrickQuestColors.TextPrimary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = BrickQuestColors.TextSecondary
            )
        }
    }
}

@Composable
private fun BallSkinCard(
    skin: BallSkin,
    unlocked: Boolean,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = when {
        selected -> BrickQuestColors.NeonCyan
        unlocked -> BrickQuestColors.CardBorder
        else -> BrickQuestColors.CardBorder.copy(alpha = 0.5f)
    }

    Surface(
        onClick = onClick,
        enabled = unlocked,
        shape = RoundedCornerShape(16.dp),
        color = if (selected) BrickQuestColors.CardSurfaceElevated else BrickQuestColors.CardSurface,
        border = BorderStroke(if (selected) 2.dp else 1.2.dp, borderColor),
        modifier = Modifier
            .width(104.dp)
            .testTag("ball_skin_${skin.name.lowercase()}")
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier.size(44.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawBallSkinGraphic(
                        skin = skin,
                        center = center,
                        radius = size.minDimension * 0.24f
                    )
                }
                if (selected) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Selected",
                        tint = BrickQuestColors.NeonCyan,
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.TopEnd)
                    )
                } else if (!unlocked) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Locked",
                        tint = BrickQuestColors.TextMuted,
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.TopEnd)
                    )
                }
            }

            Text(
                text = skin.displayName,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = if (unlocked) BrickQuestColors.TextPrimary else BrickQuestColors.TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (!unlocked) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = BrickQuestColors.NeonGold,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "${skin.unlockStars}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = BrickQuestColors.NeonGold
                    )
                }
            } else {
                Text(
                    text = if (selected) "EQUIPPED" else "USE",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (selected) BrickQuestColors.NeonCyan else BrickQuestColors.TextSecondary
                )
            }
        }
    }
}

private val SHOWCASE_PALETTE = arrayOf(
    Color(0xFFEF4444), Color(0xFFEF4444), Color(0xFFFACC15),
    Color(0xFF22D3EE), Color(0xFF22D3EE), Color(0xFF22C55E), Color(0xFFA855F7)
)

@Composable
private fun LiveNeonShowcaseBanner(
    selectedSkin: BallSkin,
    highestStage: Int,
    modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition(label = "showcase")
    val animTState = infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing), RepeatMode.Restart),
        label = "balls"
    )
    val laserPulseState = infinite.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(750), RepeatMode.Reverse),
        label = "laser"
    )
    val starPath = remember { Path() }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF040915))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Read animated values ONLY inside draw phase -> zero Home recompositions!
            val animT = animTState.value
            val laserPulse = laserPulseState.value
            val pad = 10.dp.toPx()
            val innerW = size.width - pad * 2
            val innerH = size.height - pad * 2
            val frameCorner = CornerRadius(16.dp.toPx(), 16.dp.toPx())
            val brickCorner = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            val brickBorder = Stroke(width = 1.6.dp.toPx())

            drawRoundRect(
                brush = BrickQuestColors.ArenaBorderBrush,
                topLeft = Offset(pad, pad),
                size = Size(innerW, innerH),
                cornerRadius = frameCorner,
                alpha = 0.25f,
                style = Stroke(width = 8.dp.toPx())
            )
            drawRoundRect(
                brush = BrickQuestColors.ArenaBorderBrush,
                topLeft = Offset(pad, pad),
                size = Size(innerW, innerH),
                cornerRadius = frameCorner,
                style = Stroke(width = 2.dp.toPx())
            )

            val cols = 7
            val brickW = (innerW * 0.80f) / cols
            val brickH = brickW * 0.72f
            val startX = pad + (innerW - brickW * cols) / 2f
            val startY = pad + innerH * 0.14f
            val inset = 3.dp.toPx()
            val bw = brickW - inset * 2f
            val bh = brickH - inset * 2f
            val cyanCol = Color(0xFF22D3EE)

            for (r in 0..1) {
                for (c in 0 until cols) {
                    val col = if (r == 1 && c in 1..5) cyanCol else SHOWCASE_PALETTE[(c + r * 2) % SHOWCASE_PALETTE.size]
                    val left = startX + c * brickW + inset
                    val top = startY + r * brickH + inset
                    drawRoundRect(color = col.copy(alpha = 0.22f), topLeft = Offset(left, top), size = Size(bw, bh), cornerRadius = brickCorner)
                    drawRoundRect(color = col, topLeft = Offset(left, top), size = Size(bw, bh), cornerRadius = brickCorner, style = brickBorder)
                }
            }

            val laserY = startY + brickH * 1.5f
            drawLine(color = BrickQuestColors.NeonGreen.copy(alpha = 0.35f * laserPulse), start = Offset(pad + 6f, laserY), end = Offset(size.width - pad - 6f, laserY), strokeWidth = 10.dp.toPx(), cap = StrokeCap.Round)
            drawLine(color = BrickQuestColors.NeonGreen.copy(alpha = 0.85f * laserPulse), start = Offset(pad + 6f, laserY), end = Offset(size.width - pad - 6f, laserY), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
            drawLine(color = Color.White.copy(alpha = 0.9f * laserPulse), start = Offset(pad + 6f, laserY), end = Offset(size.width - pad - 6f, laserY), strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)

            val originX = pad + innerW * 0.44f
            val originY = pad + innerH * 0.88f
            val targetX = pad + innerW * 0.20f
            val targetY = startY + brickH * 1.6f
            val bounceX = pad + innerW * 0.72f
            val bounceY = pad + innerH * 0.80f
            val ballR = 5.5.dp.toPx()

            for (i in 0 until 7) {
                val p = (animT + i * 0.14f) % 1f
                val pos = if (p < 0.5f) {
                    val u = p * 2f
                    Offset(originX + (targetX - originX) * u, originY + (targetY - originY) * u)
                } else {
                    val u = (p - 0.5f) * 2f
                    Offset(targetX + (bounceX - targetX) * u, targetY + (bounceY - targetY) * u)
                }
                drawBallSkinGraphic(
                    skin = selectedSkin,
                    center = pos,
                    radius = ballR,
                    rotationRad = animT * 6.28f,
                    reusableStarPath = starPath
                )
            }

            val sparkBaseX = targetX + 8.dp.toPx()
            val sparkBaseY = targetY
            val sparkSize = 5.dp.toPx()
            val sparkStroke = Stroke(width = 1.3.dp.toPx())
            val sparkColor = Color(0xFF67E8F9).copy(alpha = 0.75f * laserPulse)
            for (i in 0 until 8) {
                val a = i * 0.78f + animT * 2f
                val dist = (6.dp.toPx() + (i % 3) * 7.dp.toPx()) * laserPulse
                drawRect(
                    color = sparkColor,
                    topLeft = Offset(sparkBaseX + cos(a) * dist, sparkBaseY + sin(a) * dist),
                    size = Size(sparkSize, sparkSize),
                    style = sparkStroke
                )
            }
        }

        // Bottom-right subtle stage pill overlay
        Surface(
            shape = RoundedCornerShape(50),
            color = BrickQuestColors.CardSurface.copy(alpha = 0.88f),
            border = BorderStroke(1.dp, BrickQuestColors.NeonCyan.copy(alpha = 0.6f)),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(18.dp)
        ) {
            Text(
                text = "NEXT: STAGE $highestStage",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = BrickQuestColors.NeonCyanBright,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}
