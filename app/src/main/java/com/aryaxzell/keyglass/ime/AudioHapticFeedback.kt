package com.aryaxzell.keyglass.ime

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

class AudioHapticFeedback(private val context: Context) {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val audioScope = CoroutineScope(Dispatchers.Default)

    // Pre-generated short soft pop sound buffer
    private val sampleRate = 22050
    private val popAudioData: ShortArray by lazy {
        val durationMs = 25
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)
        val frequency = 480.0 // Warm soft pop pitch
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            // Exponential decay envelope
            val envelope = Math.exp(-t * 180.0)
            val wave = sin(2.0 * Math.PI * frequency * t) * envelope
            buffer[i] = (wave * 24000.0).toInt().coerceIn(-32768, 32767).toShort()
        }
        buffer
    }

    fun triggerKeyFeedback(
        view: View?,
        hapticEnabled: Boolean,
        hapticIntensity: Float,
        soundEnabled: Boolean,
        soundVolume: Float
    ) {
        // Haptic feedback
        if (hapticEnabled) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && vibrator != null && vibrator.hasVibrator()) {
                    val amplitude = (hapticIntensity.coerceIn(0.1f, 1.0f) * 255).toInt().coerceIn(1, 255)
                    val effect = VibrationEffect.createOneShot(12L, amplitude)
                    vibrator.vibrate(effect)
                } else {
                    view?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                }
            } catch (e: Exception) {
                view?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            }
        }

        // Sound feedback
        if (soundEnabled && soundVolume > 0.05f) {
            audioScope.launch {
                try {
                    val track = AudioTrack.Builder()
                        .setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build()
                        )
                        .setAudioFormat(
                            AudioFormat.Builder()
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setSampleRate(sampleRate)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build()
                        )
                        .setBufferSizeInBytes(popAudioData.size * 2)
                        .setTransferMode(AudioTrack.MODE_STATIC)
                        .build()

                    track.setVolume(soundVolume.coerceIn(0f, 1f))
                    track.write(popAudioData, 0, popAudioData.size)
                    track.play()
                    // Release after playing
                    kotlinx.coroutines.delay(60)
                    track.stop()
                    track.release()
                } catch (e: Exception) {
                    // Ignore sound track failure gracefully
                }
            }
        }
    }
}
