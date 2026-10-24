package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class QuranApp : Application() {

    companion object {
        const val PLAYBACK_CHANNEL_ID = "quran_playback_channel"
        lateinit var instance: QuranApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannel()
        com.example.data.FavoritesManager.initialize(this)
        com.example.ui.theme.ThemeManager.initialize(this)
        com.example.data.DeveloperContentManager.initialize(this)
        com.example.data.DeveloperAuthManager.initialize(this)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                PLAYBACK_CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }
}
