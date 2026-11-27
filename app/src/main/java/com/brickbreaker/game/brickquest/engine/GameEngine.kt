package com.brickbreaker.game.brickquest.engine

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntSize
import com.brickbreaker.game.brickquest.data.BallSkin
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

enum class GameAudioCue {
    HIT,
    BRICK_BREAK,
    LASER_OR_PICKUP
}

/**
 * Square grid cells ([cellW] = [cellH] = [cellSize]). On tall screens the arena gets extra
 * rows below the danger line ([rows], 12 up to MAX_ARENA_ROWS) instead of stretched bricks.
 */
data class ArenaMetrics(
    val offsetX: Float,
    val offsetY: Float,
    val cellSize: Float,
    val arenaWidth: Float,
    val arenaHeight: Float,
    val cellW: Float = cellSize,
    val cellH: Float = cellSize,
    val rows: Float = ARENA_ROWS.toFloat()
)

private val CACHED_HP_STRINGS = Array(512) { it.toString() }
private val CACHED_COUNT_STRINGS = Array(256) { "x$it" }

private fun hpString(hp: Int): String =
    if (hp in 0 until CACHED_HP_STRINGS.size) CACHED_HP_STRINGS[hp] else hp.toString()

private fun ballCountString(count: Int): String =
    if (count in 0 until CACHED_COUNT_STRINGS.size) CACHED_COUNT_STRINGS[count] else "x$count"

/**
 * Hardware-friendly native Canvas render cache:
 * 1) Pre-bakes the static arena backdrop, danger dashed line, and glowing neon gradient border into
 *    a single cached Bitmap (1 blit per frame instead of expensive stroked gradient paths!).
 * 2) Pre-bakes each BallSkin (with lush radial neon glow / 5-point Starlight polygon) into a tiny
 *    sprite Bitmap (1 drawBitmap per ball instead of 5 blended drawCircle/drawPath calls!).
 * 3) Batches all bricks, pickups, beams, particles, and text into ONE native Canvas pass with
 *    zero per-frame heap allocations and zero GC pressure.
 */
private class NativeRenderCache {
    var lastW = -1
    var lastH = -1
    var metrics = ArenaMetrics(0f, 0f, 1f, 1f, 1f)
    var brickCornerPx = 10f
    var hpTextSize = 24f

    // Cached static arena backdrop + neon outer frame bitmap (arena-sized, not full screen)
    var arenaFrameBitmap: Bitmap? = null
    var arenaFrameLeft: Float = 0f
    var arenaFrameTop: Float = 0f

    // Cached pre-rendered ball sprite bitmap (rebuilt ONLY if skin or ball radius changes)
    var ballSpriteBitmap: Bitmap? = null
    var ballSpriteHalfSize: Float = 0f
    private var cachedSpriteSkin: BallSkin? = null
    private var cachedSpriteRadiusPx: Int = -1

    // Pre-allocated native Paints (reused every frame, ZERO allocations)
    val bitmapPaint = Paint(Paint.FILTER_BITMAP_FLAG)

