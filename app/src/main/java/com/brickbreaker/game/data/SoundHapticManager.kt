package com.brickbreaker.game.data

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.brickbreaker.game.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

/**
 * Low-latency game audio + haptics.
 *
 * Sounds use [SoundPool] with short preloaded clips (res/raw/sfx_*.wav). SoundPool.play() is
 * non-blocking and mixes up to [MAX_STREAMS] sounds at once, so rapid brick hits no longer cut
 * each other off the way the old ToneGenerator did.
 * Vibration still goes through a background channel because Vibrator calls are Binder IPC.
 */
class SoundHapticManager(context: Context) {

    private val appContext = context.applicationContext

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(MAX_STREAMS)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    // Lock-free loaded flags indexed by SoundPool sampleId (avoids HashSet allocation & boxing)
    @Volatile
    private var loadedMask: Long = 0L

    private val sfxTap: Int
    private val sfxHit: Int
    private val sfxBreak: Int
    private val sfxPower: Int
    private val sfxWin: Int
    private val sfxLose: Int

    private data class HapticEvent(val durationMs: Long, val amplitude: Int)
    private data class SoundEvent(val soundId: Int, val volume: Float, val rate: Float)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val hapticChannel = Channel<HapticEvent>(capacity = Channel.CONFLATED)
    private val soundChannel = Channel<SoundEvent>(capacity = 12, onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST)

    @Volatile
    private var cachedVibrator: Vibrator? = null

    init {
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0 && sampleId in 1..62) {
                loadedMask = loadedMask or (1L shl sampleId)
            }
        }
        sfxTap = soundPool.load(appContext, R.raw.sfx_tap, 1)
        sfxHit = soundPool.load(appContext, R.raw.sfx_hit, 1)
        sfxBreak = soundPool.load(appContext, R.raw.sfx_break, 1)
        sfxPower = soundPool.load(appContext, R.raw.sfx_power, 1)
        sfxWin = soundPool.load(appContext, R.raw.sfx_win, 1)
        sfxLose = soundPool.load(appContext, R.raw.sfx_lose, 1)

        // Run SoundPool IPC completely off the 60/120fps render thread
        scope.launch {
            for (ev in soundChannel) {
                try {
                    soundPool.play(ev.soundId, ev.volume, ev.volume, 1, 0, ev.rate)
                } catch (_: Throwable) {
                }
            }
        }

        scope.launch {
            cachedVibrator = resolveVibrator()
            for (event in hapticChannel) {
                vibrateNow(event.durationMs, event.amplitude)
            }
        }
    }

    private fun play(soundId: Int, volume: Float, rate: Float = 1f) {
        if (soundId !in 1..62) return
        if ((loadedMask and (1L shl soundId)) == 0L) return
        soundChannel.trySend(SoundEvent(soundId, volume, rate))
    }

    private fun haptic(enabled: Boolean, durationMs: Long, amplitude: Int) {
        if (enabled) hapticChannel.trySend(HapticEvent(durationMs, amplitude))
    }

    /** UI taps and buttons. */
    fun playTap(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (soundEnabled) play(sfxTap, 0.55f)
        haptic(hapticsEnabled, 12L, 50)
    }

    /** Ball hits a brick without breaking it. */
    fun playHit(soundEnabled: Boolean) {
        if (soundEnabled) play(sfxHit, 0.35f)
    }

    /** A brick breaks. */
    fun playBreak(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (soundEnabled) play(sfxBreak, 0.6f)
        haptic(hapticsEnabled, 14L, 65)
    }

    /** Power-up, laser, pickup, bomb. */
    fun playCorrect(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (soundEnabled) play(sfxPower, 0.7f)
        haptic(hapticsEnabled, 22L, 100)
    }

    fun playWrong(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (soundEnabled) play(sfxLose, 0.8f)
        haptic(hapticsEnabled, 40L, 85)
    }

    fun playWinFanfare(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (soundEnabled) play(sfxWin, 0.85f)
        haptic(hapticsEnabled, 48L, 130)
    }

    private fun resolveVibrator(): Vibrator? = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }.getOrNull()

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
        soundChannel.close()
        hapticChannel.close()
        scope.cancel()
        runCatching { soundPool.release() }
    }

    private companion object {
        const val MAX_STREAMS = 8
    }
}
