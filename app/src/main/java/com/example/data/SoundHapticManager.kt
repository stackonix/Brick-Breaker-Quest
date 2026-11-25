package com.example.data

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

/**
 * Lightweight, non-blocking offline sound & haptic feedback controller.
 * All AudioService / Vibrator Binder IPC runs off the Main thread via a conflated channel
 * so 60 FPS gameplay frames are NEVER stalled by audio or vibration calls.
 */
class SoundHapticManager(private val context: Context) {

    private data class FeedbackEvent(
        val toneType: Int,
        val toneDurationMs: Int,
        val vibDurationMs: Long,
        val vibAmplitude: Int,
        val soundEnabled: Boolean,
        val hapticsEnabled: Boolean
    )

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val channel = Channel<FeedbackEvent>(capacity = Channel.CONFLATED)

    @Volatile
    private var toneGenerator: ToneGenerator? = null

    @Volatile
    private var cachedVibrator: Vibrator? = null

    init {
        scope.launch {
            cachedVibrator = resolveVibrator()
            for (event in channel) {
                if (event.soundEnabled) {
                    runCatching { getToneGenerator()?.startTone(event.toneType, event.toneDurationMs) }
                }
                if (event.hapticsEnabled && event.vibDurationMs > 0L) {
                    vibrateNow(event.vibDurationMs, event.vibAmplitude)
                }
            }
        }
    }

    private fun getToneGenerator(): ToneGenerator? {
        if (toneGenerator == null) {
            toneGenerator = runCatching {
                ToneGenerator(AudioManager.STREAM_MUSIC, 50)
            }.getOrNull()
        }
        return toneGenerator
    }

    private fun resolveVibrator(): Vibrator? = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }.getOrNull()

    fun playTap(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (!soundEnabled && !hapticsEnabled) return
        channel.trySend(FeedbackEvent(ToneGenerator.TONE_PROP_BEEP, 28, 12L, 50, soundEnabled, hapticsEnabled))
    }

    fun playCorrect(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (!soundEnabled && !hapticsEnabled) return
        channel.trySend(FeedbackEvent(ToneGenerator.TONE_PROP_ACK, 95, 22L, 100, soundEnabled, hapticsEnabled))
    }

    fun playWrong(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (!soundEnabled && !hapticsEnabled) return
        channel.trySend(FeedbackEvent(ToneGenerator.TONE_PROP_NACK, 100, 40L, 85, soundEnabled, hapticsEnabled))
    }

    fun playCountdownTick(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (!soundEnabled && !hapticsEnabled) return
        channel.trySend(FeedbackEvent(ToneGenerator.TONE_PROP_BEEP2, 48, 14L, 65, soundEnabled, hapticsEnabled))
    }

    fun playWinFanfare(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (!soundEnabled && !hapticsEnabled) return
        channel.trySend(FeedbackEvent(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 220, 48L, 130, soundEnabled, hapticsEnabled))
    }

    private fun vibrateNow(durationMs: Long, amplitude: Int) {
        runCatching {
            val vibrator = cachedVibrator ?: return
            if (vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(durationMs, amplitude.coerceIn(1, 255)))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(durationMs)
                }
            }
        }
    }

    fun release() {
        channel.close()
        scope.cancel()
        runCatching { toneGenerator?.release() }
        toneGenerator = null
    }
}
