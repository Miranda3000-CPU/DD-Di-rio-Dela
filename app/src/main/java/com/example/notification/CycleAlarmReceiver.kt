package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class CycleAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "DD • Diário Dela 🌷"
        val message = intent.getStringExtra("message") ?: "Lembrete do seu ciclo."
        val channelId = intent.getStringExtra("channelId") ?: NotificationHelper.CHANNEL_PREDICTIONS

        NotificationHelper.showNotification(
            context = context,
            title = title,
            message = message,
            channelId = channelId
        )
    }
}
