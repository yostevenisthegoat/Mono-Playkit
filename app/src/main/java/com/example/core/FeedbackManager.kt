package com.example.core

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

enum class SoundCue {
    CLICK,
    TICK,
    ACCENT_TICK,
    COMPLETE,
    WARNING,
}

/**
 * Offline synthesized sound and tactile feedback engine.
 * Generates clean, short PCM waveforms in memory with zero external audio assets or permissions.
 */
class FeedbackManager(context: Context) {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Volatile
    var soundEnabled: Boolean = true

    @Volatile
    var hapticEnabled: Boolean = true

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private val clickPcm by lazy { synthesizeTone(freqHz = 880.0, durationMs = 22, decay = 9.0, gain = 0.25f) }
    private val tickPcm by lazy { synthesizeTone(freqHz = 1050.0, durationMs = 24, decay = 11.0, gain = 0.38f) }
    private val accentTickPcm by lazy { synthesizeTone(freqHz = 1480.0, durationMs = 32, decay = 9.5, gain = 0.50f) }
    private val warningPcm by lazy { synthesizeTone(freqHz = 660.0, durationMs = 95, decay = 4.0, gain = 0.42f) }
    private val completePcm by lazy { synthesizeChime() }

    fun play(cue: SoundCue, withHaptic: Boolean = true) {
        if (soundEnabled) {
            scope.launch {
                val samples = when (cue) {
                    SoundCue.CLICK -> clickPcm
                    SoundCue.TICK -> tickPcm
                    SoundCue.ACCENT_TICK -> accentTickPcm
                    SoundCue.WARNING -> warningPcm
                    SoundCue.COMPLETE -> completePcm
                }
                playPcm(samples)
            }
        }
        if (withHaptic && hapticEnabled) {
            vibrateLight(
                durationMs = when (cue) {
                    SoundCue.COMPLETE -> 48L
                    SoundCue.WARNING -> 36L
                    SoundCue.ACCENT_TICK -> 20L
                    else -> 12L
                },
            )
        }
    }

    fun vibrateLight(durationMs: Long = 14L) {
        if (!hapticEnabled) return
        try {
            val vib = vibrator ?: return
            if (!vib.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib.vibrate(
                    VibrationEffect.createOneShot(
                        durationMs,
                        VibrationEffect.DEFAULT_AMPLITUDE,
                    ),
                )
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(durationMs)
            }
        } catch (_: Throwable) {
            // Safe fallback on devices without vibrator hardware
        }
    }

    private fun playPcm(samples: ShortArray) {
        var track: AudioTrack? = null
        try {
            val sampleRate = 22050
            val bufferBytes = samples.size * 2
            track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build(),
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build(),
                )
                .setTransferMode(AudioTrack.MODE_STATIC)
                .setBufferSizeInBytes(bufferBytes)
                .build()
            track.write(samples, 0, samples.size)
            track.play()
            Thread.sleep((samples.size * 1000L / sampleRate) + 25L)
        } catch (_: Throwable) {
            // Ignore audio errors gracefully
        } finally {
            try {
                track?.stop()
                track?.release()
            } catch (_: Throwable) {
            }
        }
    }

    private fun synthesizeTone(
        freqHz: Double,
        durationMs: Int,
        decay: Double,
        gain: Float,
    ): ShortArray {
        val sampleRate = 22050
        val count = (sampleRate * durationMs) / 1000
        return ShortArray(count) { i ->
            val t = i.toDouble() / sampleRate
            val env = exp(-decay * (i.toDouble() / count))
            val sample = sin(2.0 * PI * freqHz * t) * env * gain
            (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
    }

    private fun synthesizeChime(): ShortArray {
        val sampleRate = 22050
        val count = (sampleRate * 240) / 1000
        return ShortArray(count) { i ->
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / count
            val f = if (progress < 0.45) 880.0 else 1318.5
            val localProgress = if (progress < 0.45) progress / 0.45 else (progress - 0.45) / 0.55
            val env = exp(-4.5 * localProgress)
            val sample = sin(2.0 * PI * f * t) * env * 0.4f
            (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
    }
}
