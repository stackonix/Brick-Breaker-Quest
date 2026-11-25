package com.example.brickquest.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brickquest.data.BrickQuestProgress
import com.example.brickquest.data.TOTAL_QUEST_STAGES
import com.example.brickquest.engine.BrickBreakerCanvas
import com.example.brickquest.engine.BrickQuestMode
import com.example.brickquest.engine.GameAudioCue
import com.example.brickquest.engine.GameEngine
import com.example.brickquest.engine.GameEngineLoop
import com.example.brickquest.engine.TurnPhase
import com.example.data.SoundHapticManager
import kotlin.math.abs

@Composable
fun BrickQuestGameScreen(
    mode: BrickQuestMode,
    stageNumber: Int,
    sessionNonce: Long,
    progress: BrickQuestProgress,
    soundHaptic: SoundHapticManager,
    onUseLightning: (onApplied: () -> Boolean) -> Unit,
    onUseExtraBalls: (onApplied: () -> Boolean) -> Unit,
    onStageCleared: (stage: Int, stars: Int) -> Unit,
    onEndlessFinished: (score: Int) -> Unit,
    onStageGameOver: () -> Unit,
    onNextStage: () -> Unit,
    onRestart: () -> Unit,
    onOpenStages: () -> Unit,
    onGoHome: () -> Unit,
    onToggleSound: () -> Unit,
    onToggleHaptics: () -> Unit
) {
    val gameEngine = remember(mode, stageNumber, sessionNonce) {
        GameEngine(mode = mode, stageNumber = stageNumber)
    }

    var isPaused by remember(sessionNonce) { mutableStateOf(false) }
    var outcomeHandled by remember(sessionNonce) { mutableStateOf(false) }

    // HUD states ONLY mutate when values actually change (and score/bar throttled to ~9 Hz!)
    var hudScore by remember(sessionNonce) { mutableIntStateOf(0) }
    var hudStarRatio by remember(sessionNonce) { mutableFloatStateOf(0f) }
    var hudStarCount by remember(sessionNonce) { mutableIntStateOf(0) }
    var hudPhase by remember(sessionNonce) { mutableStateOf(TurnPhase.AIMING) }
    var hudTurbo by remember(sessionNonce) { mutableStateOf(false) }
    var hudTurnNumber by remember(sessionNonce) { mutableIntStateOf(1) }
    var showDragHint by remember(sessionNonce) { mutableStateOf(true) }
    var hudMultiBallSec by remember(sessionNonce) { mutableIntStateOf(0) }
    var hudLaserSec by remember(sessionNonce) { mutableIntStateOf(0) }
    var hudPaddleSec by remember(sessionNonce) { mutableIntStateOf(0) }
    val lastScoreSyncRef = remember(sessionNonce) { LongArray(1) { 0L } }

    fun syncHudFromEngine(frameTimeMillis: Long = -1L, force: Boolean = false) {
        val phaseChanged = hudPhase != gameEngine.phase
        if (phaseChanged) hudPhase = gameEngine.phase
        if (hudTurbo != gameEngine.turboSpeed) hudTurbo = gameEngine.turboSpeed
        if (hudTurnNumber != gameEngine.turnNumber) hudTurnNumber = gameEngine.turnNumber
        val wantHint = gameEngine.phase == TurnPhase.AIMING && !gameEngine.isDraggingAim && gameEngine.turnNumber == 1
        if (showDragHint != wantHint) showDragHint = wantHint

        // Throttle score, star bar & active bonus countdown updates (~110ms)
        val shouldSyncScore = force || phaseChanged || frameTimeMillis < 0L ||
            (frameTimeMillis - lastScoreSyncRef[0] >= 110L)
        if (shouldSyncScore) {
            if (frameTimeMillis >= 0L) lastScoreSyncRef[0] = frameTimeMillis
            if (hudScore != gameEngine.score) hudScore = gameEngine.score
            val sr = gameEngine.starProgressRatio
            if (abs(hudStarRatio - sr) > 0.012f || sr == 0f || sr >= 1f) hudStarRatio = sr
            val sc = gameEngine.currentStarCount
            if (hudStarCount != sc) hudStarCount = sc
            val mb = kotlin.math.ceil(gameEngine.multiBallTimer).toInt().coerceAtLeast(0)
            val ls = kotlin.math.ceil(gameEngine.laserBeamTimer).toInt().coerceAtLeast(0)
            val pw = kotlin.math.ceil(gameEngine.paddleExpandTimer).toInt().coerceAtLeast(0)
            if (hudMultiBallSec != mb) hudMultiBallSec = mb
            if (hudLaserSec != ls) hudLaserSec = ls
            if (hudPaddleSec != pw) hudPaddleSec = pw
        }

        if (!outcomeHandled) {
            when (gameEngine.phase) {
                TurnPhase.STAGE_CLEAR -> {
                    outcomeHandled = true
                    hudScore = gameEngine.score
                    hudStarRatio = gameEngine.starProgressRatio
                    hudStarCount = gameEngine.currentStarCount
                    onStageCleared(stageNumber, gameEngine.currentStarCount.coerceAtLeast(1))
                }
                TurnPhase.GAME_OVER -> {
                    outcomeHandled = true
                    hudScore = gameEngine.score
                    if (mode == BrickQuestMode.ENDLESS) {
                        onEndlessFinished(gameEngine.score)
                    } else {
                        onStageGameOver()
                    }
                }
                else -> Unit
            }
        }
    }

    BackHandler(enabled = true) {
        if (gameEngine.phase == TurnPhase.STAGE_CLEAR || gameEngine.phase == TurnPhase.GAME_OVER) {
            if (mode == BrickQuestMode.STAGE) onOpenStages() else onGoHome()
        } else {
            isPaused = !isPaused
        }
    }

    // Core 60 FPS Game Loop powered by LaunchedEffect + withFrameMillis inside GameEngine.kt
    GameEngineLoop(
        engine = gameEngine,
        isPaused = isPaused,
        onAudioCue = { cue ->
            when (cue) {
                GameAudioCue.LASER_OR_PICKUP ->
                    soundHaptic.playCorrect(progress.soundEnabled, progress.hapticsEnabled)
                GameAudioCue.BRICK_BREAK ->
                    soundHaptic.playCountdownTick(progress.soundEnabled, progress.hapticsEnabled)
                GameAudioCue.HIT ->
                    soundHaptic.playTap(progress.soundEnabled, false)
            }
        },
        onFrameSynced = { frameTimeMillis ->
            syncHudFromEngine(frameTimeMillis = frameTimeMillis, force = false)
        }
    )

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
                .widthIn(max = 640.dp)
        ) {
            // TOP HUD
            GameTopHudBar(
                score = hudScore,
                starRatio = hudStarRatio,
                starCount = hudStarCount,
                turboActive = hudTurbo,
                isShooting = hudPhase == TurnPhase.SHOOTING,
                onToggleTurbo = {
                    if (gameEngine.toggleTurbo()) {
                        hudTurbo = gameEngine.turboSpeed
                        soundHaptic.playTap(progress.soundEnabled, progress.hapticsEnabled)
                    }
                },
                onPauseClick = {
                    soundHaptic.playTap(progress.soundEnabled, progress.hapticsEnabled)
                    isPaused = true
                }
            )

            // ACTIVE TEMPORARY POWER-UP TIMER PILLS (Multi-Ball, Laser Beam, Wide Paddle)
            if (hudMultiBallSec > 0 || hudLaserSec > 0 || hudPaddleSec > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (hudMultiBallSec > 0) {
                        ActivePowerUpTimerChip(
                            label = "×2 MULTI-BALL ${hudMultiBallSec}s",
                            accent = Color(0xFFEC4899),
                            tag = "active_bonus_multiball"
                        )
                    }
                    if (hudLaserSec > 0) {
                        ActivePowerUpTimerChip(
                            label = "⚡ LASER ${hudLaserSec}s",
                            accent = Color(0xFF4ADE80),
                            tag = "active_bonus_laser"
                        )
                    }
                    if (hudPaddleSec > 0) {
                        ActivePowerUpTimerChip(
                            label = "↔ WIDE ${hudPaddleSec}s",
                            accent = Color(0xFF38BDF8),
                            tag = "active_bonus_paddle"
                        )
                    }
                }
            }

            // CENTER 60FPS CANVAS GAME ENGINE VIEWPORT
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                BrickBreakerCanvas(
                    engine = gameEngine,
                    skin = progress.selectedSkin,
                    isPaused = isPaused,
                    onAimStarted = {
                        if (showDragHint) showDragHint = false
                    },
                    onShotFired = { syncHudFromEngine() },
                    modifier = Modifier.fillMaxSize()
                )

                if (showDragHint) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = BrickQuestColors.CardSurface.copy(alpha = 0.86f),
                        border = BorderStroke(1.dp, BrickQuestColors.NeonCyan.copy(alpha = 0.7f)),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 34.dp)
                    ) {
                        Text(
                            text = "DRAG TO AIM  •  RELEASE TO SHOOT",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            ),
                            color = BrickQuestColors.NeonCyanBright,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            // BOTTOM POWER-UPS & RECALL CONTROLS
            GameBottomControlsBar(
                mode = mode,
                stageNumber = stageNumber,
                turnNumber = hudTurnNumber,
                isShooting = hudPhase == TurnPhase.SHOOTING,
                lightningCount = progress.lightningCount,
                extraBallsCount = progress.extraBallsCount,
                onUseLightning = {
                    onUseLightning {
                        val applied = gameEngine.triggerLightningRowBlast()
                        if (applied) syncHudFromEngine()
                        applied
                    }
                },
                onUseExtraBalls = {
                    onUseExtraBalls {
                        val applied = gameEngine.triggerExtraBallsStorm()
                        if (applied) syncHudFromEngine()
                        applied
                    }
                },
                onRecallBalls = {
                    gameEngine.recallBalls()
                    soundHaptic.playTap(progress.soundEnabled, progress.hapticsEnabled)
                }
            )
        }

        // PAUSE OVERLAY
        if (isPaused && hudPhase != TurnPhase.STAGE_CLEAR && hudPhase != TurnPhase.GAME_OVER) {
            GamePauseOverlay(
                mode = mode,
                stageNumber = stageNumber,
                soundEnabled = progress.soundEnabled,
                hapticsEnabled = progress.hapticsEnabled,
                onResume = { isPaused = false },
                onRestart = {
                    isPaused = false
                    onRestart()
                },
                onToggleSound = onToggleSound,
                onToggleHaptics = onToggleHaptics,
                onStages = {
                    isPaused = false
                    onOpenStages()
                },
                onHome = {
                    isPaused = false
                    onGoHome()
                }
            )
        }

        // STAGE CLEAR OVERLAY
        if (hudPhase == TurnPhase.STAGE_CLEAR) {
            StageClearOverlay(
                stageNumber = stageNumber,
                score = hudScore,
                stars = hudStarCount.coerceAtLeast(1),
                hasNextStage = stageNumber < TOTAL_QUEST_STAGES,
                onNextStage = onNextStage,
                onReplay = onRestart,
                onStages = onOpenStages
            )
        }

        // GAME OVER OVERLAY
        if (hudPhase == TurnPhase.GAME_OVER) {
            GameOverOverlay(
                mode = mode,
                stageNumber = stageNumber,
                score = hudScore,
                bestScore = maxOf(hudScore, progress.endlessBestScore),
                onRetry = onRestart,
                onStages = onOpenStages,
                onHome = onGoHome
            )
        }
    }
}

