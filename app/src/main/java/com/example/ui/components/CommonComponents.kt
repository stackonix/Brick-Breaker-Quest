package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.BrainSkills
import com.example.domain.model.RankTier
import com.example.domain.model.RivalId
import com.example.ui.i18n.LocalStrings
import com.example.ui.theme.ArcadeCoral
import com.example.ui.theme.ArcadeCyan
import com.example.ui.theme.ArcadeGold
import com.example.ui.theme.ArcadeMint
import com.example.ui.theme.LocalMindfeudColors
import com.example.ui.theme.RoyalVioletBright
import com.example.ui.theme.WhiteCanvasBottom
import com.example.ui.theme.WhiteCanvasMid
import com.example.ui.theme.WhiteCanvasTop
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Tactile 3D Arcade Game Card with crisp tinted surface and chunky 4dp colored bottom ledge.
 */
@Composable
fun MindfeudCard(
    modifier: Modifier = Modifier,
    borderColor: Color = LocalMindfeudColors.current.borderSubtle,
    containerColor: Color = LocalMindfeudColors.current.cardSurface,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val palette = LocalMindfeudColors.current
    val shape = RoundedCornerShape(22.dp)
    val lipColor = if (borderColor == palette.borderSubtle) {
        palette.cardLip
    } else {
        borderColor.copy(alpha = if (palette.isDark) 0.5f else 0.55f)
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(lipColor)
            .padding(bottom = 4.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clip(shape).clickable(onClick = onClick) else Modifier),
            shape = shape,
            color = containerColor,
            border = BorderStroke(1.8.dp, borderColor),
            shadowElevation = if (palette.isDark) 0.dp else 2.dp
        ) {
            Column(
                modifier = Modifier.padding(contentPadding),
                content = content
            )
        }
    }
}

/**
 * Vibrant Full-Color 3D Gradient Game Card (for Daily Puzzle, Duel Hero, Friend Challenge, Practice, Brain Profile).
 * Includes chunky matching 3D lip and subtle decorative arcade circles.
 */
@Composable
fun MindfeudGradientCard(
    gradientColors: List<Color>,
    lipColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(lipColor)
            .padding(bottom = 5.dp)
            .clip(shape)
            .background(Brush.linearGradient(gradientColors))
            .drawBehind {
                // Playful translucent arcade circles for glossy mobile-game richness
                drawCircle(
                    color = Color.White.copy(alpha = 0.14f),
                    radius = size.minDimension * 0.55f,
                    center = Offset(size.width * 0.92f, size.height * 0.12f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.08f),
                    radius = size.minDimension * 0.34f,
                    center = Offset(size.width * 0.10f, size.height * 0.92f)
                )
            }
            .border(1.5.dp, Color.White.copy(alpha = 0.28f), shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            content = content
        )
    }
}

/**
 * Juicy 3D Arcade Play Button with 5dp bottom ledge, vibrant Sunset Orange-Gold gradient,
 * and glossy game sheen highlight.
 */