    val brickFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    val brickStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    val brickHighlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = 0x4DFFFFFF
        strokeWidth = 1.6f
    }
    val bombBadgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = 0x5CFDE047
    }
    val brickHpPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    val pickupFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    val pickupStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    val laserPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    val particleFillPaint = Paint().apply {
        style = Paint.Style.FILL
    }
    val particleStrokePaint = Paint().apply {
        style = Paint.Style.STROKE
    }
    val aimDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val paddleFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    val paddleStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    fun ensureLayout(width: Float, height: Float, density: Float): ArenaMetrics {
        val iw = width.toInt().coerceAtLeast(1)
        val ih = height.toInt().coerceAtLeast(1)
        if (iw != lastW || ih != lastH) {
            lastW = iw
            lastH = ih
            metrics = calculateArenaMetrics(width, height)
            val cs = metrics.cellSize
            brickCornerPx = cs * 0.16f
            brickStrokePaint.strokeWidth = (cs * 0.062f).coerceAtLeast(2.2f)
            bombBadgePaint.strokeWidth = 2.0f * density
            pickupStrokePaint.strokeWidth = (cs * 0.062f).coerceAtLeast(2.0f)
            particleStrokePaint.strokeWidth = 1.5f * density
            hpTextSize = (cs * 0.40f).coerceIn(18f, 44f)
            brickHpPaint.textSize = hpTextSize
            rebuildArenaFrameBitmap(iw, ih, density)
        }
        return metrics
    }

    private fun rebuildArenaFrameBitmap(w: Int, h: Int, density: Float) {
        arenaFrameBitmap?.recycle()
        val m = metrics
        // Only the arena (+ room for the glow) is baked, so each frame blends far fewer pixels.
        val pad = (6f * density).coerceAtLeast(4f)
        val bmpW = (m.arenaWidth + pad * 2f).toInt().coerceIn(1, w)
        val bmpH = (m.arenaHeight + pad * 2f).toInt().coerceIn(1, h)
        arenaFrameLeft = m.offsetX - pad
        arenaFrameTop = m.offsetY - pad
        val bmp = Bitmap.createBitmap(bmpW, bmpH, Bitmap.Config.ARGB_8888)
        val c = AndroidCanvas(bmp)
        c.translate(-arenaFrameLeft, -arenaFrameTop)
        val ox = m.offsetX
        val oy = m.offsetY
        val aw = m.arenaWidth
        val ah = m.arenaHeight
        val corner = 14f * density

        // 1. Arena inner backdrop
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = 0xB8040814.toInt()
        }
        c.drawRoundRect(ox, oy, ox + aw, oy + ah, corner, corner, bgPaint)

        // 2. Danger zone + glowing danger line, ~2 rows above the floor (moves with the arena)
        val dangerY = oy + dangerRowFor(m.rows) * m.cellH
        val zonePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            shader = LinearGradient(
                0f, dangerY, 0f, oy + ah,
                intArrayOf(0x2EEF4444, 0x0AEF4444),
                null,
                Shader.TileMode.CLAMP
            )
        }
        c.save()
        c.clipRect(ox, dangerY, ox + aw, oy + ah)
        c.drawRoundRect(ox, oy, ox + aw, oy + ah, corner, corner, zonePaint)
        c.restore()
        val dangerGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = 0x55FF3B3B
            strokeWidth = 7f * density
            strokeCap = Paint.Cap.ROUND
            maskFilter = android.graphics.BlurMaskFilter(4f * density, android.graphics.BlurMaskFilter.Blur.NORMAL)
        }
        c.drawLine(ox + 10f, dangerY, ox + aw - 10f, dangerY, dangerGlowPaint)
        val dangerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = 0xF2FF4D4D.toInt()
            strokeWidth = 2.2f * density
            strokeCap = Paint.Cap.ROUND
            pathEffect = DashPathEffect(floatArrayOf(14f * density, 8f * density), 0f)
        }
        c.drawLine(ox + 10f, dangerY, ox + aw - 10f, dangerY, dangerPaint)

        // 3. Outer glowing neon cyan -> violet -> magenta frame (baked ONCE!)
        val gradient = LinearGradient(
            0f, oy, 0f, oy + ah,
            intArrayOf(0xFF38BDF8.toInt(), 0xFFA855F7.toInt(), 0xFFE879F9.toInt()),
            null,
            Shader.TileMode.CLAMP
        )
        val glowBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            shader = gradient
            strokeWidth = 7.5f * density
            alpha = 68
        }
        c.drawRoundRect(ox, oy, ox + aw, oy + ah, corner, corner, glowBorderPaint)

        val crispBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            shader = gradient
            strokeWidth = 2.2f * density
        }
        c.drawRoundRect(ox, oy, ox + aw, oy + ah, corner, corner, crispBorderPaint)

        arenaFrameBitmap = bmp
    }

    fun ensureBallSprite(skin: BallSkin, ballRadiusPx: Float): Bitmap {
        val rInt = ceil(ballRadiusPx).toInt().coerceAtLeast(4)
        val existing = ballSpriteBitmap
        if (existing != null && cachedSpriteSkin == skin && cachedSpriteRadiusPx == rInt) {
            return existing
        }
        existing?.recycle()

        val size = (rInt * 5).coerceAtLeast(16)
        val half = size * 0.5f
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val c = AndroidCanvas(bmp)
        val r = ballRadiusPx

        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        val corePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

        when (skin) {
            BallSkin.NEON_PEARL -> {
                glowPaint.shader = RadialGradient(
                    half, half, r * 2.15f,
                    intArrayOf(0xD8E879F9.toInt(), 0x5CA855F7, 0x00A855F7),
                    floatArrayOf(0f, 0.52f, 1f),
                    Shader.TileMode.CLAMP
                )
                c.drawCircle(half, half, r * 2.15f, glowPaint)
                corePaint.color = 0xFFF5D0FE.toInt()
                c.drawCircle(half, half, r * 1.05f, corePaint)
                corePaint.color = android.graphics.Color.WHITE
                c.drawCircle(half, half, r * 0.72f, corePaint)
            }

            BallSkin.STARLIGHT -> {
                glowPaint.shader = RadialGradient(
                    half, half, r * 2.25f,
                    intArrayOf(0xD838BDF8.toInt(), 0x4C0284C7, 0x000284C7),
                    floatArrayOf(0f, 0.55f, 1f),
                    Shader.TileMode.CLAMP
                )
                c.drawCircle(half, half, r * 2.25f, glowPaint)

                val starPath = AndroidPath()
                val step = (PI / 5.0).toFloat()
                val start = -(PI / 2.0).toFloat()
                val outerR = r * 1.32f
                val innerR = r * 0.58f
                for (i in 0 until 10) {
                    val rad = if (i % 2 == 0) outerR else innerR
                    val a = start + i * step
                    val sx = half + cos(a) * rad
                    val sy = half + sin(a) * rad
                    if (i == 0) starPath.moveTo(sx, sy) else starPath.lineTo(sx, sy)
                }
                starPath.close()

                corePaint.color = 0xFF7DD3FC.toInt()
                c.drawPath(starPath, corePaint)
                val strokeP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    color = 0xFF38BDF8.toInt()
                    strokeWidth = r * 0.24f
                }
                c.drawPath(starPath, strokeP)
                corePaint.color = android.graphics.Color.WHITE
                c.drawCircle(half, half, r * 0.55f, corePaint)
            }

            BallSkin.SOLAR_FIRE -> {
                glowPaint.shader = RadialGradient(
                    half, half, r * 2.15f,
                    intArrayOf(0xE0FF7A1A.toInt(), 0x5CEF4444, 0x00EF4444),
                    floatArrayOf(0f, 0.55f, 1f),
                    Shader.TileMode.CLAMP
                )
                c.drawCircle(half, half, r * 2.15f, glowPaint)
                corePaint.color = 0xFFFACC15.toInt()
                c.drawCircle(half, half, r * 1.05f, corePaint)
                corePaint.color = 0xFFFFFBEB.toInt()
                c.drawCircle(half, half, r * 0.66f, corePaint)
            }

            BallSkin.CYBER_MINT -> {
                glowPaint.shader = RadialGradient(
                    half, half, r * 2.15f,
                    intArrayOf(0xE034D399.toInt(), 0x5206B6D4, 0x0006B6D4),
                    floatArrayOf(0f, 0.55f, 1f),
                    Shader.TileMode.CLAMP
                )
                c.drawCircle(half, half, r * 2.15f, glowPaint)
                corePaint.color = 0xFF6EE7B7.toInt()
                c.drawCircle(half, half, r * 1.05f, corePaint)
                corePaint.color = android.graphics.Color.WHITE
                c.drawCircle(half, half, r * 0.68f, corePaint)
            }

            else -> {
                // Generic glow skin driven by the skin's colors
                val glowRgb = skin.glowArgb and 0x00FFFFFFL
                glowPaint.shader = RadialGradient(
                    half, half, r * 2.15f,
                    intArrayOf(
                        (glowRgb or 0xE0000000L).toInt(),
                        (glowRgb or 0x58000000L).toInt(),
                        glowRgb.toInt()
                    ),
                    floatArrayOf(0f, 0.55f, 1f),
                    Shader.TileMode.CLAMP
                )
                c.drawCircle(half, half, r * 2.15f, glowPaint)
                corePaint.color = skin.ringArgb.toInt()
                c.drawCircle(half, half, r * 1.05f, corePaint)
                corePaint.color = skin.coreArgb.toInt()
                c.drawCircle(half, half, r * 0.68f, corePaint)
            }
        }

        ballSpriteBitmap = bmp
        ballSpriteHalfSize = half
        cachedSpriteSkin = skin
        cachedSpriteRadiusPx = rInt
        return bmp
    }
}

