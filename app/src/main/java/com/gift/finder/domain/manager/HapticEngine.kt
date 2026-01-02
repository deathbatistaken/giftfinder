package com.gift.finder.domain.manager

import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.gift.finder.data.manager.PreferencesManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Engine for custom haptic feedback patterns.
 */
@Singleton
class HapticEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: PreferencesManager
) {
    private val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    /**
     * Triggers a success haptic pattern (two short pulses).
     */
    suspend fun success() {
        if (!preferencesManager.hapticsEnabled.first()) return
        
        val pattern = longArrayOf(0, 50, 50, 50)
        val amplitudes = intArrayOf(0, 150, 0, 255)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }

    /**
     * Triggers a heartbeat haptic pattern (double pulse like a heart).
     */
    suspend fun matchOccurred() {
        if (!preferencesManager.hapticsEnabled.first()) return
        
        val pattern = longArrayOf(0, 40, 60, 100)
        val amplitudes = intArrayOf(0, 100, 0, 200)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }

    /**
     * Triggers a subtle tap pulse.
     */
    suspend fun tap() {
        if (!preferencesManager.hapticsEnabled.first()) return
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(20)
        }
    }

    /**
     * Triggers a spin haptic pattern (vibrates with increasing intensity).
     */
    suspend fun spin() {
        if (!preferencesManager.hapticsEnabled.first()) return
        
        // A single short pulse for each tick of the wheel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(10, 100))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(10)
        }
    }

    /**
     * Triggers a celebration haptic pattern (a series of joyful pulses).
     */
    suspend fun celebration() {
        if (!preferencesManager.hapticsEnabled.first()) return
        
        val pattern = longArrayOf(0, 40, 40, 40, 40, 100)
        val amplitudes = intArrayOf(0, 150, 0, 200, 0, 255)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }

    /**
     * Triggers an error/warning pattern.
     */
    suspend fun error() {

        if (!preferencesManager.hapticsEnabled.first()) return
        
        val pattern = longArrayOf(0, 100, 50, 300)
        
        @Suppress("DEPRECATION")
        vibrator.vibrate(pattern, -1)
    }
}