@Composable
private fun GameTopHudBar(
    score: Int,
    starRatio: Float,
    starCount: Int,
    turboActive: Boolean,
    isShooting: Boolean,
    onToggleTurbo: () -> Unit,
    onPauseClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (turboActive) Color(0xFF2B2107) else Color(0xFF0A2238))
                .border(
                    width = 2.dp,
                    color = if (turboActive) BrickQuestColors.NeonGold else BrickQuestColors.NeonCyanBright,
                    shape = CircleShape
                )
                .clickable(onClick = onToggleTurbo)
                .testTag("game_turbo_button"),
            contentAlignment = Alignment.Center
        ) {
            if (isShooting) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.FastForward,
                        contentDescription = "2x Speed",
                        tint = if (turboActive) BrickQuestColors.NeonGold else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    if (turboActive) {
                        Text(
                            text = "2×",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                            color = BrickQuestColors.NeonGold
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF0A1628))
                        .border(1.dp, BrickQuestColors.NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(50))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(starRatio.coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF38BDF8), Color(0xFF93C5FD), Color.White)
                                )
                            )
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(0.20f))
                    StarProgressPip(earned = starCount >= 1)
                    Spacer(modifier = Modifier.weight(0.32f))
                    StarProgressPip(earned = starCount >= 2)
                    Spacer(modifier = Modifier.weight(0.32f))
                    StarProgressPip(earned = starCount >= 3)
                    Spacer(modifier = Modifier.weight(0.06f))
                }
            }
        }

        Text(
            text = "$score",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontFeatureSettings = "tnum"
            ),
            color = Color.White,
            modifier = Modifier.testTag("game_score_text")
        )

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFF0A2238))
                .border(2.dp, BrickQuestColors.NeonCyanBright, CircleShape)
                .clickable(onClick = onPauseClick)
                .testTag("game_pause_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Pause,
                contentDescription = "Pause Game",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun ActivePowerUpTimerChip(
    label: String,
    accent: Color,
    tag: String
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = accent.copy(alpha = 0.22f),
        border = BorderStroke(1.2.dp, accent),
        modifier = Modifier.testTag(tag)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 0.6.sp
            ),
            color = Color.White,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun StarProgressPip(earned: Boolean) {
    Icon(
        imageVector = Icons.Filled.Star,
        contentDescription = null,
        tint = if (earned) Color(0xFFFEF08A) else Color(0xFF1E293B),
        modifier = Modifier.size(16.dp)
    )
}

@Composable
private fun GameBottomControlsBar(
    mode: BrickQuestMode,
    stageNumber: Int,
    turnNumber: Int,
    isShooting: Boolean,
    lightningCount: Int,
    extraBallsCount: Int,
    onUseLightning: () -> Unit,
    onUseExtraBalls: () -> Unit,
    onRecallBalls: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CrimsonPowerUpButton(
            count = lightningCount,
            isLightning = true,
            tag = "power_lightning_button",
            contentDescription = "Blast Bottom Row",
            onClick = onUseLightning
        )

        if (isShooting) {
            Surface(
                onClick = onRecallBalls,
                shape = RoundedCornerShape(50),
                color = BrickQuestColors.CardSurfaceElevated,
                border = BorderStroke(1.6.dp, BrickQuestColors.NeonCyan),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("game_recall_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowDownward,
                        contentDescription = "Recall Balls",
                        tint = BrickQuestColors.NeonCyanBright,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "RECALL",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = Color.White
                    )
                }
            }
        } else {
            Surface(
                shape = RoundedCornerShape(50),
                color = BrickQuestColors.CardSurface,
                border = BorderStroke(1.2.dp, BrickQuestColors.CardBorder)
            ) {
                Text(
                    text = if (mode == BrickQuestMode.STAGE) "STAGE $stageNumber" else "ENDLESS • WAVE $turnNumber",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    ),
                    color = BrickQuestColors.NeonCyanBright,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }

        CrimsonPowerUpButton(
            count = extraBallsCount,
            isLightning = false,
            tag = "power_balls_button",
            contentDescription = "Add 25 Extra Balls",
            onClick = onUseExtraBalls
        )
    }
}

