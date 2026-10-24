
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
                    MediaSession.Builder(appContext, player)
                        .setId("QuranAudioSession_${System.nanoTime()}")
                        .setSessionActivity(pendingIntent)
                        .build()
                } catch (e: Exception) {
                    Log.e(TAG, "Error building MediaSession", e)
                    null
                }
            }

            return sharedMediaSession
                ?: throw IllegalStateException(
                    "Unable to create the Quran MediaSession"
                )
        }

        @Synchronized
        fun releaseSharedEngine() {
            Log.d(TAG, "Explicitly releasing shared engine")

            sharedMediaSession?.release()
            sharedMediaSession = null

            sharedPlayer?.release()
            sharedPlayer = null
        }
    }

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
            Log.e(TAG, "Notification update failed", e)
        }
    }

    private val servicePlayerListener =
        object : Player.Listener {

            override fun onPlaybackStateChanged(playbackState: Int) {
                Log.i(
                    TAG,
                    "Playback state=$playbackState, " +
                        "playWhenReady=${sharedPlayer?.playWhenReady}"
                )
                requestNotificationUpdate()
            }

            override fun onPlayWhenReadyChanged(
                playWhenReady: Boolean,
                reason: Int
            ) {
                Log.i(TAG, "playWhenReady=$playWhenReady")

                if (playWhenReady) {
                    SleepTimerManager.resumeTimer()
                } else {
                    SleepTimerManager.pauseTimer()
                }

                requestNotificationUpdate()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                Log.i(TAG, "isPlaying=$isPlaying")
                requestNotificationUpdate()
            }

            override fun onMediaItemTransition(
                mediaItem: MediaItem?,
                reason: Int
            ) {
                Log.i(
                    TAG,
                    "Media transition: " +
                        (mediaItem?.mediaMetadata?.title ?: "القرآن الكريم")
                )
                requestNotificationUpdate()
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e(TAG, "Player error: ${error.errorCodeName}", error)
            }
        }

    override fun onCreate() {
        super.onCreate()

        Log.i(TAG, "QuranPlaybackService onCreate")

        setMediaNotificationProvider(timerNotificationProvider)

        val session = getSharedSession(this)
        mediaSession = session

        // Ensure the playback session is registered with this service.
        if (!isSessionAdded(session)) {
            addSession(session)
        }

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
        Log.i(TAG, "onStartCommand startId=$startId")

        // Preserve Media3's handling of its own service commands.
        val result = super.onStartCommand(intent, flags, startId)

        val player = sharedPlayer

        when (intent?.action) {
            ACTION_PLAY -> {
                if (player != null) {
                    if (player.playbackState == Player.STATE_ENDED) {
                        player.seekTo(0)
                    }
                    player.play()
                }
            }

            ACTION_PAUSE -> {
                player?.pause()
            }
        }

        requestNotificationUpdate()

        return result
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

        Log.i(TAG, "onTaskRemoved isStillPlaying=$isStillPlaying")

        if (!isStillPlaying) {
            stopSelf()
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
