package com.example.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.MainActivity
import com.example.QuranApp
import com.example.R

@UnstableApi
class QuranPlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    companion object {
        const val TAG = "QuranPlaybackService"
        const val NOTIFICATION_ID = 1001

        const val ACTION_PLAY =
            "com.example.quranaudio.action.PLAY"
        const val ACTION_PAUSE =
            "com.example.quranaudio.action.PAUSE"

        @Volatile
        private var sharedPlayer: ExoPlayer? = null

        @Volatile
        private var sharedMediaSession: MediaSession? = null

        @Synchronized
        fun getSharedPlayer(context: Context): ExoPlayer {
            if (sharedPlayer == null) {
                Log.d(
                    TAG,
                    "Creating new singleton ExoPlayer instance"
                )

                val appContext = context.applicationContext

                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                    .setUsage(C.USAGE_MEDIA)
                    .build()

                sharedPlayer = ExoPlayer.Builder(appContext)
                    .setAudioAttributes(audioAttributes, true)
                    .setHandleAudioBecomingNoisy(true)
                    .setWakeMode(C.WAKE_MODE_NETWORK)
                    .build()
            }

            return sharedPlayer!!
        }

        @Synchronized
        fun getSharedSession(context: Context): MediaSession {
            if (sharedMediaSession == null) {
                val appContext = context.applicationContext
                val player = getSharedPlayer(appContext)

                val sessionActivityIntent =
                    Intent(appContext, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }

                val pendingIntent = PendingIntent.getActivity(
                    appContext,
                    0,
                    sessionActivityIntent,
                    PendingIntent.FLAG_IMMUTABLE or
                        PendingIntent.FLAG_UPDATE_CURRENT
                )

                sharedMediaSession = try {
                    Log.d(
                        TAG,
                        "Building shared MediaSession"
                    )

                    MediaSession.Builder(appContext, player)
                        .setId(
                            "QuranAudioSession_${System.nanoTime()}"
                        )
                        .setSessionActivity(pendingIntent)
                        .build()
                } catch (e: Exception) {
                    Log.e(
                        TAG,
                        "Error building MediaSession: ${e.message}",
                        e
                    )
                    null
                }
            }

            return sharedMediaSession
                ?: MediaSession.Builder(
                    context.applicationContext,
                    getSharedPlayer(context)
                )
                    .setId(
                        "QuranAudioFallback_${System.nanoTime()}"
                    )
                    .build()
        }