@Composable
private fun CrimsonPowerUpButton(
    count: Int,
    isLightning: Boolean,
    tag: String,
    contentDescription: String,
    onClick: () -> Unit
) {
    val enabled = count > 0
    val crimsonBrush = remember {
        Brush.radialGradient(
            colors = listOf(
                BrickQuestColors.CrimsonGlow,
                BrickQuestColors.CrimsonDeep,
                Color(0xFF4C0519)
            )
        )
    }
    Box(
        modifier = Modifier
            .size(66.dp)
            .alpha(if (enabled) 1f else 0.42f)
            .clip(CircleShape)
            .background(crimsonBrush)
            .border(2.2.dp, BrickQuestColors.CrimsonBorder, CircleShape)
            .clickable(enabled = enabled, onClick = onClick)
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isLightning) {
                Icon(
                    imageVector = Icons.Filled.Bolt,
                    contentDescription = contentDescription,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            } else {
                Canvas(modifier = Modifier.size(28.dp)) {
                    val r = size.minDimension * 0.145f
                    for (rowIdx in 0 until 3) {
                        val countInRow = rowIdx + 1
                        val cy = size.height * (0.22f + rowIdx * 0.27f)
                        val startX = size.width / 2f - ((countInRow - 1) * r * 2.25f) / 2f
                        for (i in 0 until countInRow) {
                            drawCircle(
                                color = Color.White,
                                radius = r,
                                center = Offset(startX + i * r * 2.25f, cy)
                            )
                        }
                    }
                }
            }
            Text(
                text = "x $count",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp
                ),
                color = Color.White
            )
        }
    }
}

