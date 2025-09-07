package com.singularis.eateria

import android.app.Application
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
