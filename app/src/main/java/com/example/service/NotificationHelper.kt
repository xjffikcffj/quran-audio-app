package com.example.service

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

/**
 * Handles posting in-app and developer push reminders in compliance with Android 13+ POST_NOTIFICATIONS.
 */
object NotificationHelper {

    const val CHANNEL_ID = "quran_developer_notifications"
    private const val NOTIFICATION_ID = 2001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "إشعارات وتذكيرات التطبيق"
            val descriptionText = "إشعارات وتذكيرات التلاوة وسورة الكهف يوم الجمعة"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Dispatches an immediate notification sent by the developer from the dashboard.
     */
    fun sendDeveloperNotification(context: Context, title: String, message: String): Boolean {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(title.ifEmpty { "تذكير التلاوة المباركة" })
            .setContentText(message.ifEmpty { "لا تنسَ تلاوة وردك اليومي من القرآن الكريم." })
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        return try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
            true
        } catch (e: SecurityException) {
            false
        }
    }
}
