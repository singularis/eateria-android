package com.singularis.eateria.services

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class MascotState {
    HAPPY, ANGRY, BAD_FOOD, GYM, ALCOHOL
}

enum class AppMascot(val value: String, val displayName: String, val icon: String) {
    NONE("none", "Default", "star.fill"),
    CAT("cat", "Cat", "🐱"),
    DOG("dog", "Root", "🐶");

    companion object {
        fun fromValue(value: String?): AppMascot {
            return values().find { it.value == value } ?: NONE
        }
    }

    fun images(state: MascotState): List<String> {
        return when (this) {
            NONE -> emptyList()
            CAT -> when (state) {
                MascotState.HAPPY -> listOf("british_cat_happy", "british_cat_excited", "british_cat_food_bowl")
                MascotState.BAD_FOOD, MascotState.ANGRY -> listOf("british_cat_bad_food")
                MascotState.GYM -> listOf("british_cat_gym")
                MascotState.ALCOHOL -> listOf("british_cat_alcohol")
            }
            DOG -> when (state) {
                MascotState.HAPPY -> listOf("french_bulldog_happy", "french_bulldog_toys", "french_bulldog_duck", "french_bulldog_coconut")
                MascotState.BAD_FOOD, MascotState.ANGRY -> listOf("french_bulldog_bad_food")
                MascotState.GYM -> listOf("french_bulldog_gym", "french_bulldog_towel")
                MascotState.ALCOHOL -> listOf("french_bulldog_alcohol")
            }
        }
    }

    fun icon(systemIcon: String): String {
        return when (this) {
            NONE -> systemIcon
            CAT -> when (systemIcon) {
                "checkmark.circle.fill" -> "pawprint.circle.fill"
                "flame.fill" -> "fish.fill"
                "figure.run" -> "hare.fill"
                "trophy.fill" -> "crown.fill"
                "heart.fill" -> "suit.heart.fill"
                "wineglass", "wineglass.fill" -> "pawprint.circle.fill"
                else -> systemIcon
            }
            DOG -> when (systemIcon) {
                "checkmark.circle.fill" -> "pawprint.circle.fill"
                "flame.fill" -> "pawprint.fill"
                "figure.run" -> "hare.fill"
                "trophy.fill" -> "medal.fill"
                "heart.fill" -> "suit.heart.fill"
                "wineglass", "wineglass.fill" -> "pawprint.circle.fill"
                else -> systemIcon
            }
        }
    }
}