fun calculateArenaMetrics(width: Float, height: Float): ArenaMetrics {
    // Small margins so the arena uses (almost) the full screen width on phones.
    val marginX = width * 0.012f
    val marginY = height * 0.01f
    val availW = (width - marginX * 2f).coerceAtLeast(1f)
    val availH = (height - marginY * 2f).coerceAtLeast(1f)
    // Square cells using the full width; tall screens get extra rows (12 up to MAX_ARENA_ROWS)
    // so the arena fills the height. Wide screens / tablets: 12 rows, height decides the size.
    val cellW = min(availW / ARENA_COLS, availH / ARENA_ROWS)
    val cellH = cellW
    val rows = (availH / cellH).coerceIn(ARENA_ROWS.toFloat(), MAX_ARENA_ROWS)
    val arenaW = cellW * ARENA_COLS
    val arenaH = cellH * rows
    return ArenaMetrics(
        offsetX = (width - arenaW) / 2f,
        offsetY = (height - arenaH) / 2f,
        cellSize = cellW,
        arenaWidth = arenaW,
        arenaHeight = arenaH,
        cellW = cellW,
        cellH = cellH,
        rows = rows
    )
}

/**
 * High-performance Canvas-based Brick-Breaker Game Engine.
 *
 * Uses VSync-aligned [withFrameNanos] stepping with real-time sub-steps (no slow motion on frame drops),
 * pre-baked static arena & neon ball sprite Bitmaps, single-pass batched native Canvas rendering,
 * cached HP/count Strings (zero GC pauses), and draw-phase-only [frameTick] invalidation.
 */
