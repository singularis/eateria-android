package com.singularis.eateria.services

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.singularis.eateria.work.FoodReminderWorker
import java.util.Calendar

object ReminderScheduler {
    private const val WORK_BREAKFAST = "work_breakfast"
    private const val WORK_LUNCH = "work_lunch"
    private const val WORK_DINNER = "work_dinner"

    fun scheduleAll(context: Context) {
        scheduleBreakfast(context)
        scheduleLunch(context)
        scheduleDinner(context)
    }

    fun cancelAll(context: Context) {
        val wm = WorkManager.getInstance(context)
        wm.cancelUniqueWork(WORK_BREAKFAST)
        wm.cancelUniqueWork(WORK_LUNCH)
        wm.cancelUniqueWork(WORK_DINNER)
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

    private fun scheduleAt(context: Context, hour: Int, minute: Int, type: String, uniqueName: String) {
        val delayMs = millisUntilNext(hour, minute)
        val request = OneTimeWorkRequestBuilder<FoodReminderWorker>()
            .setInitialDelay(delayMs, java.util.concurrent.TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(FoodReminderWorker.KEY_TYPE to type))
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(uniqueName, ExistingWorkPolicy.REPLACE, request)
    }

    private fun millisUntilNext(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val next = Calendar.getInstance().apply {
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
}