@Composable
private fun GamePauseOverlay(
    mode: BrickQuestMode,
    stageNumber: Int,
    soundEnabled: Boolean,
    hapticsEnabled: Boolean,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onToggleSound: () -> Unit,
    onToggleHaptics: () -> Unit,
    onStages: () -> Unit,
    onHome: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = BrickQuestColors.CardSurface,
            border = BorderStroke(1.8.dp, BrickQuestColors.NeonCyan.copy(alpha = 0.75f)),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 360.dp)
                .padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "PAUSED",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    ),
                    color = BrickQuestColors.TextPrimary
                )
                Text(
                    text = if (mode == BrickQuestMode.STAGE) "Stage $stageNumber" else "Endless Mode",
                    style = MaterialTheme.typography.labelLarge,
                    color = BrickQuestColors.NeonCyanBright
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = BrickQuestColors.CardSurfaceElevated,
                        border = BorderStroke(
                            1.4.dp,
                            if (soundEnabled) BrickQuestColors.NeonCyan else BrickQuestColors.CardBorder
                        )
                    ) {
                        IconButton(
                            onClick = onToggleSound,
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = if (soundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                                contentDescription = "Toggle Sound",
                                tint = if (soundEnabled) BrickQuestColors.NeonCyanBright else BrickQuestColors.TextMuted
                            )
                        }
                    }
                    Surface(
                        shape = CircleShape,
                        color = BrickQuestColors.CardSurfaceElevated,
                        border = BorderStroke(
                            1.4.dp,
                            if (hapticsEnabled) BrickQuestColors.NeonCyan else BrickQuestColors.CardBorder
                        )
                    ) {
                        IconButton(
                            onClick = onToggleHaptics,
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Vibration,
                                contentDescription = "Toggle Haptics",
                                tint = if (hapticsEnabled) BrickQuestColors.NeonCyanBright else BrickQuestColors.TextMuted
                            )
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                Button(
                    onClick = onResume,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrickQuestColors.NeonCyan,
                        contentColor = Color(0xFF04111F)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("pause_resume_button")
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("RESUME", fontWeight = FontWeight.Black)
                }

                OutlinedButton(
                    onClick = onRestart,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.4.dp, BrickQuestColors.NeonMagenta),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("pause_restart_button")
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, tint = BrickQuestColors.NeonMagenta)
                    Spacer(Modifier.width(6.dp))
                    Text("RESTART", color = BrickQuestColors.TextPrimary, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onStages,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.2.dp, BrickQuestColors.CardBorder),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("pause_stages_button")
                    ) {
                        Icon(Icons.Filled.GridView, contentDescription = null, tint = BrickQuestColors.TextSecondary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("STAGES", color = BrickQuestColors.TextPrimary, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onHome,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.2.dp, BrickQuestColors.CardBorder),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("pause_home_button")
                    ) {
                        Icon(Icons.Filled.Home, contentDescription = null, tint = BrickQuestColors.TextSecondary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("HOME", color = BrickQuestColors.TextPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun StageClearOverlay(
    stageNumber: Int,
    score: Int,
    stars: Int,
    hasNextStage: Boolean,
    onNextStage: () -> Unit,
    onReplay: () -> Unit,
    onStages: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.76f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = BrickQuestColors.CardSurface,
            border = BorderStroke(2.dp, BrickQuestColors.NeonGold),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 360.dp)
                .padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "STAGE $stageNumber CLEAR!",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    ),
                    color = BrickQuestColors.NeonGold,
                    textAlign = TextAlign.Center
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..3) {
                        val earned = i <= stars
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = if (earned) BrickQuestColors.NeonGold else Color.White.copy(alpha = 0.16f),
                            modifier = Modifier
                                .size(if (i == 2) 56.dp else 44.dp)
                                .offset(y = if (i == 2) (-6).dp else 0.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = BrickQuestColors.CardSurfaceElevated,
                    border = BorderStroke(1.dp, BrickQuestColors.CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "SCORE",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                            color = BrickQuestColors.TextSecondary
                        )
                        Text(
                            text = "$score",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                            color = Color.White
                        )
                    }
                }

                if (hasNextStage) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(BrickQuestColors.PlayButtonBrush)
                            .border(1.4.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .clickable(onClick = onNextStage)
                            .testTag("stage_clear_next_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "NEXT STAGE ${stageNumber + 1}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = Color.White
                            )
                            Spacer(Modifier.width(6.dp))
                            Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onReplay,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.3.dp, BrickQuestColors.NeonCyan),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("stage_clear_replay_button")
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, tint = BrickQuestColors.NeonCyanBright, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("REPLAY", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onStages,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.3.dp, BrickQuestColors.CardBorder),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("stage_clear_stages_button")
                    ) {
                        Icon(Icons.Filled.GridView, contentDescription = null, tint = BrickQuestColors.TextSecondary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("STAGES", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun GameOverOverlay(
    mode: BrickQuestMode,
    stageNumber: Int,
    score: Int,
    bestScore: Int,
    onRetry: () -> Unit,
    onStages: () -> Unit,
    onHome: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.78f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = BrickQuestColors.CardSurface,
            border = BorderStroke(2.dp, BrickQuestColors.CrimsonBorder),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 360.dp)
                .padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "GAME OVER",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.8.sp
                    ),
                    color = BrickQuestColors.CrimsonBorder
                )
                Text(
                    text = if (mode == BrickQuestMode.STAGE) "Bricks reached the bottom on Stage $stageNumber"
                    else "Endless Mode Best: $bestScore",
                    style = MaterialTheme.typography.bodySmall,
                    color = BrickQuestColors.TextSecondary,
                    textAlign = TextAlign.Center
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = BrickQuestColors.CardSurfaceElevated,
                    border = BorderStroke(1.dp, BrickQuestColors.CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "FINAL SCORE",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                            color = BrickQuestColors.TextSecondary
                        )
                        Text(
                            text = "$score",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                            color = Color.White
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(BrickQuestColors.EndlessButtonBrush)
                        .border(1.4.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .clickable(onClick = onRetry)
                        .testTag("game_over_retry_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "TRY AGAIN",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = Color.White
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onStages,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.3.dp, BrickQuestColors.CardBorder),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("game_over_stages_button")
                    ) {
                        Icon(Icons.Filled.GridView, contentDescription = null, tint = BrickQuestColors.TextSecondary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("STAGES", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onHome,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.3.dp, BrickQuestColors.CardBorder),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("game_over_home_button")
                    ) {
                        Icon(Icons.Filled.Home, contentDescription = null, tint = BrickQuestColors.TextSecondary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("HOME", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
