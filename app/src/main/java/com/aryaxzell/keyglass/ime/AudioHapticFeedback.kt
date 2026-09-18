package com.aryaxzell.keyglass.ime

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View

class AudioHapticFeedback(private val context: Context) {
    enum class KeyType {
        STANDARD,
        SPACE,
        BACKSPACE,
        RETURN,
        FUNCTION
    }

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val audioManager: AudioManager? = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    fun triggerKeyFeedback(
        view: View?,
        hapticEnabled: Boolean,
        hapticIntensity: Float,
        soundEnabled: Boolean,
        soundVolume: Float,
        keyType: KeyType = KeyType.STANDARD
    ) {
        // Haptic feedback simulating iOS Taptic Engine click response
        if (hapticEnabled) {
            try {
                if (vibrator != null && vibrator.hasVibrator()) {
                    val scale = hapticIntensity.coerceIn(0.1f, 1.0f)
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && vibrator.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_TICK)) {
                        val primitive = when (keyType) {
                            KeyType.SPACE, KeyType.RETURN -> VibrationEffect.Composition.PRIMITIVE_CLICK
                            KeyType.BACKSPACE -> VibrationEffect.Composition.PRIMITIVE_TICK
                            KeyType.FUNCTION -> VibrationEffect.Composition.PRIMITIVE_LOW_TICK
                            KeyType.STANDARD -> VibrationEffect.Composition.PRIMITIVE_TICK
                        }
                        val primitiveScale = when (keyType) {
                            KeyType.STANDARD -> (scale * 0.7f).coerceIn(0.1f, 1.0f)
                            KeyType.FUNCTION -> (scale * 0.5f).coerceIn(0.1f, 1.0f)
                            else -> scale
                        }
                        val effect = VibrationEffect.startComposition()
                            .addPrimitive(primitive, primitiveScale)
                            .compose()
                        vibrator.vibrate(effect)
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val effectType = if (keyType == KeyType.SPACE || keyType == KeyType.RETURN) {
                            VibrationEffect.EFFECT_CLICK
                        } else {
                            VibrationEffect.EFFECT_TICK
                        }
                        vibrator.vibrate(VibrationEffect.createPredefined(effectType))
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val duration = when (keyType) {
                            KeyType.SPACE, KeyType.RETURN -> 14L
                            KeyType.BACKSPACE -> 10L
                            KeyType.FUNCTION -> 6L
                            KeyType.STANDARD -> 8L
                        }
                        val amplitude = (scale * 255).toInt().coerceIn(1, 255)
                        vibrator.vibrate(VibrationEffect.createOneShot(duration, amplitude))
                    } else {
                        view?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    }
                } else {
                    view?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                }
            } catch (e: Exception) {
                view?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            }
        }

        // Zero-allocation native system sound feedback
        if (soundEnabled && soundVolume > 0.05f) {
            try {
                val effect = when (keyType) {
                    KeyType.SPACE -> AudioManager.FX_KEYPRESS_SPACEBAR
                    KeyType.BACKSPACE -> AudioManager.FX_KEYPRESS_DELETE
                    KeyType.RETURN -> AudioManager.FX_KEYPRESS_RETURN
                    else -> AudioManager.FX_KEYPRESS_STANDARD
                }
                audioManager?.playSoundEffect(effect, soundVolume.coerceIn(0f, 1f))
            } catch (e: Exception) {
                // Ignore audio feedback failures
            }
        }
    }
}

