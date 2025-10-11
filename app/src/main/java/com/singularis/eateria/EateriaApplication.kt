package com.singularis.eateria

import android.app.Application
import com.singularis.eateria.services.AppSettingsService
import com.singularis.eateria.services.HapticsService
import com.singularis.eateria.services.NotificationHelper
import com.singularis.eateria.services.ReminderScheduler
import com.singularis.eateria.services.ReminderService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class EateriaApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize services
        AppSettingsService.initialize(this)
        HapticsService.initialize(this)

        NotificationHelper.ensureChannel(this)
        CoroutineScope(Dispatchers.Default).launch {
            val reminderService = ReminderService(this@EateriaApplication)
            val enabled = reminderService.notificationsEnabled.first()
            if (enabled) {
                ReminderScheduler.scheduleAll(this@EateriaApplication)
            }
        }
    }
}