@Composable
fun MindfeudPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    topColor: Color = LocalMindfeudColors.current.primaryActionTop,
    bottomColor: Color = LocalMindfeudColors.current.primaryActionBg,
    lipColorOverride: Color = LocalMindfeudColors.current.primaryActionLip,
    contentColor: Color = LocalMindfeudColors.current.primaryActionFg,
    testTagName: String = "primary_cta_button"
) {
    val palette = LocalMindfeudColors.current
    val outerShape = RoundedCornerShape(18.dp)
    val lipColor = if (enabled) lipColorOverride else palette.borderSubtle
    val topBrush = if (enabled) {
        Brush.verticalGradient(listOf(topColor, bottomColor))
    } else {
        Brush.verticalGradient(listOf(palette.elevatedSurface, palette.elevatedSurface))
    }
    val fgColor = if (enabled) contentColor else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .minimumInteractiveComponentSize()
            .clip(outerShape)
            .background(lipColor)
            .padding(bottom = 5.dp)
            .clip(outerShape)
            .background(topBrush)
            .border(1.2.dp, Color.White.copy(alpha = if (enabled) 0.38f else 0f), outerShape)
            .clickable(enabled = enabled, onClick = onClick)
            .testTag(testTagName),
        contentAlignment = Alignment.Center
    ) {
        // Glossy arcade shine bar at top
        if (enabled) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth(0.90f)
                    .padding(top = 3.dp)
                    .height(7.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.28f))
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = fgColor,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(10.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                ),
                color = fgColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun MindfeudSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    accentColor: Color = LocalMindfeudColors.current.violetText,
    testTagName: String = "secondary_cta_button"
) {
    val palette = LocalMindfeudColors.current
    val shape = RoundedCornerShape(16.dp)
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .height(54.dp)
            .minimumInteractiveComponentSize()
            .testTag(testTagName),
        shape = shape,
        border = BorderStroke(2.dp, accentColor.copy(alpha = 0.75f)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = palette.cardSurface,
            contentColor = accentColor
        )
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ScreenTopBar(
    title: String,
    onBack: () -> Unit,
    trailing: @Composable (() -> Unit)? = null
) {
    val strings = LocalStrings.current
    val palette = LocalMindfeudColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = palette.cardSurface,
            border = BorderStroke(1.2.dp, palette.borderSubtle),
            modifier = Modifier.size(44.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("top_bar_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = strings.backContentDesc,
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        trailing?.invoke()
    }
}

@Composable
fun RankChip(
    rank: RankTier,
    rankLabel: String,
    xp: Int,
    onClick: (() -> Unit)? = null
) {
    val palette = LocalMindfeudColors.current
    val chipColor = if (rank == RankTier.LEGEND) palette.gold else palette.violet
    val chipBg = if (rank == RankTier.LEGEND) palette.goldContainer else palette.violetContainer
    val textColor = if (rank == RankTier.LEGEND) palette.goldText else palette.violetText

    Surface(
        shape = RoundedCornerShape(50),
        color = chipBg,
        border = BorderStroke(1.8.dp, chipColor.copy(alpha = 0.7f)),
        shadowElevation = if (palette.isDark) 0.dp else 2.dp,
        modifier = Modifier
            .then(
                if (onClick != null) Modifier.clip(RoundedCornerShape(50)).clickable(onClick = onClick) else Modifier
            )
            .testTag("rank_chip")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.WorkspacePremium,
                contentDescription = rankLabel,
                tint = chipColor,
                modifier = Modifier.size(19.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "$rankLabel • $xp XP",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFeatureSettings = "tnum"
                ),
                color = textColor,
                maxLines = 1
            )
        }
    }
}

@Composable
fun StreakChip(
    streak: Int,
    freezes: Int,
    onDarkHeader: Boolean = false
) {
    val palette = LocalMindfeudColors.current
    val bg = if (onDarkHeader) Color.White.copy(alpha = 0.20f) else palette.goldContainer
    val border = if (onDarkHeader) Color.White.copy(alpha = 0.45f) else palette.gold.copy(alpha = 0.7f)
    val streakTextColor = if (onDarkHeader) Color.White else palette.goldText
    val freezeTextColor = if (onDarkHeader) Color(0xFFA5F3FC) else palette.cyanText

    Surface(
        shape = RoundedCornerShape(50),
        color = bg,
        border = BorderStroke(1.4.dp, border),
        modifier = Modifier.testTag("streak_chip")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.LocalFireDepartment,
                contentDescription = "Streak",
                tint = Color(0xFFFDE047),
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "$streak",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFeatureSettings = "tnum"
                ),
                color = streakTextColor
            )
            if (freezes > 0) {
                Spacer(Modifier.width(7.dp))
                Icon(
                    imageVector = Icons.Filled.AcUnit,
                    contentDescription = "Streak Freezes",
                    tint = Color(0xFF67E8F9),
                    modifier = Modifier.size(15.dp)
                )
                Spacer(Modifier.width(2.dp))
                Text(
                    text = "$freezes",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFeatureSettings = "tnum"
                    ),
                    color = freezeTextColor
                )
            }
        }
    }
}

@Composable
fun RivalAvatar(
    rival: RivalId,
    contentDescription: String,
    size: Dp = 56.dp,
    borderColor: Color = LocalMindfeudColors.current.cyan
) {
    Image(
        painter = painterResource(id = rival.avatarRes),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .border(3.dp, borderColor, CircleShape)
    )
}

