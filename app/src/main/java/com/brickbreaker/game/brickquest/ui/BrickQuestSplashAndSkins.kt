package com.brickbreaker.game.brickquest.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.brickbreaker.game.R
import com.brickbreaker.game.brickquest.data.BallSkin
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

private val SplashScrim = Brush.verticalGradient(
    0.0f to Color(0x3805081E),
    0.20f to Color(0x28060B28),
    0.50f to Color(0x48070B2A),
    0.76f to Color(0x18070B2A),
    1.0f to Color(0x00070B2A)
)

/**
 * Splash screen, title emblem, and shared zero-allocation ball-skin preview renderer.
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
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }
    val titleScale by animateFloatAsState(
        targetValue = if (entered) 1f else 0.84f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "splash_scale"
    )
    val titleAlpha by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(240),
        label = "splash_alpha"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_home_neon_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SplashScrim)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .graphicsLayer {
                    scaleX = titleScale
                    scaleY = titleScale
                    alpha = titleAlpha
                },
            contentAlignment = Alignment.Center
        ) {
            QuestGameTitle()
        }
    }
}

@Composable
private fun QuestGameTitle() {
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
    }
}
