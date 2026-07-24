package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class CycleAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Lembrete do Ciclo"
        val message = intent.getStringExtra("message") ?: "Sua menstruação está prevista para breve."

        NotificationHelper.showNotification(context, title, message)
    }
}
