package com.singularis.eateria.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.singularis.eateria.services.Localization
import com.singularis.eateria.services.NotificationHelper
import com.singularis.eateria.services.ReminderScheduler
import com.singularis.eateria.services.ReminderService
import kotlinx.coroutines.flow.first

class FoodReminderWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    companion object {
        const val KEY_TYPE = "type"
        const val TYPE_BREAKFAST = "breakfast"
        const val TYPE_LUNCH = "lunch"
        const val TYPE_DINNER = "dinner"
        private const val ID_BREAKFAST = 1001
        private const val ID_LUNCH = 1002
        private const val ID_DINNER = 1003
        private const val NOON_MS = 12 * 60 * 60 * 1000L
        private const val H17_MS = 17 * 60 * 60 * 1000L
        private const val H21_MS = 21 * 60 * 60 * 1000L
    }

    override suspend fun doWork(): Result {
        val type = inputData.getString(KEY_TYPE) ?: return Result.success()
        val reminderService = ReminderService(applicationContext)
        val enabled = reminderService.notificationsEnabled.first()
        if (!enabled) return Result.success()

        val lastSnap = reminderService.lastSnapTime.first()
        val firstSnapToday = reminderService.firstSnapToday.first()
        val cal =
            java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
        val todayStart = cal.timeInMillis
        val reference = if (firstSnapToday >= todayStart) firstSnapToday else lastSnap
        val shouldNotify =
            when (type) {
                TYPE_BREAKFAST -> reference < todayStart + NOON_MS
                TYPE_LUNCH -> reference < todayStart + H17_MS
                TYPE_DINNER -> reference < todayStart + H21_MS
                else -> false
            }

        if (shouldNotify) {
            val prefix =
                Localization.tr(
                    applicationContext,
                    "notif.reminder.message",
                    "Can I remind you to snap your food? This helps maintain healthy habits.",
                )
            val title =
                when (type) {
                    TYPE_BREAKFAST -> Localization.tr(applicationContext, "notif.breakfast.title", "Breakfast reminder")
                    TYPE_LUNCH -> Localization.tr(applicationContext, "notif.lunch.title", "Lunch reminder")
                    else -> Localization.tr(applicationContext, "notif.dinner.title", "Dinner reminder")
                }
            val id =
                when (type) {
                    TYPE_BREAKFAST -> ID_BREAKFAST
                    TYPE_LUNCH -> ID_LUNCH
                    else -> ID_DINNER
                }
            NotificationHelper.showReminder(applicationContext, id, title, prefix)
        }
        return Result.success()
    }
}
