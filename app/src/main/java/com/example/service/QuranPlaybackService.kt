
package com.example.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
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

@UnstableApi
class QuranPlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    companion object {
        const val TAG = "QuranPlaybackService"

        @Volatile
        private var sharedPlayer: ExoPlayer? = null

        @Volatile
        private var sharedMediaSession: MediaSession? = null

        @Synchronized
        fun getSharedPlayer(context: Context): ExoPlayer {
            if (sharedPlayer == null) {
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

                Log.d(TAG, "Shared ExoPlayer created")
            }

            return checkNotNull(sharedPlayer)
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

                sharedMediaSession = MediaSession.Builder(
                    appContext,
                    player
                )
                    .setId("QuranAudioSession")
                    .setSessionActivity(pendingIntent)
                    .build()

                Log.d(TAG, "Shared MediaSession created")
            }

            return checkNotNull(sharedMediaSession)
        }

        @Synchronized
        fun releaseSharedEngine() {
            Log.d(TAG, "Releasing shared playback engine")

            sharedMediaSession?.release()
            sharedMediaSession = null

            sharedPlayer?.release()
            sharedPlayer = null
        }
    }

    private val servicePlayerListener = object : Player.Listener {

        override fun onPlaybackStateChanged(playbackState: Int) {
            val stateName = when (playbackState) {
                Player.STATE_IDLE -> "IDLE"
                Player.STATE_BUFFERING -> "BUFFERING"
                Player.STATE_READY -> "READY"
                Player.STATE_ENDED -> "ENDED"
                else -> "UNKNOWN($playbackState)"
            }

            Log.d(TAG, "Playback state: $stateName")
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            Log.d(TAG, "Is playing: $isPlaying")
        }

        override fun onMediaItemTransition(
            mediaItem: MediaItem?,
            reason: Int
        ) {
            val title = mediaItem?.mediaMetadata?.title
                ?.toString() ?: "القرآن الكريم"

            Log.d(TAG, "Current media item: $title")
        }

        override fun onPlayerError(error: PlaybackException) {
            Log.e(
                TAG,
                "Playback error: ${error.errorCodeName}",
                error
            )
        }
    }

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "QuranPlaybackService created")

        val session = getSharedSession(this)
        mediaSession = session

        session.player.addListener(servicePlayerListener)
    }

    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo
    ): MediaSession {
        return mediaSession ?: getSharedSession(this)
    }

    override fun onDestroy() {
        Log.d(TAG, "QuranPlaybackService destroyed")

        mediaSession?.player?.removeListener(servicePlayerListener)
        mediaSession = null

        super.onDestroy()
    }
}
