package com.example.brickquest.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.brickquest.data.BallSkin
import com.example.brickquest.data.BrickQuestProgress
import com.example.brickquest.data.TOTAL_QUEST_STAGES
import com.example.brickquest.engine.StageWorlds
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

private val ArcadeButtonFillBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF38BDF8),
        Color(0xFF2563EB),
        Color(0xFF5B21B6)
    )
)

private val ArcadeButtonRimBrush = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF67E8F9),
        Color(0xFFE879F9),
        Color(0xFF67E8F9)
    )
)

private val HomeCenterLegibilityScrim = Brush.verticalGradient(
    0.0f to Color(0x3805081E),
    0.20f to Color(0x28060B28),
    0.50f to Color(0x48070B2A),
    0.76f to Color(0x18070B2A),
    1.0f to Color(0x00070B2A)
)

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

        else -> {
            // Generic glow look for the newer skins (colors come from BallSkin)
            val glow = Color(skin.glowArgb)
            drawCircle(color = glow.copy(alpha = 0.27f), radius = radius * 1.95f, center = center)
            drawCircle(color = glow.copy(alpha = 0.53f), radius = radius * 1.38f, center = center)
            drawCircle(color = Color(skin.ringArgb), radius = radius * 1.02f, center = center)
            drawCircle(color = Color(skin.coreArgb), radius = radius * 0.68f, center = center)
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
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_home_arcade_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(HomeCenterLegibilityScrim)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            contentAlignment = Alignment.Center
        ) {
            ArcadeGameTitle(showStageTag = false)
        }
    }
}

@Composable
fun BrickQuestHomeScreen(
    progress: BrickQuestProgress,
    onStartGame: () -> Unit,
    onSelectSkin: (BallSkin) -> Unit,
    onToggleSound: () -> Unit,
    onToggleHaptics: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        // Full-bleed static AAA arcade game background artwork (zero animations)
        Image(
            painter = painterResource(id = R.drawable.img_home_arcade_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Subtle center contrast veil that preserves vivid side bricks and bottom glowing paddle
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(HomeCenterLegibilityScrim)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .widthIn(max = 560.dp)
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xC2090F2D),
                        border = BorderStroke(1.4.dp, BrickQuestColors.NeonGold.copy(alpha = 0.75f))
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
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = Color.White
                            )
                        }
                    }

                    if (progress.endlessBestScore > 0) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xC2090F2D),
                            border = BorderStroke(1.4.dp, BrickQuestColors.NeonCyan.copy(alpha = 0.7f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.EmojiEvents,
                                    contentDescription = "Best Score",
                                    tint = BrickQuestColors.NeonCyanBright,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "${progress.endlessBestScore}",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ArcadeCircleIconToggle(
                        active = progress.soundEnabled,
                        icon = if (progress.soundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                        contentDescription = "Toggle Sound",
                        tag = "home_sound_toggle",
                        onClick = onToggleSound
                    )
                    ArcadeCircleIconToggle(
                        active = progress.hapticsEnabled,
                        icon = Icons.Filled.Vibration,
                        contentDescription = "Toggle Haptics",
                        tag = "home_haptics_toggle",
                        onClick = onToggleHaptics
                    )
                }
            }

            // Room for the bursting top neon bricks & blazing chrome ball artwork
            Spacer(modifier = Modifier.height(64.dp))

            // Bold Colorful 3D Arcade Title Emblem
            ArcadeGameTitle(showStageTag = true)

            Spacer(modifier = Modifier.height(44.dp))

            // PRIMARY GLOSSY NEON 3D "START GAME" BUTTON -> OPENS LEVELS
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.84f)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0xFF1A145E))
                    .padding(bottom = 5.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(ArcadeButtonFillBrush)
                    .border(2.2.dp, ArcadeButtonRimBrush, RoundedCornerShape(30.dp))
                    .clickable(onClick = onStartGame)
                    .minimumInteractiveComponentSize()
                    .testTag("home_start_game_button"),
                contentAlignment = Alignment.Center
            ) {
                // Glossy top specular sheen strip
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.86f)
                        .height(14.dp)
                        .align(Alignment.TopCenter)
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.24f))
                )

                Row(
                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 22.dp),
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
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "START GAME",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.8.sp,
                                shadow = Shadow(
                                    color = Color(0xAA040826),
                                    offset = Offset(0f, 3f),
                                    blurRadius = 6f
                                )
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "Stage ${progress.highestUnlockedStage}  •  World ${StageWorlds.worldIndex(progress.highestUnlockedStage) + 1}: ${StageWorlds.name(StageWorlds.worldIndex(progress.highestUnlockedStage))}  •  $TOTAL_QUEST_STAGES Levels",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color(0xFFE0F2FE),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Compact Translucent Arsenal + Ball Skins Panel (floats cleanly above the glowing paddle!)
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color(0xB8080D2B),
                border = BorderStroke(1.3.dp, Color(0x8038BDF8)),
                modifier = Modifier.fillMaxWidth(0.92f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Compact Power-Up Inventory Strip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CompactPowerUpBadge(
                            title = "Lightning",
                            count = progress.lightningCount,
                            isLightning = true
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(26.dp)
                                .background(Color(0x4567E8F9))
                        )
                        CompactPowerUpBadge(
                            title = "+25 Balls",
                            count = progress.extraBallsCount,
                            isLightning = false
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0x3038BDF8))
                    )

                    // Ball skin selector (scrolls sideways: there are more skins than fit in one row)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "BALL SKIN",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.2.sp
                            ),
                            color = Color(0xFFBAE6FD)
                        )
                        Text(
                            text = progress.selectedSkin.displayName,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = BrickQuestColors.NeonGold
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BallSkin.entries.forEach { skin ->
                            val unlocked = progress.isSkinUnlocked(skin)
                            val selected = progress.selectedSkin == skin
                            CompactBallSkinOrb(
                                skin = skin,
                                unlocked = unlocked,
                                selected = selected,
                                onClick = { onSelectSkin(skin) },
                                modifier = Modifier.width(62.dp)
                            )
                        }
                    }
                }
            }

            // Generous bottom clearance so the glowing red/silver/cyan paddle in the background stays fully visible
            Spacer(modifier = Modifier.height(136.dp))
        }
    }
}