class ThemeService private constructor(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("eateria_prefs", Context.MODE_PRIVATE)

    private val _currentMascot = MutableStateFlow(AppMascot.fromValue(prefs.getString("app_mascot", "none")))
    val currentMascotFlow: StateFlow<AppMascot> = _currentMascot.asStateFlow()

    var currentMascot: AppMascot
        get() = _currentMascot.value
        set(value) {
            prefs.edit().putString("app_mascot", value.value).apply()
            _currentMascot.value = value
        }

    private val _soundEnabled = MutableStateFlow(prefs.getBoolean("theme_sound_enabled", true))
    val soundEnabledFlow: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    var soundEnabled: Boolean
        get() = _soundEnabled.value
        set(value) {
            prefs.edit().putBoolean("theme_sound_enabled", value).apply()
            _soundEnabled.value = value
        }

    private var mediaPlayer: MediaPlayer? = null

    companion object {
        @Volatile
        private var instance: ThemeService? = null

        fun initialize(context: Context) {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = ThemeService(context.applicationContext)
                    }
                }
            }
        }

        fun getInstance(): ThemeService {
            return instance ?: throw IllegalStateException("ThemeService is not initialized, call initialize(Context) first.")
        }
    }

    fun getMotivationalMessage(action: String, language: String = "en"): String {
        return when (currentMascot) {
            AppMascot.NONE -> getDefaultMessage(action, language)
            AppMascot.CAT -> getCatMessage(action, language)
            AppMascot.DOG -> getDogMessage(action, language)
        }
    }

    private fun getDefaultMessage(action: String, language: String): String {
        return when (action) {
            "food_logged" -> if (language == "uk") "Їжа записана!" else "Food Logged!"
            "activity_recorded" -> if (language == "uk") "Активність записана!" else "Activity Recorded!"
            "goal_reached" -> if (language == "uk") "Мета досягнута!" else "Goal Reached!"
            else -> if (language == "uk") "Чудово!" else "Great!"
        }
    }

    private fun getCatMessage(action: String, language: String): String {
        return when (action) {
            "food_logged", "good_food" -> if (language == "uk") "Мур-мур! Смачного! 🐱" else "Meow! Enjoy your meal! 🐱"
            "bad_food", "sugar", "alcohol" -> if (language == "uk") "Фр-р-р! Це не здорова їжа! 😾" else "Hiss! That's not healthy! 😾"
            "activity_recorded" -> if (language == "uk") "Гарний котик! Продовжуй рухатись! 🐾" else "Good kitty! Keep moving! 🐾"
            "goal_reached" -> if (language == "uk") "Мур-р-р! Ти досяг мети! 👑" else "Purr-fect! Goal achieved! 👑"
            "water_logged" -> if (language == "uk") "Лап лап! Води ковток! 💧" else "Lap lap! Water break! 💧"
            "chess_won" -> if (language == "uk") "Мяу! Котик перехитрив! 🐱♟️" else "Meow! Cat outsmarted them! 🐱♟️"
            "loss" -> if (language == "uk") "Мур-р... Наступного разу! 🐾" else "Meow... Next time! 🐾"
            else -> if (language == "uk") "Мур-р! Чудово! 🐱" else "Purr-fect! 🐱"
        }
    }

    private fun getDogMessage(action: String, language: String): String {
        return when (action) {
            "food_logged", "good_food" -> if (language == "uk") "Гав-гав! Смачна їжа! 🐶" else "Woof! Yummy food! 🐶"
            "bad_food", "sugar", "alcohol" -> if (language == "uk") "Гр-р-р! Це погана їжа! 😠" else "Grr! That's bad food! 😠"
            "activity_recorded" -> if (language == "uk") "Гарна робота! Ще гуляти! 🐾" else "Good job! More walkies! 🐾"
            "goal_reached" -> if (language == "uk") "Гав! Ти найкращий! 🏆" else "Woof! You're the best! 🏆"
            "water_logged" -> if (language == "uk") "Хап-хап! Ковток води! 💧" else "Slurp slurp! Water time! 💧"
            "chess_won" -> if (language == "uk") "Гав! Собака виграв! 🐶♟️" else "Woof! Doggo wins! 🐶♟️"
            "loss" -> if (language == "uk") "Гав-гав... Спробуй ще! 🐾" else "Woof... Try again! 🐾"
            else -> if (language == "uk") "Гав! Молодець! 🐶" else "Woof! Great job! 🐶"
        }
    }

    fun playSound(action: String) {
        if (!soundEnabled) return

        val soundName = when (Pair(currentMascot, action)) {
            Pair(AppMascot.CAT, "success"), Pair(AppMascot.CAT, "happy"), Pair(AppMascot.CAT, "good_food"), Pair(AppMascot.CAT, "activity") -> "cat_happy"
            Pair(AppMascot.DOG, "success"), Pair(AppMascot.DOG, "happy"), Pair(AppMascot.DOG, "good_food"), Pair(AppMascot.DOG, "activity") -> "dog_happy"
            Pair(AppMascot.CAT, "error"), Pair(AppMascot.CAT, "angry"), Pair(AppMascot.CAT, "bad_food"), Pair(AppMascot.CAT, "sugar"), Pair(AppMascot.CAT, "alcohol"), Pair(AppMascot.CAT, "loss") -> "cat_hiss"
            Pair(AppMascot.DOG, "error"), Pair(AppMascot.DOG, "angry"), Pair(AppMascot.DOG, "bad_food"), Pair(AppMascot.DOG, "sugar"), Pair(AppMascot.DOG, "alcohol"), Pair(AppMascot.DOG, "loss") -> "dog_growl"
            else -> null
        }

        if (soundName != null) {
            val resId = context.resources.getIdentifier(soundName, "raw", context.packageName)
            if (resId != 0) {
                try {
                    mediaPlayer?.release()
                    mediaPlayer = MediaPlayer.create(context, resId)
                    mediaPlayer?.setVolume(0.5f, 0.5f)
                    mediaPlayer?.start()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun playSoundForFood(healthRating: Int) {
        if (healthRating <= 50) {
            playSound("bad_food")
        } else {
            playSound("good_food")
        }
    }

    fun getMascotImage(state: MascotState): String? {
        val availableImages = currentMascot.images(state)
        if (availableImages.isEmpty()) return null

        if (availableImages.size == 1) return availableImages[0]

        val key = "mascot_rotation_${currentMascot.value}_${state.name}"
        val currentIndex = prefs.getInt(key, 0)

        val imageIndex = currentIndex % availableImages.size
        val selectedImage = availableImages[imageIndex]

        prefs.edit().putInt(key, currentIndex + 1).apply()

        return selectedImage
    }

    fun getMascotImageForAction(action: String): String? {
        return when (action) {
            "gym", "activity_recorded" -> getMascotImage(MascotState.GYM)
            "alcohol" -> getMascotImage(MascotState.ALCOHOL)
            "bad_food" -> getMascotImage(MascotState.BAD_FOOD)
            "sugar", "loss", "error", "angry" -> getMascotImage(MascotState.ANGRY)
            else -> getMascotImage(MascotState.HAPPY)
        }
    }

    fun getUniquePreviewImageNames(count: Int = 5): List<String> {
        if (currentMascot == AppMascot.NONE) return emptyList()

        // Build a unique pool (avoid angry because it often duplicates badFood).
        val poolStates = listOf(MascotState.HAPPY, MascotState.GYM, MascotState.BAD_FOOD, MascotState.ALCOHOL)
        val unique = mutableListOf<String>()
        val seen = mutableSetOf<String>()
        
        for (state in poolStates) {
            for (name in currentMascot.images(state)) {
                if (!seen.contains(name)) {
                    unique.add(name)
                    seen.add(name)
                }
            }
        }

        if (unique.isEmpty()) return emptyList()

        // Rotate across sessions so previews vary, but keep uniqueness within a row.
        val key = "mascot_preview_rotation_${currentMascot.value}"
        val start = prefs.getInt(key, 0) % unique.size
        val take = minOf(count, unique.size)

        val result = mutableListOf<String>()
        for (i in 0 until take) {
            result.add(unique[(start + i) % unique.size])
        }

        prefs.edit().putInt(key, (start + take) % unique.size).apply()
        return result
    }
}
