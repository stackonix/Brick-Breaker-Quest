package com.brickbreaker.game

import android.graphics.Color as AndroidColor
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brickbreaker.game.brickquest.ui.BrickQuestGameScreen
import com.brickbreaker.game.brickquest.ui.BrickQuestHomeScreen
import com.brickbreaker.game.brickquest.ui.BrickQuestScreen
import com.brickbreaker.game.brickquest.ui.BrickQuestSplashScreen
import com.brickbreaker.game.brickquest.ui.BrickQuestTheme
import com.brickbreaker.game.brickquest.ui.BrickQuestViewModel
import com.brickbreaker.game.brickquest.ui.StageSelectScreen

class MainActivity : ComponentActivity() {

    private val viewModel: BrickQuestViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestHighestRefreshRate()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )
        setContent {
            BrickQuestApp(
                viewModel = viewModel,
                onKeepScreenOnChanged = { keepOn ->
                    if (keepOn) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                }
            )
        }
    }

    private fun requestHighestRefreshRate() {
        runCatching {
            val activeDisplay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                display
            } else {
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay
            } ?: return
            val bestMode = activeDisplay.supportedModes.maxByOrNull { it.refreshRate } ?: return
            val attrs = window.attributes
            attrs.preferredDisplayModeId = bestMode.modeId
            window.attributes = attrs
        }
    }
}

@Composable
fun BrickQuestApp(
    viewModel: BrickQuestViewModel,
    onKeepScreenOnChanged: (Boolean) -> Unit = {}
) {
    val screen by viewModel.screen.collectAsStateWithLifecycle()
    val progress by viewModel.progress.collectAsStateWithLifecycle()

    val isPlaying = screen is BrickQuestScreen.Playing
    DisposableEffect(isPlaying) {
        onKeepScreenOnChanged(isPlaying)
        onDispose { onKeepScreenOnChanged(false) }
    }

    BrickQuestTheme {
        AnimatedContent(
            targetState = screen,
            transitionSpec = {
                (fadeIn(tween(180, easing = FastOutSlowInEasing)) +
                    scaleIn(initialScale = 0.96f, animationSpec = tween(190, easing = FastOutSlowInEasing)))
                    .togetherWith(fadeOut(tween(140, easing = FastOutSlowInEasing)))
            },
            label = "screen_transition"
        ) { current ->
            when (current) {
                BrickQuestScreen.Splash -> BrickQuestSplashScreen()

                BrickQuestScreen.Home -> BrickQuestHomeScreen(
                    progress = progress,
                    onPlayStage = viewModel::startStage,
                    onOpenWorldMap = viewModel::openStageSelect,
                    onStartEndless = viewModel::startEndless,
                    onSelectSkin = viewModel::selectSkin,
                    onToggleSound = viewModel::toggleSound,
                    onToggleHaptics = viewModel::toggleHaptics
                )

                BrickQuestScreen.StageSelect -> StageSelectScreen(
                    progress = progress,
                    onSelectStage = viewModel::startStage,
                    onBack = viewModel::goHome
                )

                is BrickQuestScreen.Playing -> BrickQuestGameScreen(
                    mode = current.mode,
                    stageNumber = current.stageNumber,
                    sessionNonce = current.sessionNonce,
                    progress = progress,
                    soundHaptic = viewModel.soundHaptic,
                    onUseLightning = viewModel::useLightningPowerUp,
                    onUseExtraBalls = viewModel::useExtraBallsPowerUp,
                    onStageCleared = viewModel::onStageCleared,
                    onEndlessFinished = viewModel::onEndlessFinished,
                    onStageGameOver = viewModel::onStageGameOver,
                    onNextStage = viewModel::startNextStage,
                    onRestart = viewModel::restartCurrentMatch,
                    onOpenStages = viewModel::openStageSelect,
                    onGoHome = viewModel::goHome,
                    onToggleSound = viewModel::toggleSound,
                    onToggleHaptics = viewModel::toggleHaptics
                )
            }
        }
    }
}