@Composable
fun LeaveMatchDialog(
    onKeepPlaying: () -> Unit,
    onLeave: () -> Unit
) {
    val strings = LocalStrings.current
    val palette = LocalMindfeudColors.current
    AlertDialog(
        onDismissRequest = onKeepPlaying,
        containerColor = palette.cardSurface,
        title = {
            Text(
                text = strings.leaveDialogTitle,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Text(
                text = strings.leaveDialogBody,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onKeepPlaying,
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.primaryActionBg,
                    contentColor = palette.primaryActionFg
                ),
                modifier = Modifier.testTag("keep_playing_button")
            ) {
                Text(strings.keepPlayingCta, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onLeave,
                modifier = Modifier.testTag("confirm_leave_button")
            ) {
                Text(strings.leaveMatchCta, color = palette.coralText)
            }
        }
    )
}

@Composable
fun ConfettiOverlay(
    trigger: Boolean,
    reduceMotion: Boolean,
    modifier: Modifier = Modifier
) {
    if (!trigger || reduceMotion) return
    val palette = LocalMindfeudColors.current
    val progress = remember { Animatable(0f) }

    val particles = remember {
        val rng = Random(42)
        val colors = listOf(palette.cyan, palette.violet, palette.mint, palette.gold, palette.coral)
        List(38) {
            ConfettiParticle(
                angle = rng.nextFloat() * 2f * PI.toFloat(),
                speed = 0.25f + rng.nextFloat() * 0.65f,
                radius = 6f + rng.nextFloat() * 8f,
                color = colors[it % colors.size]
            )
        }
    }

    LaunchedEffect(trigger) {
        progress.snapTo(0f)
        progress.animateTo(1f, animationSpec = tween(durationMillis = 700, easing = LinearEasing))
    }

    val p = progress.value
    if (p >= 1f) return

    Canvas(modifier = modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height * 0.34f
        val maxDist = size.minDimension * 0.52f
        val alpha = (1f - p).coerceIn(0f, 1f)
        for (particle in particles) {
            val dist = maxDist * particle.speed * p
            val x = cx + cos(particle.angle) * dist
            val y = cy + sin(particle.angle) * dist + (p * p * 180f)
            drawCircle(
                color = particle.color.copy(alpha = alpha),
                radius = particle.radius * (1f - 0.3f * p),
                center = Offset(x, y)
            )
        }
    }
}

private data class ConfettiParticle(
    val angle: Float,
    val speed: Float,
    val radius: Float,
    val color: Color
)

@Composable
fun BrainRadarChart(
    skills: BrainSkills,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    val palette = LocalMindfeudColors.current
    val values = listOf(skills.logic, skills.speed, skills.memory, skills.pattern, skills.word)

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val radius = (size.minDimension / 2f) * 0.72f

            for (ring in 1..4) {
                val r = radius * (ring / 4f)
                val gridPath = Path()
                for (i in 0 until 5) {
                    val angle = -PI / 2.0 + i * (2.0 * PI / 5.0)
                    val x = cx + (r * cos(angle)).toFloat()
                    val y = cy + (r * sin(angle)).toFloat()
                    if (i == 0) gridPath.moveTo(x, y) else gridPath.lineTo(x, y)
                }
                gridPath.close()
                drawPath(
                    path = gridPath,
                    color = palette.borderSubtle.copy(alpha = 0.9f),
                    style = Stroke(width = 2.2f)
                )
            }

            for (i in 0 until 5) {
                val angle = -PI / 2.0 + i * (2.0 * PI / 5.0)
                val x = cx + (radius * cos(angle)).toFloat()
                val y = cy + (radius * sin(angle)).toFloat()
                drawLine(
                    color = palette.borderSubtle.copy(alpha = 0.75f),
                    start = Offset(cx, cy),
                    end = Offset(x, y),
                    strokeWidth = 2f
                )
            }

            val dataPath = Path()
            val points = mutableListOf<Offset>()
            for (i in 0 until 5) {
                val angle = -PI / 2.0 + i * (2.0 * PI / 5.0)
                val norm = (values[i].coerceIn(10, 100)) / 100f
                val pt = Offset(
                    cx + (radius * norm * cos(angle)).toFloat(),
                    cy + (radius * norm * sin(angle)).toFloat()
                )
                points.add(pt)
                if (i == 0) dataPath.moveTo(pt.x, pt.y) else dataPath.lineTo(pt.x, pt.y)
            }
            dataPath.close()

            drawPath(
                path = dataPath,
                brush = Brush.radialGradient(
                    colors = listOf(palette.violet.copy(alpha = 0.45f), palette.cyan.copy(alpha = 0.30f)),
                    center = Offset(cx, cy),
                    radius = radius
                )
            )
            drawPath(
                path = dataPath,
                color = palette.violet,
                style = Stroke(width = 5f)
            )

            for (pt in points) {
                drawCircle(color = palette.violet, radius = 8f, center = pt)
                drawCircle(color = Color.White, radius = 3.5f, center = pt)
            }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${labels.getOrElse(0) { "Logic" }} ${values[0]}",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = palette.violetText
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${labels.getOrElse(4) { "Word" }} ${values[4]}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = palette.cyanText
                )
                Text(
                    text = "${labels.getOrElse(1) { "Speed" }} ${values[1]}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = palette.goldText
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${labels.getOrElse(3) { "Pattern" }} ${values[3]}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = palette.coralText
                )
                Text(
                    text = "${labels.getOrElse(2) { "Memory" }} ${values[2]}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = palette.mintText
                )
            }
        }
    }
}

