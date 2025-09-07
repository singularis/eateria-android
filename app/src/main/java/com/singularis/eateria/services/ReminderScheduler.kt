package com.singularis.eateria.services

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.singularis.eateria.work.FoodReminderWorker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    private const val WORK_BREAKFAST = "work_breakfast"
    private const val WORK_LUNCH = "work_lunch"
    private const val WORK_DINNER = "work_dinner"
    private const val TAG_REMINDER = "tag_reminder"
    private const val TAG_BREAKFAST = "tag_meal_breakfast"
    private const val TAG_LUNCH = "tag_meal_lunch"
    private const val TAG_DINNER = "tag_meal_dinner"

    fun scheduleAll(context: Context) {
        scheduleNextNDays(context, days = 7)
    }

    fun cancelAll(context: Context) {
        val wm = WorkManager.getInstance(context)
        wm.cancelAllWorkByTag(TAG_REMINDER)
    }

    fun scheduleBreakfast(context: Context) {
        scheduleAt(context, hour = 12, minute = 0, type = FoodReminderWorker.TYPE_BREAKFAST, uniqueName = WORK_BREAKFAST)
    }

    fun scheduleLunch(context: Context) {
        scheduleAt(context, hour = 17, minute = 0, type = FoodReminderWorker.TYPE_LUNCH, uniqueName = WORK_LUNCH)
    }

    fun scheduleDinner(context: Context) {
        scheduleAt(context, hour = 21, minute = 0, type = FoodReminderWorker.TYPE_DINNER, uniqueName = WORK_DINNER)
    }

    private fun scheduleAt(
        context: Context,
        hour: Int,
        minute: Int,
        type: String,
        uniqueName: String,
    ) {
        val delayMs = millisUntilNext(hour, minute)
        val builder =
            OneTimeWorkRequestBuilder<FoodReminderWorker>()
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .setInputData(workDataOf(FoodReminderWorker.KEY_TYPE to type))
                .addTag(TAG_REMINDER)
                .addTag(tagForMeal(type))
                .addTag(tagForDateFromDelay(delayMs))
        val request = builder.build()
        WorkManager.getInstance(context).enqueueUniqueWork(uniqueName, ExistingWorkPolicy.REPLACE, request)
    }

    private fun millisUntilNext(
        hour: Int,
        minute: Int,
    ): Long {
        val now = Calendar.getInstance()
        val next =
            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= now.timeInMillis) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }
        return next.timeInMillis - now.timeInMillis
    }

    fun scheduleNextNDays(
        context: Context,
        days: Int,
    ) {
        val wm = WorkManager.getInstance(context)
        val now = System.currentTimeMillis()
        val today =
            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        for (i in 0 until days) {
            val dayCal = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, i) }
            scheduleForDay(context, wm, dayCal, FoodReminderWorker.TYPE_BREAKFAST, 12, 0)
            scheduleForDay(context, wm, dayCal, FoodReminderWorker.TYPE_LUNCH, 17, 0)
            scheduleForDay(context, wm, dayCal, FoodReminderWorker.TYPE_DINNER, 21, 0)
        }
    }

    private fun scheduleForDay(
        context: Context,
        wm: WorkManager,
        dayCal: Calendar,
        type: String,
        hour: Int,
        minute: Int,
    ) {
        val target =
            (dayCal.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        val delayMs = target.timeInMillis - System.currentTimeMillis()
        if (delayMs <= 0) return
        val uniqueName = uniqueNameFor(type, target.timeInMillis)
        val request =
            OneTimeWorkRequestBuilder<FoodReminderWorker>()
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .setInputData(workDataOf(FoodReminderWorker.KEY_TYPE to type))
                .addTag(TAG_REMINDER)
                .addTag(tagForMeal(type))
                .addTag(tagForDate(target.timeInMillis))
                .build()
        wm.enqueueUniqueWork(uniqueName, ExistingWorkPolicy.REPLACE, request)
    }

    private fun uniqueNameFor(
        type: String,
        timeInMillis: Long,
    ): String {
        val date = SimpleDateFormat("yyyyMMdd", Locale.US).format(java.util.Date(timeInMillis))
        return when (type) {
            FoodReminderWorker.TYPE_BREAKFAST -> "${WORK_BREAKFAST}_$date"
            FoodReminderWorker.TYPE_LUNCH -> "${WORK_LUNCH}_$date"
            else -> "${WORK_DINNER}_$date"
        }
    }

    private fun tagForMeal(type: String): String =
        when (type) {
            FoodReminderWorker.TYPE_BREAKFAST -> TAG_BREAKFAST
            FoodReminderWorker.TYPE_LUNCH -> TAG_LUNCH
            else -> TAG_DINNER
        }

    private fun tagForDateFromDelay(delayMs: Long): String {
        val target = System.currentTimeMillis() + delayMs
        return tagForDate(target)
    }

    private fun tagForDate(timeInMillis: Long): String {
        val date = SimpleDateFormat("yyyyMMdd", Locale.US).format(java.util.Date(timeInMillis))
        return "tag_day_$date"
    }

    fun cancelToday(context: Context) {
        val wm = WorkManager.getInstance(context)
        val tag = tagForDate(System.currentTimeMillis())
        wm.cancelAllWorkByTag(tag)
    }

    fun cancelNextUpcomingToday(context: Context) {
        val wm = WorkManager.getInstance(context)
        val now = Calendar.getInstance()
        val today =
            (now.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

        val times =
            listOf(
                Triple(FoodReminderWorker.TYPE_BREAKFAST, 12, 0),
                Triple(FoodReminderWorker.TYPE_LUNCH, 17, 0),
                Triple(FoodReminderWorker.TYPE_DINNER, 21, 0),
            )

        for ((type, hour, minute) in times) {
            val target =
                (today.clone() as Calendar).apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
            if (target.timeInMillis > now.timeInMillis) {
                val uniqueName = uniqueNameFor(type, target.timeInMillis)
                wm.cancelUniqueWork(uniqueName)
                return
            }
        }
    }
}