        fun releaseSharedEngine() {
            Log.d(TAG, "Explicitly releasing shared engine")

            sharedMediaSession?.run {
                player.release()
                release()
            }

            sharedMediaSession = null
            sharedPlayer = null
        }
    }

    private val servicePlayerListener =
        object : Player.Listener {

            override fun onPlaybackStateChanged(
                playbackState: Int
            ) {
                val stateName = when (playbackState) {
                    Player.STATE_IDLE -> "STATE_IDLE"
                    Player.STATE_BUFFERING -> "STATE_BUFFERING"
                    Player.STATE_READY -> "STATE_READY"
                    Player.STATE_ENDED -> "STATE_ENDED"
                    else -> "UNKNOWN($playbackState)"
                }

                Log.i(
                    TAG,
                    "[PlaybackDiagnostics] State changed: " +
                        "$stateName, " +
                        "playWhenReady=${sharedPlayer?.playWhenReady}"
                )

                if (playbackState == Player.STATE_ENDED) {
                    Log.i(
                        TAG,
                        "[PlaybackDiagnostics] Playback ended"
                    )
                }

                updateCurrentNotification()
            }

            override fun onPlayWhenReadyChanged(
                playWhenReady: Boolean,
                reason: Int
            ) {
                Log.i(
                    TAG,
                    "[PlaybackDiagnostics] playWhenReady changed: " +
                        "$playWhenReady"
                )

                updateCurrentNotification()
            }

            override fun onIsPlayingChanged(
                isPlaying: Boolean
            ) {
                Log.i(
                    TAG,
                    "[PlaybackDiagnostics] isPlaying changed: " +
                        "$isPlaying"
                )

                updateCurrentNotification()
            }

            override fun onMediaItemTransition(
                mediaItem: MediaItem?,
                reason: Int
            ) {
                val title = mediaItem
                    ?.mediaMetadata
                    ?.title
                    ?.toString() ?: "القرآن الكريم"

                Log.i(
                    TAG,
                    "[PlaybackDiagnostics] Transition to: " +
                        "$title (reason=$reason)"
                )

                updateCurrentNotification()
            }

            override fun onPlayerError(
                error: PlaybackException
            ) {
                Log.e(
                    TAG,
                    "[PlaybackDiagnostics] Player error: " +
                        "${error.errorCodeName} (${error.errorCode})",
                    error
                )

                Log.e(
                    TAG,
                    "[PlaybackDiagnostics] Underlying cause: " +
                        "${error.cause?.message}"
                )
            }
        }

    override fun onCreate() {
        super.onCreate()

        Log.i(TAG, "QuranPlaybackService onCreate")

        val session = getSharedSession(this)
        mediaSession = session
        session.player.addListener(servicePlayerListener)
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        Log.i(
            TAG,
            "QuranPlaybackService onStartCommand " +
                "(startId=$startId)"
        )

        val player = sharedPlayer

        when (intent?.action) {
            ACTION_PLAY -> {
                if (player != null) {
                    if (
                        player.playbackState ==
                        Player.STATE_ENDED
                    ) {
                        player.seekTo(0)
                    }

                    player.play()
                }

                updateCurrentNotification()
                return START_STICKY
            }

            ACTION_PAUSE -> {
                player?.pause()

                updateCurrentNotification()
                return START_STICKY
            }
        }

        updateCurrentNotification()
        return START_STICKY
    }

    private fun updateCurrentNotification() {
        try {
            val player = sharedPlayer ?: return

            val title = player.currentMediaItem
                ?.mediaMetadata
                ?.title
                ?.toString()
                ?: getString(R.string.app_name)

            val artist = player.currentMediaItem
                ?.mediaMetadata
                ?.artist
                ?.toString()
                ?: "تلاوة القرآن الكريم"

            val shouldShowPause =
                player.playWhenReady &&
                    player.playbackState != Player.STATE_ENDED

            val sessionActivityIntent =
                Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                }

            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                sessionActivityIntent,
                PendingIntent.FLAG_IMMUTABLE or
                    PendingIntent.FLAG_UPDATE_CURRENT
            )

            val actionIntent = Intent(
                this,
                QuranPlaybackService::class.java
            ).apply {
                action = if (shouldShowPause) {
                    ACTION_PAUSE
                } else {
                    ACTION_PLAY
                }
            }

            val actionPendingIntent = PendingIntent.getService(
                this,
                if (shouldShowPause) 1 else 2,
                actionIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(
                this,
                QuranApp.PLAYBACK_CHANNEL_ID
            )
                .setContentTitle(title)
                .setContentText(artist)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setOngoing(shouldShowPause)
                .setPriority(
                    NotificationCompat.PRIORITY_LOW
                )
                .setVisibility(
                    NotificationCompat.VISIBILITY_PUBLIC
                )
                .addAction(
                    if (shouldShowPause) {
                        android.R.drawable.ic_media_pause
                    } else {
                        android.R.drawable.ic_media_play
                    },
                    if (shouldShowPause) {
                        "إيقاف مؤقت"
                    } else {
                        "تشغيل"
                    },
                    actionPendingIntent
                )
                .build()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(
                    NOTIFICATION_ID,
                    notification
                )
            }
        } catch (e: Exception) {
            Log.w(
                TAG,
                "Notification update skipped: ${e.message}",
                e
            )
        }
    }

    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo
    ): MediaSession? {
        return mediaSession ?: getSharedSession(this)
    }

    override fun onTaskRemoved(
        rootIntent: Intent?
    ) {
        val player = mediaSession?.player

        val isStillPlaying =
            player?.isPlaying == true ||
                player?.playWhenReady == true

        Log.i(
            TAG,
            "QuranPlaybackService onTaskRemoved: " +
                "isStillPlaying=$isStillPlaying"
        )

        if (!isStillPlaying) {
            Log.i(
                TAG,
                "Stopping service because playback is not active"
            )
            stopSelf()
        } else {
            Log.i(
                TAG,
                "Preserving foreground service"
            )
        }
    }

    override fun onDestroy() {
        Log.i(TAG, "QuranPlaybackService onDestroy")

        sharedPlayer?.removeListener(servicePlayerListener)
        mediaSession = null

        super.onDestroy()
    }
}
