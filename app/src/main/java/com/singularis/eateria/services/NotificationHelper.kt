package com.singularis.eateria.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.singularis.eateria.R
import com.singularis.eateria.services.QuotesService
import com.singularis.eateria.services.Localization

object NotificationHelper {
    const val CHANNEL_ID = "eateria_reminders"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = Localization.tr(context, "notif.title", "Eateria Reminder")
            val descriptionText = Localization.tr(context, "notif.description", "Reminders to snap your meals")
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showReminder(context: Context, id: Int, title: String, bodyPrefix: String) {
        ensureChannel(context)
        val quote = QuotesService.getRandomQuote(context)
        val body = "$bodyPrefix\n\n$quote"
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(context)) {
            notify(id, builder.build())
        }
    }
}


