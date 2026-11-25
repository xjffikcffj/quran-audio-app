package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.ads.AdManager
import com.example.data.DeveloperAuthManager
import com.example.data.DeveloperContentManager
import com.example.data.FavoritesManager
import com.example.ui.theme.ThemeManager

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

        FavoritesManager.initialize(this)
        ThemeManager.initialize(this)
        DeveloperContentManager.initialize(this)
        DeveloperAuthManager.initialize(this)

        // Initialize Google Mobile Ads.
        AdManager.initialize(this)
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

            val notificationManager =
                getSystemService(NotificationManager::class.java)

            notificationManager?.createNotificationChannel(channel)
        }
    }
}