@Composable
private fun ArcadeGameTitle(showStageTag: Boolean) {
    val brickText = buildAnnotatedString {
        withStyle(SpanStyle(color = Color(0xFFFF4D2D))) { append("B") }
        withStyle(SpanStyle(color = Color(0xFFFFC928))) { append("R") }
        withStyle(SpanStyle(color = Color(0xFF5CE637))) { append("I") }
        withStyle(SpanStyle(color = Color(0xFFE040FB))) { append("C") }
        withStyle(SpanStyle(color = Color(0xFF29D8FF))) { append("K") }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = brickText,
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp,
                shadow = Shadow(
                    color = Color(0xF003061A),
                    offset = Offset(0f, 6f),
                    blurRadius = 12f
                )
            ),
            textAlign = TextAlign.Center
        )

        Text(
            text = "BREAKER",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                letterSpacing = 4.sp,
                shadow = Shadow(
                    color = Color(0xE60B296E),
                    offset = Offset(0f, 5f),
                    blurRadius = 14f
                )
            ),
            color = Color(0xFF5CEBFF),
            textAlign = TextAlign.Center
        )

        if (showStageTag) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0xB0090E2E),
                border = BorderStroke(1.2.dp, Color(0x9938BDF8))
            ) {
                Text(
                    text = "QUEST  •  ARCADE EDITION",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    ),
                    color = Color(0xFFE0F2FE),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                )
            }
        }
    }
}

@Composable
private fun ArcadeCircleIconToggle(
    active: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    tag: String,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = if (active) Color(0xD4111D52) else Color(0xB3090D24),
        border = BorderStroke(
            width = 1.6.dp,
            color = if (active) Color(0xFF38BDF8) else Color(0x6694A3B8)
        ),
        modifier = Modifier.size(46.dp)
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
                tint = if (active) Color(0xFF7DD3FC) else Color(0xFF94A3B8),
                modifier = Modifier.size(21.dp)
            )
        }
    }
}

@Composable
private fun CompactPowerUpBadge(
    title: String,
    count: Int,
    isLightning: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(BrickQuestColors.CrimsonGlow, BrickQuestColors.CrimsonDeep)
                    )
                )
                .border(1.4.dp, BrickQuestColors.CrimsonBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isLightning) {
                Icon(
                    imageVector = Icons.Filled.Bolt,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Canvas(modifier = Modifier.size(18.dp)) {
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
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                color = Color.White
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFBAE6FD)
            )
        }
    }
}

@Composable
private fun CompactBallSkinOrb(
    skin: BallSkin,
    unlocked: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = when {
        selected -> Color(0xFF38BDF8)
        unlocked -> Color(0x55818CF8)
        else -> Color(0x3364748B)
    }

    Column(
        modifier = modifier
            .padding(horizontal = 3.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) Color(0x881D2B6E) else Color(0x550C1338))
            .border(if (selected) 1.8.dp else 1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(enabled = unlocked, onClick = onClick)
            .minimumInteractiveComponentSize()
            .testTag("ball_skin_${skin.name.lowercase()}")
            .padding(vertical = 7.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(
            modifier = Modifier.size(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawBallSkinGraphic(
                    skin = skin,
                    center = center,
                    radius = size.minDimension * 0.25f
                )
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Selected",
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier
                        .size(13.dp)
                        .align(Alignment.TopEnd)
                )
            } else if (!unlocked) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "Locked",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier
                        .size(13.dp)
                        .align(Alignment.TopEnd)
                )
            }
        }

        if (!unlocked) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = BrickQuestColors.NeonGold,
                    modifier = Modifier.size(10.dp)
                )
                Text(
                    text = "${skin.unlockStars}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = BrickQuestColors.NeonGold
                )
            }
        } else {
            Text(
                text = if (selected) "ON" else "USE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = if (selected) Color(0xFF67E8F9) else Color(0xFFCBD5E1)
            )
        }
    }
}