class GameEngine(
    val mode: BrickQuestMode,
    val stageNumber: Int
) {
    val simulation = BrickQuestEngine(mode = mode, stageNumber = stageNumber)
    val frameTick: MutableLongState = mutableLongStateOf(0L)

    private val renderCache = NativeRenderCache()
    private var lastFrameNanos: Long = -1L

    private var lastHitCount: Int = 0
    private var lastBreakCount: Int = 0
    private var lastLaserCount: Int = 0
    private var lastAudioMillis: Long = 0L

    val score: Int get() = simulation.score
    val starProgressRatio: Float get() = simulation.starProgressRatio
    val currentStarCount: Int get() = simulation.currentStarCount
    val phase: TurnPhase get() = simulation.phase
    val turboSpeed: Boolean get() = simulation.turboSpeed
    val turnNumber: Int get() = simulation.turnNumber
    val turnsUntilDanger: Int get() = simulation.turnsUntilDanger
    val isDraggingAim: Boolean get() = simulation.isDraggingAim
    val multiBallTimer: Float get() = simulation.multiBallTimer
    val laserBeamTimer: Float get() = simulation.laserBeamTimer
    val paddleExpandTimer: Float get() = simulation.paddleExpandTimer

    fun resetFrameClock() {
        lastFrameNanos = -1L
    }

    /**
     * Advances the simulation by the real time elapsed since the previous VSync frame.
     *
     * A long frame (phone dropped to 30 or 20 FPS) is split into equal sub-steps of at most
     * [MAX_SIM_STEP_SEC], so the game keeps its real speed instead of going into slow motion.
     * At a steady 60 FPS this is exactly one update per frame, same as before. On 90/120 Hz
     * screens each frame simply gets a smaller dt.
     */
    fun stepFrameNanos(
        frameTimeNanos: Long,
        onAudioCue: (GameAudioCue) -> Unit
    ) {
        if (lastFrameNanos < 0L) {
            lastFrameNanos = frameTimeNanos
            return
        }
        val frameDtSec = ((frameTimeNanos - lastFrameNanos) / 1_000_000_000f)
            .coerceIn(0f, MAX_FRAME_DT_SEC)
        lastFrameNanos = frameTimeNanos
        if (frameDtSec <= 0f) return
        val frameTimeMillis = frameTimeNanos / 1_000_000L

        val simSteps = ceil(frameDtSec / MAX_SIM_STEP_SEC).toInt().coerceIn(1, MAX_SIM_STEPS_PER_FRAME)
        val stepDt = frameDtSec / simSteps
        for (s in 0 until simSteps) {
            simulation.update(stepDt)
        }

        // Emit throttled non-blocking audio/haptic cues
        val laserDelta = simulation.laserSoundCounter - lastLaserCount
        val breakDelta = simulation.breakSoundCounter - lastBreakCount
        val hitDelta = simulation.hitSoundCounter - lastHitCount
        if (frameTimeMillis - lastAudioMillis >= AUDIO_THROTTLE_MS) {
            when {
                laserDelta > 0 -> {
                    onAudioCue(GameAudioCue.LASER_OR_PICKUP)
                    lastAudioMillis = frameTimeMillis
                }
                breakDelta > 0 -> {
                    onAudioCue(GameAudioCue.BRICK_BREAK)
                    lastAudioMillis = frameTimeMillis
                }
                hitDelta > 0 -> {
                    onAudioCue(GameAudioCue.HIT)
                    lastAudioMillis = frameTimeMillis
                }
            }
        }
        lastHitCount = simulation.hitSoundCounter
        lastBreakCount = simulation.breakSoundCounter
        lastLaserCount = simulation.laserSoundCounter

        // Invalidate ONLY the Canvas draw phase
        frameTick.longValue++
    }

    fun updateAimFromCanvasPoint(
        posX: Float,
        posY: Float,
        canvasWidth: Float,
        canvasHeight: Float
    ) {
        if (canvasWidth <= 1f || canvasHeight <= 1f) return
        val m = calculateArenaMetrics(canvasWidth, canvasHeight)
        val normX = (posX - m.offsetX) / m.cellW
        val normY = (posY - m.offsetY) / m.cellH
        simulation.updateAimFromPointer(normX, normY)
        frameTick.longValue++
    }

    fun movePaddleFromCanvasPoint(
        posX: Float,
        canvasWidth: Float,
        canvasHeight: Float
    ) {
        if (canvasWidth <= 1f || canvasHeight <= 1f) return
        val m = calculateArenaMetrics(canvasWidth, canvasHeight)
        val normX = (posX - m.offsetX) / m.cellW
        simulation.updatePaddlePosition(normX)
        frameTick.longValue++
    }

    fun fireVolley() {
        simulation.fireVolley()
        frameTick.longValue++
    }

    fun toggleTurbo(): Boolean {
        if (simulation.phase != TurnPhase.SHOOTING) return false
        simulation.turboSpeed = !simulation.turboSpeed
        frameTick.longValue++
        return true
    }

    fun recallBalls() {
        simulation.recallBallsNow()
        frameTick.longValue++
    }

    fun triggerLightningRowBlast(): Boolean {
        val applied = simulation.triggerLightningRowBlast()
        if (applied) frameTick.longValue++
        return applied
    }

    fun triggerExtraBallsStorm(): Boolean {
        val applied = simulation.triggerExtraBallsStorm()
        if (applied) frameTick.longValue++
        return applied
    }

    /**
     * Ultra-fast single-pass batched native Canvas renderer (pre-baked frame + ball sprites,
     * zero per-frame allocations, zero String GC).
     */
    fun DrawScope.render(skin: BallSkin) {
        val cache = renderCache
        val metrics = cache.ensureLayout(size.width, size.height, density)
        // Keep the physics floor in sync with the visible arena height (no-op if unchanged).
        simulation.setArenaRows(metrics.rows)
        val ox = metrics.offsetX
        val oy = metrics.offsetY
        val cs = metrics.cellSize
        val cw = metrics.cellW
        val ch = metrics.cellH
        val aw = metrics.arenaWidth
        val ah = metrics.arenaHeight
        val ballRadiusPx = BALL_RADIUS * cs
        val ballSprite = cache.ensureBallSprite(skin, ballRadiusPx)
        val spriteHalf = cache.ballSpriteHalfSize

        drawIntoCanvas { composeCanvas ->
            val nc = composeCanvas.nativeCanvas

            // 1. Pre-baked Arena Backdrop + Danger Dashed Line + Neon Outer Border (1 single blit!)
            cache.arenaFrameBitmap?.let { frameBmp ->
                nc.drawBitmap(frameBmp, cache.arenaFrameLeft, cache.arenaFrameTop, null)
            }

            // 2. Glowing Neon Bricks + Cached HP Text
            val brickFillPaint = cache.brickFillPaint
            val brickStrokePaint = cache.brickStrokePaint
            val brickHighlightPaint = cache.brickHighlightPaint
            val bombBadgePaint = cache.bombBadgePaint
            val brickHpPaint = cache.brickHpPaint
            val corner = cache.brickCornerPx
            val baseHpTextSize = cache.hpTextSize
            val bricks = simulation.bricks

            for (i in 0 until bricks.size) {
                val brick = bricks[i]
                val baseRgb = BrickQuestEngine.brickRgbForHp(brick.hp, brick.special)
                val left = ox + brick.left * cw
                val top = oy + brick.top * ch
                val right = ox + brick.right * cw
                val bottom = oy + brick.bottom * ch
                val bw = right - left
                val bh = bottom - top

                val fillAlpha = ((0.28f + brick.hitFlash * 0.42f).coerceIn(0f, 1f) * 255f).toInt()
                brickFillPaint.color = (fillAlpha shl 24) or baseRgb
                nc.drawRoundRect(left, top, right, bottom, corner, corner, brickFillPaint)

                brickStrokePaint.color = if (brick.hitFlash > 0.35f) {
                    android.graphics.Color.WHITE
                } else {
                    (0xFF shl 24) or baseRgb
                }
                nc.drawRoundRect(left, top, right, bottom, corner, corner, brickStrokePaint)

                // Glossy top highlight
                val hy = top + bh * 0.11f
                nc.drawLine(left + bw * 0.18f, hy, left + bw * 0.82f, hy, brickHighlightPaint)

                if (brick.special == BrickSpecial.BOMB_3X3) {
                    nc.drawCircle(left + bw * 0.5f, top + bh * 0.5f, bw * 0.36f, bombBadgePaint)
                } else if (brick.special != BrickSpecial.NONE) {
                    // Distinct neon corner icon on special power-up bricks
                    val badgeLabel = when (brick.special) {
                        BrickSpecial.POWER_MULTI_BALL -> "×2"
                        BrickSpecial.POWER_LASER_BEAM -> "⚡"
                        BrickSpecial.POWER_PADDLE_EXPAND -> "↔"
                        else -> ""
                    }
                    cache.badgePaint.textSize = (cs * 0.22f).coerceIn(11f, 22f)
                    cache.badgePaint.color = android.graphics.Color.WHITE
                    nc.drawText(badgeLabel, right - bw * 0.22f, top + bh * 0.31f, cache.badgePaint)
                }

                // 3-digit HP gets a smaller font so it always fits inside the brick
                val hpSize = if (brick.hp >= 100) baseHpTextSize * 0.78f else baseHpTextSize
                if (brickHpPaint.textSize != hpSize) brickHpPaint.textSize = hpSize
                nc.drawText(
                    hpString(brick.hp),
                    left + bw * 0.5f,
                    top + bh * 0.5f + hpSize * 0.36f,
                    brickHpPaint
                )
            }

            // 2b. Falling Collectable Power-Up Capsules (Multi-Ball, Laser Beam, Paddle Expand)
            val fallingPowerUps = simulation.fallingPowerUps
            if (fallingPowerUps.isNotEmpty()) {
                val capHalfW = cs * 0.42f
                val capHalfH = cs * 0.24f
                val capCorner = capHalfH
                val pFill = cache.pickupFillPaint
                val pStroke = cache.pickupStrokePaint
                cache.badgePaint.textSize = (cs * 0.27f).coerceIn(13f, 26f)
                val capTextOffset = -(cache.badgePaint.descent() + cache.badgePaint.ascent()) * 0.5f
                for (i in 0 until fallingPowerUps.size) {
                    val fp = fallingPowerUps[i]
                    val cx = ox + fp.x * cw
                    val cy = oy + fp.y * ch
                    val pulse = 1f + 0.08f * sin(fp.pulsePhase)
                    val hw = capHalfW * pulse
                    val hh = capHalfH * pulse
                    val rgb = when (fp.kind) {
                        PowerUpType.MULTI_BALL -> 0x00EC4899
                        PowerUpType.LASER_BEAM -> 0x0022C55E
                        PowerUpType.PADDLE_EXPAND -> 0x0038BDF8
                    }
                    val sym = when (fp.kind) {
                        PowerUpType.MULTI_BALL -> "×2"
                        PowerUpType.LASER_BEAM -> "⚡"
                        PowerUpType.PADDLE_EXPAND -> "↔"
                    }
                    // Soft neon outer aura
                    pFill.color = (0x48 shl 24) or rgb
                    nc.drawRoundRect(cx - hw * 1.25f, cy - hh * 1.32f, cx + hw * 1.25f, cy + hh * 1.32f, capCorner * 1.3f, capCorner * 1.3f, pFill)
                    // Vibrant capsule body
                    pFill.color = (0xE6 shl 24) or rgb
                    nc.drawRoundRect(cx - hw, cy - hh, cx + hw, cy + hh, capCorner, capCorner, pFill)
                    // Crisp white border
                    pStroke.strokeWidth = 2.0f * density
                    pStroke.color = android.graphics.Color.WHITE
                    nc.drawRoundRect(cx - hw, cy - hh, cx + hw, cy + hh, capCorner, capCorner, pStroke)
                    // Bold white power-up glyph
                    cache.badgePaint.color = android.graphics.Color.WHITE
                    nc.drawText(sym, cx, cy + capTextOffset, cache.badgePaint)
                }
            }

            // 3. Pickups (+1 Magenta Orbs & Neon Green/Cyan Laser Orbs)
            val pickupFill = cache.pickupFillPaint
            val pickupStroke = cache.pickupStrokePaint
            val pickups = simulation.pickups
            val basePickupR = cs * 0.26f
            val basePickupStrokeW = (cs * 0.062f).coerceAtLeast(2.0f)
            val pickupIconStrokeW = cs * 0.08f

            for (i in 0 until pickups.size) {
                val pickup = pickups[i]
                if (pickup.collected) continue
                val cx = ox + pickup.cx * cw
                val cy = oy + pickup.cy * ch
                val pulse = 1f + 0.13f * sin(pickup.pulsePhase)

                when (pickup.kind) {
                    PickupKind.PLUS_BALL -> {
                        pickupFill.color = 0x40E879F9
                        nc.drawCircle(cx, cy, basePickupR * 1.6f * pulse, pickupFill)
                        pickupStroke.strokeWidth = basePickupStrokeW
                        pickupStroke.color = 0xFFE879F9.toInt()
                        nc.drawCircle(cx, cy, basePickupR * 1.12f * pulse, pickupStroke)
                        pickupFill.color = android.graphics.Color.WHITE
                        nc.drawCircle(cx, cy, basePickupR * 0.55f, pickupFill)
                    }
                    PickupKind.LASER_HORIZONTAL -> {
                        pickupFill.color = 0x384ADE80
                        nc.drawCircle(cx, cy, basePickupR * 1.55f * pulse, pickupFill)
                        pickupStroke.strokeWidth = basePickupStrokeW
                        pickupStroke.color = 0xFF4ADE80.toInt()
                        nc.drawCircle(cx, cy, basePickupR * 1.10f, pickupStroke)
                        pickupStroke.strokeWidth = pickupIconStrokeW
                        pickupStroke.color = android.graphics.Color.WHITE
                        nc.drawLine(cx - basePickupR * 0.72f, cy, cx + basePickupR * 0.72f, cy, pickupStroke)
                    }
                    PickupKind.LASER_VERTICAL -> {
                        pickupFill.color = 0x3838BDF8
                        nc.drawCircle(cx, cy, basePickupR * 1.55f * pulse, pickupFill)
                        pickupStroke.strokeWidth = basePickupStrokeW
                        pickupStroke.color = 0xFF38BDF8.toInt()
                        nc.drawCircle(cx, cy, basePickupR * 1.10f, pickupStroke)
                        pickupStroke.strokeWidth = pickupIconStrokeW
                        pickupStroke.color = android.graphics.Color.WHITE
                        nc.drawLine(cx, cy - basePickupR * 0.72f, cx, cy + basePickupR * 0.72f, pickupStroke)
                    }
                    PickupKind.LASER_CROSS -> {
                        pickupFill.color = 0x3DFACC15
                        nc.drawCircle(cx, cy, basePickupR * 1.6f * pulse, pickupFill)
                        pickupStroke.strokeWidth = basePickupStrokeW
                        pickupStroke.color = 0xFFFACC15.toInt()
                        nc.drawCircle(cx, cy, basePickupR * 1.12f, pickupStroke)
                        pickupStroke.strokeWidth = pickupIconStrokeW
                        pickupStroke.color = android.graphics.Color.WHITE
                        nc.drawLine(cx - basePickupR * 0.65f, cy, cx + basePickupR * 0.65f, cy, pickupStroke)
                        nc.drawLine(cx, cy - basePickupR * 0.65f, cx, cy + basePickupR * 0.65f, pickupStroke)
                    }
                }
            }

            // 4. Blazing Horizontal & Vertical Laser Beams
            val beams = simulation.laserBeams
            val laserPaint = cache.laserPaint
            for (i in 0 until beams.size) {
                val beam = beams[i]
                val a = beam.life.coerceIn(0f, 1f)
                val beamRgb = beam.rgb and 0x00FFFFFF
                val x1: Float
                val y1: Float
                val x2: Float
                val y2: Float
                if (beam.isHorizontal) {
                    val y = oy + beam.gridCoord * ch
                    x1 = ox + 4f; y1 = y; x2 = ox + aw - 4f; y2 = y
                } else {
                    val x = ox + beam.gridCoord * cw
                    x1 = x; y1 = oy + 4f; x2 = x; y2 = oy + ah - 4f
                }
                laserPaint.strokeWidth = cs * 0.54f
                laserPaint.color = (((0.28f * a) * 255f).toInt() shl 24) or beamRgb
                nc.drawLine(x1, y1, x2, y2, laserPaint)

                laserPaint.strokeWidth = cs * 0.22f
                laserPaint.color = (((0.85f * a) * 255f).toInt() shl 24) or beamRgb
                nc.drawLine(x1, y1, x2, y2, laserPaint)

                laserPaint.strokeWidth = cs * 0.085f
                laserPaint.color = (((0.95f * a) * 255f).toInt() shl 24) or 0x00FFFFFF
                nc.drawLine(x1, y1, x2, y2, laserPaint)
            }

            // 5. Bursting Translucent Neon Square Particles
            val particles = simulation.particles
            val pFill = cache.particleFillPaint
            val pStroke = cache.particleStrokePaint
            for (i in 0 until particles.size) {
                val p = particles[i]
                val px = ox + p.x * cw
                val py = oy + p.y * ch
                val halfPs = p.size * cs * 0.5f
                val a = p.alpha.coerceIn(0f, 1f)
                val pRgb = p.rgb

                pFill.color = (((a * 0.24f) * 255f).toInt() shl 24) or pRgb
                nc.drawRect(px - halfPs, py - halfPs, px + halfPs, py + halfPs, pFill)

                pStroke.color = (((a * 0.90f) * 255f).toInt() shl 24) or pRgb
                nc.drawRect(px - halfPs, py - halfPs, px + halfPs, py + halfPs, pStroke)
            }

            // 6. Dotted Neon Aim Trajectory Preview
            if (simulation.phase == TurnPhase.AIMING && simulation.isDraggingAim) {
                simulation.refreshAimPreviewIfNeeded()
                val count = simulation.aimPreviewCount
                val invCount = 1f / count.coerceAtLeast(1)
                val glowR = cs * 0.125f
                val coreR = cs * 0.066f
                val dotPaint = cache.aimDotPaint
                for (i in 0 until count) {
                    val fade = (1f - i * invCount) * 0.9f
                    val dx = ox + simulation.aimPreviewX[i] * cw
                    val dy = oy + simulation.aimPreviewY[i] * ch
                    dotPaint.color = (((fade * 0.35f) * 255f).toInt() shl 24) or 0x0038BDF8
                    nc.drawCircle(dx, dy, glowR, dotPaint)
                    dotPaint.color = ((fade * 255f).toInt() shl 24) or 0x00FFFFFF
                    nc.drawCircle(dx, dy, coreR, dotPaint)
                }
            }

            // 7. Compulsory Interactive Glowing Neon Cyan Paddle Bar (always visible & active!)
            run {
                val bounce = simulation.paddleBounceAnim
                val launchPulse = simulation.paddleLaunchPulse
                val px = ox + simulation.paddleX * cw
                // Smooth elastic spring dip when a ball rebounds off the paddle
                val py = oy + simulation.paddleY * ch + bounce * cs * 0.09f
                val pHalfW = simulation.paddleWidth * 0.5f * cw + bounce * cs * 0.05f
                val pHalfH = cs * 0.145f
                val pCorner = pHalfH
                val isExpanded = simulation.paddleExpandTimer > 0f
                val isLaser = simulation.laserBeamTimer > 0f
                val paddleRgb = when {
                    isLaser -> 0x004ADE80
                    isExpanded -> 0x0022D3EE
                    else -> 0x0038BDF8
                }
                val pf = cache.paddleFillPaint
                val ps = cache.paddleStrokePaint
                // Wide subtle outer horizontal neon glow aura (brightens on catch/launch!)
                val auraAlpha = ((if (isExpanded) 0x48 else 0x32) + (bounce * 68f + launchPulse * 34f).toInt()).coerceAtMost(0x98)
                val auraPadX = 8f + bounce * 6f
                val auraPadY = 4f + bounce * 3f
                pf.color = (auraAlpha shl 24) or paddleRgb
                nc.drawRoundRect(
                    px - pHalfW - auraPadX,
                    py - pHalfH - auraPadY,
                    px + pHalfW + auraPadX,
                    py + pHalfH + auraPadY,
                    pCorner + 5f,
                    pCorner + 5f,
                    pf
                )
                // Glossy solid neon cyan paddle bar
                pf.color = (0xEE shl 24) or paddleRgb
                nc.drawRoundRect(px - pHalfW, py - pHalfH, px + pHalfW, py + pHalfH, pCorner, pCorner, pf)
                // Bright glossy top sheen strip
                pf.color = if (bounce > 0.25f) 0xE6FFFFFF.toInt() else 0x99E0F2FE.toInt()
                nc.drawRoundRect(
                    px - pHalfW * 0.86f,
                    py - pHalfH * 0.72f,
                    px + pHalfW * 0.86f,
                    py - pHalfH * 0.12f,
                    pCorner * 0.6f,
                    pCorner * 0.6f,
                    pf
                )
                // Crisp glowing rim
                ps.strokeWidth = (if (isExpanded || bounce > 0.2f) 2.3f else 1.8f) * density
                ps.color = if (isExpanded) 0xFFFDE047.toInt() else 0xFFE0F2FE.toInt()
                nc.drawRoundRect(px - pHalfW, py - pHalfH, px + pHalfW, py + pHalfH, pCorner, pCorner, ps)
                // Twin neon-green laser emitter tips when LASER_BEAM bonus is active
                if (isLaser) {
                    pf.color = 0xFF4ADE80.toInt()
                    val tipR = cs * 0.11f
                    nc.drawCircle(px - pHalfW * 0.72f, py - pHalfH, tipR, pf)
                    nc.drawCircle(px + pHalfW * 0.72f, py - pHalfH, tipR, pf)
                    pf.color = android.graphics.Color.WHITE
                    nc.drawCircle(px - pHalfW * 0.72f, py - pHalfH, tipR * 0.52f, pf)
                    nc.drawCircle(px + pHalfW * 0.72f, py - pHalfH, tipR * 0.52f, pf)
                }
            }

            // 8. Flying Balls with Silky Glowing Comet Trails & Floor Launch Anchor
            val balls = simulation.balls
            val bmpPaint = cache.bitmapPaint
            val trailPaint = cache.aimDotPaint
            val trailRgb = (skin.glowArgb and 0x00FFFFFFL).toInt()
            for (i in 0 until balls.size) {
                val ball = balls[i]
                if (!ball.active) continue
                val tc = ball.trailCount
                if (tc > 0 && !ball.gathering) {
                    for (t in tc - 1 downTo 0) {
                        val frac = 1f - (t + 1) / (tc + 1f)
                        val alpha = (frac * frac * 115f).toInt().coerceIn(8, 120)
                        trailPaint.color = (alpha shl 24) or trailRgb
                        nc.drawCircle(
                            ox + ball.trailX[t] * cw,
                            oy + ball.trailY[t] * ch,
                            ballRadiusPx * (0.45f + frac * 0.52f),
                            trailPaint
                        )
                    }
                }
                nc.drawBitmap(
                    ballSprite,
                    ox + ball.x * cw - spriteHalf,
                    oy + ball.y * ch - spriteHalf,
                    bmpPaint
                )
            }

            val showAnchorBall = simulation.phase == TurnPhase.AIMING ||
                simulation.phase == TurnPhase.DROPPING ||
                simulation.ballsRemainingToSpawn > 0 ||
                simulation.nextLaunchX != null
            val anchorX = ox + (simulation.nextLaunchX ?: simulation.launchX) * cw
            val anchorY = oy + simulation.launchY * ch
            if (showAnchorBall) {
                nc.drawBitmap(ballSprite, anchorX - spriteHalf, anchorY - spriteHalf, bmpPaint)
            }

            val labelPaint = cache.labelPaint
            val countVal = simulation.displayedBallCount
            if (countVal > 0) {
                labelPaint.textSize = (cs * 0.30f).coerceIn(14f, 30f)
                labelPaint.color = android.graphics.Color.WHITE
                val labelX = (anchorX + cs * 0.56f).coerceIn(ox + cs * 0.6f, ox + aw - cs * 0.6f)
                nc.drawText(
                    ballCountString(countVal),
                    labelX,
                    anchorY + labelPaint.textSize * 0.32f,
                    labelPaint
                )
            }

            // 8. Floating Feedback Texts
            val floats = simulation.floatingTexts
            for (i in 0 until floats.size) {
                val ft = floats[i]
                val life = ft.life.coerceIn(0f, 1f)
                labelPaint.textSize = (cs * 0.34f).coerceIn(18f, 34f)
                val ftRgb = ft.rgb and 0x00FFFFFF
                labelPaint.color = ((life * 255f).toInt() shl 24) or ftRgb
                nc.drawText(ft.text, ox + ft.x * cw, oy + ft.y * ch, labelPaint)
            }
        }
    }

    companion object {
        // Longer gaps (app resumed, huge hitch) are capped so balls don't teleport through bricks.
        private const val MAX_FRAME_DT_SEC = 0.1f
        // Largest dt one simulation update may take (matches the old 60 FPS behaviour).
        private const val MAX_SIM_STEP_SEC = 1f / 60f
        private const val MAX_SIM_STEPS_PER_FRAME = 6
        private const val AUDIO_THROTTLE_MS = 95L
    }
}