/**
 * Lively Colorful Pastel Arcade Game Backdrop (Sky Blue -> Lavender -> Mint Glow)
 * with playful multi-color geometric blobs and floating game shapes — bright, colorful, and never plain white!
 */
@Composable
fun AmbientDarkBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val palette = LocalMindfeudColors.current
    val bg = MaterialTheme.colorScheme.background
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = if (palette.isDark) {
                        listOf(Color(0xFF131733), bg, Color(0xFF0B0E1F))
                    } else {
                        listOf(WhiteCanvasTop, WhiteCanvasMid, WhiteCanvasBottom)
                    }
                )
            )
            .drawBehind {
                if (!palette.isDark) {
                    // Rich multi-color pastel game glows so canvas feels vibrant and alive
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(RoyalVioletBright.copy(alpha = 0.22f), Color.Transparent),
                            center = Offset(size.width * 0.90f, size.height * 0.14f),
                            radius = size.minDimension * 0.68f
                        ),
                        radius = size.minDimension * 0.68f,
                        center = Offset(size.width * 0.90f, size.height * 0.14f)
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(ArcadeCyan.copy(alpha = 0.22f), Color.Transparent),
                            center = Offset(size.width * 0.08f, size.height * 0.42f),
                            radius = size.minDimension * 0.58f
                        ),
                        radius = size.minDimension * 0.58f,
                        center = Offset(size.width * 0.08f, size.height * 0.42f)
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(ArcadeGold.copy(alpha = 0.18f), Color.Transparent),
                            center = Offset(size.width * 0.88f, size.height * 0.72f),
                            radius = size.minDimension * 0.55f
                        ),
                        radius = size.minDimension * 0.55f,
                        center = Offset(size.width * 0.88f, size.height * 0.72f)
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(ArcadeMint.copy(alpha = 0.20f), Color.Transparent),
                            center = Offset(size.width * 0.16f, size.height * 0.92f),
                            radius = size.minDimension * 0.52f
                        ),
                        radius = size.minDimension * 0.52f,
                        center = Offset(size.width * 0.16f, size.height * 0.92f)
                    )

                    // Subtle colorful arcade confetti dots & geometric diamonds
                    val dots = listOf(
                        Triple(0.14f, 0.22f, ArcadeCoral.copy(alpha = 0.22f)),
                        Triple(0.84f, 0.34f, ArcadeGold.copy(alpha = 0.28f)),
                        Triple(0.22f, 0.66f, RoyalVioletBright.copy(alpha = 0.20f)),
                        Triple(0.76f, 0.84f, ArcadeCyan.copy(alpha = 0.25f)),
                        Triple(0.50f, 0.52f, ArcadeMint.copy(alpha = 0.20f))
                    )
                    dots.forEach { (fx, fy, c) ->
                        drawCircle(
                            color = c,
                            radius = 12.dp.toPx(),
                            center = Offset(size.width * fx, size.height * fy),
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }
                }
            }
    ) {
        content()
    }
}
