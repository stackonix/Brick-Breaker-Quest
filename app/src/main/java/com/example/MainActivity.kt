package com.example

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.brickquest.ui.BrickQuestGameScreen
import com.example.brickquest.ui.BrickQuestHomeScreen
import com.example.brickquest.ui.BrickQuestScreen
import com.example.brickquest.ui.BrickQuestSplashScreen
import com.example.brickquest.ui.BrickQuestTheme
import com.example.brickquest.ui.BrickQuestViewModel
import com.example.brickquest.ui.StageSelectScreen

class MainActivity : ComponentActivity() {

    private val viewModel: BrickQuestViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )
        setContent {
            BrickQuestApp(viewModel = viewModel)
        }
    }
}

@Composable
fun BrickQuestApp(viewModel: BrickQuestViewModel) {
    val screen by viewModel.screen.collectAsStateWithLifecycle()
    val progress by viewModel.progress.collectAsStateWithLifecycle()

    BrickQuestTheme {
        Crossfade(
            targetState = screen,
            animationSpec = tween(durationMillis = 160),
            label = "screen_transition"
        ) { current ->
            when (current) {
                BrickQuestScreen.Splash -> BrickQuestSplashScreen()

                BrickQuestScreen.Home -> BrickQuestHomeScreen(
                    progress = progress,
                    onStartGame = viewModel::openStageSelect,
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
