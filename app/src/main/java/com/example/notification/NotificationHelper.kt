package com.example.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

object NotificationHelper {

    const val CHANNEL_ID = "meu_ciclo_notifications"
    const val CHANNEL_NAME = "Lembretes do Ciclo Menstrual"
    const val NOTIFICATION_ID = 1001
    const val ALARM_REQUEST_CODE = 2002

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações para previsão de menstruação e período fértil"
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showNotification(context: Context, title: String, message: String) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            val manager = NotificationManagerCompat.from(context)
            manager.notify(NOTIFICATION_ID, builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun scheduleNextPeriodReminder(context: Context, nextPeriodDate: LocalDate?) {
        if (nextPeriodDate == null) return

        val reminderDate = nextPeriodDate.minusDays(3)
        val today = LocalDate.now()

        // Check if period is within 3 days or today right now upon opening the app
        val daysUntilPeriod = ChronoUnit.DAYS.between(today, nextPeriodDate)
        if (daysUntilPeriod in 0..3) {
            val message = when (daysUntilPeriod) {
                0L -> "Sua menstruação está prevista para hoje!"
                1L -> "Sua menstruação está prevista para amanhã."
                else -> "Sua menstruação está prevista para daqui a $daysUntilPeriod dias."
            }
            showNotification(
                context,
                "Lembrete do Ciclo",
                message
            )
        }

        // Schedule future alarm for 3 days before next period at 09:00 AM
        if (reminderDate.isAfter(today)) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val alarmIntent = Intent(context, CycleAlarmReceiver::class.java).apply {
                putExtra("title", "Lembrete de Ciclo")
                putExtra("message", "Sua próxima menstruação está prevista para daqui a 3 dias.")
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                ALARM_REQUEST_CODE,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmTimeMillis = reminderDate.atTime(9, 0)
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