/**
 * Core 60 FPS game loop driven by Compose [LaunchedEffect] and [withFrameNanos].
 * Steps [engine] in lockstep with VSync and triggers [onFrameSynced] with the frame timestamp
 * so HUD updates can be smoothly throttled without per-frame UI recompositions.
 */
@Composable
fun GameEngineLoop(
    engine: GameEngine,
    isPaused: Boolean,
    onAudioCue: (GameAudioCue) -> Unit,
    onFrameSynced: (frameTimeMillis: Long) -> Unit
) {
    // Always call the latest callbacks (e.g. after sound is toggled) without restarting the loop.
    val latestAudioCue by rememberUpdatedState(onAudioCue)
    val latestFrameSynced by rememberUpdatedState(onFrameSynced)
    LaunchedEffect(engine, isPaused) {
        if (isPaused) {
            engine.resetFrameClock()
            return@LaunchedEffect
        }
        engine.resetFrameClock()
        while (true) {
            withFrameNanos { frameTimeNanos ->
                engine.stepFrameNanos(
                    frameTimeNanos = frameTimeNanos,
                    onAudioCue = latestAudioCue
                )
                latestFrameSynced(frameTimeNanos / 1_000_000L)
            }
        }
    }
}

/**
 * High-performance Compose Canvas viewport bound to [GameEngine].
 * Reads `engine.frameTick.longValue` strictly inside the Canvas draw lambda so only
 * the single-pass native Canvas blit runs at 60 FPS with zero Compose recompositions.
 */
