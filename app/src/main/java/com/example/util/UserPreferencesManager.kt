package com.example.util

import android.content.Context
import android.content.SharedPreferences

class UserPreferencesManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        const val PREFS_NAME = "meu_ciclo_user_prefs"
        const val DEFAULT_USER_NAME = "Giovanna"

        private const val KEY_USER_NAME = "user_name"
        private const val KEY_PRIVACY_DISMISSED = "privacy_notice_dismissed"
        private const val KEY_TOUR_DISMISSED = "calendar_tour_dismissed"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_PRIVATE_NOTIFICATIONS = "private_notifications"
        private const val KEY_NOTIFY_3_DAYS = "notify_3_days_before"
        private const val KEY_NOTIFY_DAY_OF = "notify_day_of_period"
        private const val KEY_NOTIFY_LATE = "notify_late_period"
        private const val KEY_NOTIFY_REMINDER = "notify_daily_reminder"
        private const val KEY_NOTIF_HOUR = "notification_hour"
        private const val KEY_NOTIF_MINUTE = "notification_minute"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"

        fun getUserName(context: Context): String {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val name = prefs.getString(KEY_USER_NAME, null)
            return if (name.isNullOrBlank()) DEFAULT_USER_NAME else name.trim()
        }

        fun setUserName(context: Context, name: String) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val finalName = if (name.isBlank()) DEFAULT_USER_NAME else name.trim()
            prefs.edit().putString(KEY_USER_NAME, finalName).apply()
        }

        fun isPrivacyNoticeDismissed(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_PRIVACY_DISMISSED, false)
        }

        fun setPrivacyNoticeDismissed(context: Context, dismissed: Boolean) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_PRIVACY_DISMISSED, dismissed).apply()
        }

        fun isTourDismissed(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_TOUR_DISMISSED, false)
        }

        fun setTourDismissed(context: Context, dismissed: Boolean) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_TOUR_DISMISSED, dismissed).apply()
        }

        fun isNotificationsEnabled(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        }

        fun setNotificationsEnabled(context: Context, enabled: Boolean) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
        }

        fun isPrivateNotifications(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_PRIVATE_NOTIFICATIONS, false)
        }

        fun setPrivateNotifications(context: Context, isPrivate: Boolean) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_PRIVATE_NOTIFICATIONS, isPrivate).apply()
        }

        fun isNotify3DaysBefore(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_NOTIFY_3_DAYS, true)
        }

        fun setNotify3DaysBefore(context: Context, enabled: Boolean) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_NOTIFY_3_DAYS, enabled).apply()
        }

        fun isNotifyDayOfPeriod(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_NOTIFY_DAY_OF, true)
        }

        fun setNotifyDayOfPeriod(context: Context, enabled: Boolean) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_NOTIFY_DAY_OF, enabled).apply()
        }

        fun isNotifyDailyReminder(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_NOTIFY_REMINDER, false)
        }

        fun setNotifyDailyReminder(context: Context, enabled: Boolean) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_NOTIFY_REMINDER, enabled).apply()
        }

        fun getNotificationTime(context: Context): Pair<Int, Int> {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val hour = prefs.getInt(KEY_NOTIF_HOUR, 9)
            val minute = prefs.getInt(KEY_NOTIF_MINUTE, 0)
            return Pair(hour, minute)
        }

        fun setNotificationTime(context: Context, hour: Int, minute: Int) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putInt(KEY_NOTIF_HOUR, hour).putInt(KEY_NOTIF_MINUTE, minute).apply()
        }

        fun getAllPreferencesMap(context: Context): Map<String, String> {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.all.mapValues { it.value.toString() }
        }

        fun restorePreferencesMap(context: Context, map: Map<String, String>) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val editor = prefs.edit()
            for ((key, value) in map) {
                when {
                    value.equals("true", ignoreCase = true) -> editor.putBoolean(key, true)
                    value.equals("false", ignoreCase = true) -> editor.putBoolean(key, false)
                    value.toIntOrNull() != null -> editor.putInt(key, value.toInt())
                    else -> editor.putString(key, value)
                }
            }
            editor.apply()
        }
    }
}
