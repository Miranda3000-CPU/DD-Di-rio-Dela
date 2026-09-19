package com.example.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.util.UserPreferencesManager
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

object NotificationHelper {

    const val CHANNEL_PREDICTIONS = "dd_predictions_v2"
    const val CHANNEL_REMINDERS = "dd_reminders_v2"

    const val NOTIFICATION_ID_PREDICTION = 1001
    const val NOTIFICATION_ID_TEST = 1002
    const val NOTIFICATION_ID_REMINDER = 1003

    const val ALARM_REQUEST_PREDICTION = 2001
    const val ALARM_REQUEST_REMINDER = 2002

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.dd_notification}")
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // 1. Predictions Channel
            val predictionsChannel = NotificationChannel(
                CHANNEL_PREDICTIONS,
                context.getString(R.string.notification_channel_predictions_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_channel_predictions_desc)
                enableVibration(true)
                setSound(soundUri, audioAttributes)
            }
            manager.createNotificationChannel(predictionsChannel)

            // 2. Reminders Channel
            val remindersChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                context.getString(R.string.notification_channel_reminders_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notification_channel_reminders_desc)
                enableVibration(true)
                setSound(soundUri, audioAttributes)
            }
            manager.createNotificationChannel(remindersChannel)
        }
    }

    fun showNotification(
        context: Context,
        title: String,
        message: String,
        channelId: String = CHANNEL_PREDICTIONS,
        notificationId: Int = NOTIFICATION_ID_PREDICTION
    ) {
        if (!UserPreferencesManager.isNotificationsEnabled(context)) return

        createNotificationChannels(context)

        val isPrivate = UserPreferencesManager.isPrivateNotifications(context)
        val finalTitle = if (isPrivate) "DD • Diário Dela 🌷" else title
        val finalMessage = if (isPrivate) "Há uma atualização sobre seu ciclo." else message

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_dd_notification)
            .setContentTitle(finalTitle)
            .setContentText(finalMessage)
            .setStyle(NotificationCompat.BigTextStyle().bigText(finalMessage))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            val manager = NotificationManagerCompat.from(context)
            manager.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun sendTestNotification(context: Context) {
        val userName = UserPreferencesManager.getUserName(context)
        val isPrivate = UserPreferencesManager.isPrivateNotifications(context)
        val message = if (isPrivate) {
            "Há uma atualização sobre seu ciclo."
        } else {
            "🌷 Olá, $userName! Esta é uma notificação de teste do seu Diário Dela."
        }

        showNotification(
            context = context,
            title = "DD • Diário Dela 🌷",
            message = message,
            channelId = CHANNEL_PREDICTIONS,
            notificationId = NOTIFICATION_ID_TEST
        )
    }

    fun scheduleNextPeriodReminder(context: Context, nextPeriodDate: LocalDate?) {
        if (!UserPreferencesManager.isNotificationsEnabled(context) || nextPeriodDate == null) return

        val today = LocalDate.now()
        val daysUntil = ChronoUnit.DAYS.between(today, nextPeriodDate)

        // Immediate check upon opening
        if (daysUntil in 0..3) {
            val (title, text) = when (daysUntil) {
                0L -> Pair("🌷 Previsão do Ciclo", "Hoje é o dia estimado para o início da sua menstruação.")
                1L -> Pair("🌷 Previsão do Ciclo", "Sua menstruação está prevista para amanhã.")
                else -> Pair("🌷 Previsão do Ciclo", "Sua menstruação está prevista para daqui a $daysUntil dias.")
            }
            if (daysUntil == 0L && UserPreferencesManager.isNotifyDayOfPeriod(context)) {
                showNotification(context, title, text, CHANNEL_PREDICTIONS, NOTIFICATION_ID_PREDICTION)
            } else if (daysUntil > 0L && UserPreferencesManager.isNotify3DaysBefore(context)) {
                showNotification(context, title, text, CHANNEL_PREDICTIONS, NOTIFICATION_ID_PREDICTION)
            }
        }

        // Schedule future alarm for 3 days before
        val reminderDate = nextPeriodDate.minusDays(3)
        if (reminderDate.isAfter(today) && UserPreferencesManager.isNotify3DaysBefore(context)) {
            val (hour, minute) = UserPreferencesManager.getNotificationTime(context)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val alarmIntent = Intent(context, CycleAlarmReceiver::class.java).apply {
                putExtra("title", "🌷 Previsão do Ciclo")
                putExtra("message", "Sua próxima menstruação está prevista para daqui a 3 dias.")
                putExtra("channelId", CHANNEL_PREDICTIONS)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                ALARM_REQUEST_PREDICTION,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmTimeMillis = reminderDate.atTime(hour, minute)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        alarmTimeMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        alarmTimeMillis,
                        pendingIntent
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