@Composable
fun BrickBreakerCanvas(
    engine: GameEngine,
    skin: BallSkin,
    isPaused: Boolean,
    onAimStarted: () -> Unit,
    onShotFired: () -> Unit,
    modifier: Modifier = Modifier
) {
    var canvasSize by remember { mutableStateOf(IntSize(1, 1)) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            // Own render layer: the 60 FPS redraw re-records only the arena,
            // not the HUD, buttons and gradient background around it.
            .graphicsLayer { }
            .onSizeChanged { canvasSize = it }
            .pointerInput(engine, isPaused) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    if (isPaused) return@awaitEachGesture

                    val pointerId = down.id
                    if (engine.phase == TurnPhase.AIMING) {
                        onAimStarted()
                        engine.updateAimFromCanvasPoint(
                            posX = down.position.x,
                            posY = down.position.y,
                            canvasWidth = canvasSize.width.toFloat(),
                            canvasHeight = canvasSize.height.toFloat()
                        )
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                            if (!change.pressed) {
                                engine.fireVolley()
                                onShotFired()
                                break
                            } else {
                                engine.updateAimFromCanvasPoint(
                                    posX = change.position.x,
                                    posY = change.position.y,
                                    canvasWidth = canvasSize.width.toFloat(),
                                    canvasHeight = canvasSize.height.toFloat()
                                )
                                change.consume()
                            }
                        }
                    } else if (engine.phase == TurnPhase.SHOOTING || engine.phase == TurnPhase.DROPPING) {
                        // Slide the compulsory glowing neon paddle smoothly to bounce balls & catch power-ups!
                        engine.movePaddleFromCanvasPoint(
                            posX = down.position.x,
                            canvasWidth = canvasSize.width.toFloat(),
                            canvasHeight = canvasSize.height.toFloat()
                        )
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                            if (!change.pressed) break
                            engine.movePaddleFromCanvasPoint(
                                posX = change.position.x,
                                canvasWidth = canvasSize.width.toFloat(),
                                canvasHeight = canvasSize.height.toFloat()
                            )
                            change.consume()
                        }
                    }
                }
            }
            .testTag("game_play_arena")
    ) {
        // Draw-phase read of frameTick -> locked 60 FPS rendering with zero recompositions!
        @Suppress("UNUSED_VARIABLE")
        val tick = engine.frameTick.longValue
        with(engine) {
            render(skin = skin)
        }
    }
}
