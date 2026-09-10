package com.example.ecofeed.service

import android.content.Context

/**
 * Persistently tracks which notification IDs have already been shown as heads-up popups
 * to prevent duplicate banners when switching screens or re-logging.
 */
class NotificationTracker(context: Context) {
    private val prefs = context.getSharedPreferences("eco_notifications_pref", Context.MODE_PRIVATE)

    fun hasBeenShown(notificationId: String): Boolean {
        if (notificationId.isBlank()) return true
        return prefs.getBoolean("shown_$notificationId", false)
    }

    fun markAsShown(notificationId: String) {
        if (notificationId.isBlank()) return
        prefs.edit().putBoolean("shown_$notificationId", true).apply()
    }
    
    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
