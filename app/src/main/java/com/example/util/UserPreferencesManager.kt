package com.example.util

import android.content.Context
import android.content.SharedPreferences

class UserPreferencesManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "meu_ciclo_user_prefs"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_PRIVACY_DISMISSED = "privacy_notice_dismissed"
        private const val KEY_TOUR_DISMISSED = "calendar_tour_dismissed"

        fun getUserName(context: Context): String {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getString(KEY_USER_NAME, "") ?: ""
        }

        fun setUserName(context: Context, name: String) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_USER_NAME, name.trim()).apply()
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
    }

    var userName: String
        get() = getUserName(context)
        set(value) { setUserName(context, value) }

    var isPrivacyDismissed: Boolean
        get() = isPrivacyNoticeDismissed(context)
        set(value) { setPrivacyNoticeDismissed(context, value) }

    var isTourDismissed: Boolean
        get() = isTourDismissed(context)
        set(value) { setTourDismissed(context, value) }
}
