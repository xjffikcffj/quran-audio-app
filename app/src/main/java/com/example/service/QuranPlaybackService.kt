
package com.example.service

import android.content.Context
import android.content.Intent
import android.app.PendingIntent
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.MainActivity
import com.example.QuranApp
import com.example.R
import com.example.timer.SleepTimerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@UnstableApi
class QuranPlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private var notificationScope: CoroutineScope? = null

    companion object {
        const val TAG = "QuranPlaybackService"
        const val NOTIFICATION_ID = 1001

        const val ACTION_PLAY =
            "com.example.quranaudio.action.PLAY"

        const val ACTION_PAUSE =
            "com.example.quranaudio.action.PAUSE"

        private const val TIMER_MARKER = " • متبقي "

        @Volatile
        private var sharedPlayer: ExoPlayer? = null

        @Volatile
        private var sharedMediaSession: MediaSession? = null

        @Synchronized
        fun getSharedPlayer(context: Context): ExoPlayer {
            if (sharedPlayer == null) {
                Log.d(TAG, "Creating new singleton ExoPlayer instance")

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
                    Log.d(TAG, "Building shared MediaSession")

                    MediaSession.Builder(appContext, player)
                        .setId("QuranAudioSession_${System.nanoTime()}")
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
                    .setId("QuranAudioFallback_${System.nanoTime()}")
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

    /*
     * The provider builds the media notification.
     * The timer text is calculated when the notification is rebuilt,
     * without replacing the current media item.
     */
    private val timerNotificationProvider:
        DefaultMediaNotificationProvider by lazy {

        object : DefaultMediaNotificationProvider(
            this,
            DefaultMediaNotificationProvider.NotificationIdProvider {
                NOTIFICATION_ID
            },
            QuranApp.PLAYBACK_CHANNEL_ID,
            R.string.app_name
        ) {

            override fun getNotificationContentText(
                metadata: MediaMetadata
            ): CharSequence? {

                val artist = metadata.artist
                    ?.toString()
                    ?.substringBefore(TIMER_MARKER)
                    ?.takeIf { it.isNotBlank() }
                    ?: "تلاوة القرآن الكريم"

                return if (SleepTimerManager.isTimerActive.value) {
                    artist + TIMER_MARKER +
                        SleepTimerManager.formatTime(
                            SleepTimerManager.remainingSeconds.value
                        )
                } else {
                    artist
                }
            }
        }.apply {
            setSmallIcon(R.mipmap.ic_launcher)
        }
    }

    private fun requestNotificationUpdate() {
        try {
            triggerNotificationUpdate()
        } catch (e: Exception) {
            Log.w(
                TAG,
                "Notification update skipped: ${e.message}",
                e
            )
        }
    }

    private val servicePlayerListener =
        object : Player.Listener {

            override fun onPlaybackStateChanged(playbackState: Int) {
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
                    Log.i(TAG, "[PlaybackDiagnostics] Playback ended")
                }

                requestNotificationUpdate()
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

                if (playWhenReady) {
                    SleepTimerManager.resumeTimer()
                } else {
                    SleepTimerManager.pauseTimer()
                }

                requestNotificationUpdate()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                Log.i(
                    TAG,
                    "[PlaybackDiagnostics] isPlaying changed: $isPlaying"
                )

                requestNotificationUpdate()
            }

            override fun onMediaItemTransition(
                mediaItem: MediaItem?,
                reason: Int
            ) {
                val title = mediaItem
                    ?.mediaMetadata
                    ?.title
                    ?.toString()
                    ?: "القرآن الكريم"

                Log.i(
                    TAG,
                    "[PlaybackDiagnostics] Transition to: " +
                        "$title (reason=$reason)"
                )

                requestNotificationUpdate()
            }

            override fun onPlayerError(error: PlaybackException) {
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

        // Use Media3's notification manager and our custom timer text.
        setMediaNotificationProvider(timerNotificationProvider)

        val session = getSharedSession(this)
        mediaSession = session
        session.player.addListener(servicePlayerListener)

        notificationScope =
            CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

        notificationScope?.launch {
            combine(
                SleepTimerManager.isTimerActive,
                SleepTimerManager.remainingSeconds
            ) { isActive, seconds ->
                isActive to seconds
            }
                .distinctUntilChanged()
                .collect {
                    requestNotificationUpdate()
                }
        }
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        Log.i(
            TAG,
            "QuranPlaybackService onStartCommand (startId=$startId)"
        )

        val player = sharedPlayer

        when (intent?.action) {
            ACTION_PLAY -> {
                if (player != null) {
                    if (player.playbackState == Player.STATE_ENDED) {
                        player.seekTo(0)
                    }

                    player.play()
                }

                requestNotificationUpdate()
                return START_STICKY
            }

            ACTION_PAUSE -> {
                player?.pause()
                requestNotificationUpdate()
                return START_STICKY
            }
        }

        requestNotificationUpdate()
        return START_STICKY
    }

    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo
    ): MediaSession? {
        return mediaSession ?: getSharedSession(this)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
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
            Log.i(TAG, "Preserving foreground service")
        }
    }

    override fun onDestroy() {
        Log.i(TAG, "QuranPlaybackService onDestroy")

        notificationScope?.cancel()
        notificationScope = null

        sharedPlayer?.removeListener(servicePlayerListener)
        mediaSession = null

        super.onDestroy()
    }
}
