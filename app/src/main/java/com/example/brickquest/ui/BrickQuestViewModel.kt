package com.example.brickquest.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.brickquest.data.BallSkin
import com.example.brickquest.data.BrickQuestProgress
import com.example.brickquest.data.BrickQuestStore
import com.example.brickquest.data.TOTAL_QUEST_STAGES
import com.example.brickquest.engine.BrickQuestMode
import com.example.data.SoundHapticManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface BrickQuestScreen {
    data object Splash : BrickQuestScreen
    data object Home : BrickQuestScreen
    data object StageSelect : BrickQuestScreen
    data class Playing(
        val mode: BrickQuestMode,
        val stageNumber: Int,
        val sessionNonce: Long
    ) : BrickQuestScreen
}

class BrickQuestViewModel(application: Application) : AndroidViewModel(application) {

    private val store = BrickQuestStore(application)
    val soundHaptic = SoundHapticManager(application)

    val progress: StateFlow<BrickQuestProgress> = store.progressFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = BrickQuestProgress()
    )

    private val _screen = MutableStateFlow<BrickQuestScreen>(BrickQuestScreen.Splash)
    val screen: StateFlow<BrickQuestScreen> = _screen.asStateFlow()

    private var sessionCounter = 1L

    init {
        viewModelScope.launch {
            delay(1050L)
            if (_screen.value is BrickQuestScreen.Splash) {
                _screen.value = BrickQuestScreen.Home
            }
        }
    }

    fun playTapFeedback() {
        val p = progress.value
        soundHaptic.playTap(p.soundEnabled, p.hapticsEnabled)
    }

    fun openStageSelect() {
        playTapFeedback()
        _screen.value = BrickQuestScreen.StageSelect
    }

    fun goHome() {
        playTapFeedback()
        _screen.value = BrickQuestScreen.Home
    }

    fun startStage(stage: Int) {
        val clamped = stage.coerceIn(1, TOTAL_QUEST_STAGES)
        if (!progress.value.isStageUnlocked(clamped)) return
        playTapFeedback()
        _screen.value = BrickQuestScreen.Playing(
            mode = BrickQuestMode.STAGE,
            stageNumber = clamped,
            sessionNonce = ++sessionCounter
        )
    }

    fun startEndless() {
        playTapFeedback()
        _screen.value = BrickQuestScreen.Playing(
            mode = BrickQuestMode.ENDLESS,
            stageNumber = 1,
            sessionNonce = ++sessionCounter
        )
    }

    fun restartCurrentMatch() {
        val current = _screen.value as? BrickQuestScreen.Playing ?: return
        playTapFeedback()
        _screen.value = current.copy(sessionNonce = ++sessionCounter)
    }

    fun startNextStage() {
        val current = _screen.value as? BrickQuestScreen.Playing ?: return
        if (current.mode != BrickQuestMode.STAGE) {
            restartCurrentMatch()
            return
        }
        val next = (current.stageNumber + 1).coerceAtMost(TOTAL_QUEST_STAGES)
        playTapFeedback()
        _screen.value = BrickQuestScreen.Playing(
            mode = BrickQuestMode.STAGE,
            stageNumber = next,
            sessionNonce = ++sessionCounter
        )
    }

    fun onStageCleared(stage: Int, stars: Int) {
        val p = progress.value
        soundHaptic.playWinFanfare(p.soundEnabled, p.hapticsEnabled)
        viewModelScope.launch {
            store.recordStageClear(stage = stage, stars = stars)
        }
    }

    fun onEndlessFinished(score: Int) {
        val p = progress.value
        soundHaptic.playWrong(p.soundEnabled, p.hapticsEnabled)
        viewModelScope.launch {
            store.recordEndlessScore(score)
        }
    }

    fun onStageGameOver() {
        val p = progress.value
        soundHaptic.playWrong(p.soundEnabled, p.hapticsEnabled)
    }

    fun useLightningPowerUp(onApplied: () -> Boolean) {
        if (progress.value.lightningCount <= 0) return
        val applied = onApplied()
        if (applied) {
            val p = progress.value
            soundHaptic.playCorrect(p.soundEnabled, p.hapticsEnabled)
            viewModelScope.launch {
                store.consumeLightning()
            }
        }
    }

    fun useExtraBallsPowerUp(onApplied: () -> Boolean) {
        if (progress.value.extraBallsCount <= 0) return
        val applied = onApplied()
        if (applied) {
            val p = progress.value
            soundHaptic.playCorrect(p.soundEnabled, p.hapticsEnabled)
            viewModelScope.launch {
                store.consumeExtraBalls()
            }
        }
    }

    fun selectSkin(skin: BallSkin) {
        if (!progress.value.isSkinUnlocked(skin)) return
        playTapFeedback()
        viewModelScope.launch {
            store.setBallSkin(skin)
        }
    }

    fun toggleSound() {
        val next = !progress.value.soundEnabled
        viewModelScope.launch {
            store.setSoundEnabled(next)
        }
        if (next) {
            soundHaptic.playTap(soundEnabled = true, hapticsEnabled = progress.value.hapticsEnabled)
        }
    }

    fun toggleHaptics() {
        val next = !progress.value.hapticsEnabled
        viewModelScope.launch {
            store.setHapticsEnabled(next)
        }
        if (next) {
            soundHaptic.playTap(soundEnabled = progress.value.soundEnabled, hapticsEnabled = true)
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundHaptic.release()
    }
}
