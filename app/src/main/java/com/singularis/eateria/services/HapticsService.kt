package com.singularis.eateria.services

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
class HapticsService private constructor(context: Context) {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private fun allowed(): Boolean {
        val motor = vibrator ?: return false
        return motor.hasVibrator()
    }

    /** One-shot and waveforms. Predefined click effects stay silent on many phones. */
    private fun pulse(ms: Long, amplitude: Int) {
        if (!allowed()) return
        val motor = vibrator ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val strength = if (motor.hasAmplitudeControl()) {
                amplitude.coerceIn(1, 255)
            } else {
                VibrationEffect.DEFAULT_AMPLITUDE
            }
            motor.vibrate(VibrationEffect.createOneShot(ms, strength))
        } else {
            @Suppress("DEPRECATION")
            motor.vibrate(ms)
        }
    }

    private fun pattern(timings: LongArray, amplitudes: IntArray) {
        if (!allowed()) return
        val motor = vibrator ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && motor.hasAmplitudeControl()) {
            motor.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            motor.vibrate(VibrationEffect.createWaveform(timings, -1))
        } else {
            @Suppress("DEPRECATION")
            motor.vibrate(timings, -1)
        }
    }

    fun lightImpact() = pulse(12, 80)

    fun mediumImpact() = pulse(22, 140)

    fun heavyImpact() = pulse(40, 255)

    fun success() = pattern(longArrayOf(0, 24, 40, 28), intArrayOf(0, 120, 0, 180))

    fun warning() = pattern(longArrayOf(0, 36, 30, 36), intArrayOf(0, 160, 0, 160))

    fun error() = pattern(longArrayOf(0, 30, 40, 30, 40, 36), intArrayOf(0, 180, 0, 180, 0, 200))

    fun select() = pulse(8, 60)

    /**
     * Buzz after a meal is logged. Cat, dog, and the plain theme each feel
     * different, and a score of 50 or below feels different from a good one.
     */
    fun foodQuality(rating: Int) {
        if (!AppSettingsService.getInstance().vibrationEnabled) return
        val pleasant = rating > 50
        val mascot = runCatching { ThemeService.getInstance().currentMascot }.getOrDefault(AppMascot.NONE)
        when (mascot) {
            AppMascot.CAT -> if (pleasant) {
                pattern(longArrayOf(0, 18, 40, 18, 40, 22, 50, 16), intArrayOf(0, 50, 0, 70, 0, 90, 0, 50))
            } else {
                pattern(longArrayOf(0, 70, 80, 24), intArrayOf(0, 255, 0, 90))
            }
            AppMascot.DOG -> if (pleasant) {
                pattern(longArrayOf(0, 40, 120, 36), intArrayOf(0, 220, 0, 180))
            } else {
                pulse(280, 200)
            }
            else -> if (pleasant) success() else error()
        }
    }

    companion object {
        @Volatile
        private var instance: HapticsService? = null

        fun initialize(context: Context) {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = HapticsService(context.applicationContext)
                    }
                }
            }
        }

        fun getInstance(): HapticsService {
            return instance
                ?: throw IllegalStateException("HapticsService must be initialized first")
        }
    }
}

