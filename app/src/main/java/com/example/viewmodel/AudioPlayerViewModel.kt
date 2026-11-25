package com.example.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.example.data.QuranData
import com.example.model.Reciter
import com.example.model.Surah
import com.example.service.QuranPlaybackService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class PlaybackStatus {
    IDLE,
    BUFFERING,
    PLAYING,
    PAUSED,
    ERROR
}

data class AudioPlayerUiState(
    val currentSurah: Surah = QuranData.surahs.first(),
    val currentReciter: Reciter = QuranData.reciters.first(),
    val playbackStatus: PlaybackStatus = PlaybackStatus.IDLE,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val isRepeatOne: Boolean = false,
    val errorMessage: String? = null,
    val isPlayerExpanded: Boolean = false
) {
    val progress: Float
        get() = if (durationMs > 0L) {
            (currentPositionMs.toFloat() / durationMs.toFloat())
                .coerceIn(0f, 1f)
        } else {
            0f
        }
}

class AudioPlayerViewModel(
    application: Application
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "AudioPlayerViewModel"
        private const val PREFS_NAME = "quran_playback_preferences"
        private const val KEY_SURAH_NUMBER = "last_surah_number"
        private const val KEY_RECITER_ID = "last_reciter_id"
        private const val KEY_AUDIO_URL = "last_audio_url"
        private const val KEY_POSITION = "last_position_ms"
        private const val KEY_SPEED = "last_playback_speed"
        private const val KEY_REPEAT = "last_repeat_one"
        private const val SURAH_POSITION_PREFIX = "surah_position_"
        private const val SAVE_INTERVAL_MS = 2000L
    }

    private val preferences =
        application.getSharedPreferences(
            PREFS_NAME,
            Application.MODE_PRIVATE
        )

    private val _uiState = MutableStateFlow(
        AudioPlayerUiState(
            currentSurah = getSavedSurah(),
            currentReciter = getSavedReciter(),
            currentPositionMs = getSavedPositionForSurah(
                getSavedSurah().number
            ),
            playbackSpeed = getSavedSpeed(),
            isRepeatOne = preferences.getBoolean(KEY_REPEAT, false)
        )
    )

    val uiState: StateFlow<AudioPlayerUiState> =
        _uiState.asStateFlow()

    private var exoPlayer: ExoPlayer? = null
    private var mediaSession: MediaSession? = null
    private var progressJob: Job? = null

    private fun positionKey(surahNumber: Int): String =
        "$SURAH_POSITION_PREFIX$surahNumber"

    private fun getSavedPositionForSurah(
        surahNumber: Int
    ): Long {
        val key = positionKey(surahNumber)

        return if (preferences.contains(key)) {
            preferences.getLong(key, 0L).coerceAtLeast(0L)
        } else {
            // Migrate the old saved position for the last Surah.
            val oldSurahNumber =
                preferences.getInt(KEY_SURAH_NUMBER, 1)

            if (oldSurahNumber == surahNumber) {
                preferences.getLong(KEY_POSITION, 0L)
                    .coerceAtLeast(0L)
            } else {
                0L
            }
        }
    }

    private val playerListener = object : Player.Listener {

        override fun onPlaybackStateChanged(playbackState: Int) {
            val stateName = when (playbackState) {
                Player.STATE_BUFFERING -> "BUFFERING"
                Player.STATE_READY -> "READY"
                Player.STATE_ENDED -> "ENDED"
                Player.STATE_IDLE -> "IDLE"
                else -> "UNKNOWN"
            }

            Log.d(TAG, "Playback state: $stateName")

            when (playbackState) {
                Player.STATE_BUFFERING -> {
                    _uiState.update {
                        it.copy(
                            playbackStatus = PlaybackStatus.BUFFERING,
                            isBuffering = true,
                            errorMessage = null
                        )
                    }
                }

                Player.STATE_READY -> {
                    val playing = exoPlayer?.isPlaying == true

                    _uiState.update {
                        it.copy(
                            playbackStatus = if (playing) {
                                PlaybackStatus.PLAYING
                            } else {
                                PlaybackStatus.PAUSED
                            },
                            isPlaying = playing,
                            isBuffering = false,
                            durationMs = (exoPlayer?.duration ?: 0L)
                                .coerceAtLeast(0L),
                            errorMessage = null
                        )
                    }

                    savePlaybackState()
                }

                Player.STATE_ENDED -> {
                    if (_uiState.value.isRepeatOne) {
                        exoPlayer?.seekTo(0)
                        exoPlayer?.play()
                    } else {
                        playNext()
                    }
                }

                Player.STATE_IDLE -> {
                    _uiState.update {
                        it.copy(
                            playbackStatus = if (it.errorMessage != null) {
                                PlaybackStatus.ERROR
                            } else {
                                PlaybackStatus.IDLE
                            },
                            isBuffering = false,
                            isPlaying = false
                        )
                    }

                    savePlaybackState()
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _uiState.update {
                it.copy(
                    isPlaying = isPlaying,
                    playbackStatus = if (isPlaying) {
                        PlaybackStatus.PLAYING
                    } else if (
                        it.playbackStatus == PlaybackStatus.BUFFERING
                    ) {
                        PlaybackStatus.BUFFERING
                    } else {
                        PlaybackStatus.PAUSED
                    }
                )
            }

            if (isPlaying) {
                startProgressPolling()
            } else {
                stopProgressPolling()
                savePlaybackState()
            }
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            savePlaybackState()
        }

        override fun onMediaItemTransition(
            mediaItem: MediaItem?,
            reason: Int
        ) {
            val number = mediaItem?.mediaId?.toIntOrNull() ?: return

            val surah = QuranData.surahs.firstOrNull {
                it.number == number
            } ?: return

            val position =
                exoPlayer?.currentPosition?.coerceAtLeast(0L) ?: 0L

            _uiState.update {
                it.copy(
                    currentSurah = surah,
                    currentPositionMs = position
                )
            }

            savePlaybackState(positionOverride = position)
        }

        override fun onPlayerError(error: PlaybackException) {
            Log.e(
                TAG,
                "Player error: ${error.errorCodeName} - ${error.message}",
                error
            )

            _uiState.update {
                it.copy(
                    playbackStatus = PlaybackStatus.ERROR,
                    isPlaying = false,
                    isBuffering = false,
                    errorMessage =
                        "تعذر تحميل التلاوة. يرجى التحقق من اتصال الإنترنت والمحاولة مرة أخرى."
                )
            }

            stopProgressPolling()
            savePlaybackState()
        }
    }

    init {
        initializeExoPlayer()
    }

    private fun getSavedSurah(): Surah {
        val number = preferences.getInt(KEY_SURAH_NUMBER, 1)

        return QuranData.surahs.firstOrNull {
            it.number == number
        } ?: QuranData.surahs.first()
    }

    private fun getSavedReciter(): Reciter {
        val savedId = preferences.getString(KEY_RECITER_ID, null)

        return QuranData.reciters.firstOrNull {
            it.id.toString() == savedId
        } ?: QuranData.reciters.first()
    }

    private fun getSavedSpeed(): Float {
        val speed = preferences.getFloat(KEY_SPEED, 1.0f)

        return if (speed in 0.25f..3.0f) {
            speed
        } else {
            1.0f
        }
    }

    private fun initializeExoPlayer() {
        val context = getApplication<Application>()

        Log.i(TAG, "Initializing shared Quran player")

        val player = QuranPlaybackService.getSharedPlayer(context)

        exoPlayer = player
        mediaSession = QuranPlaybackService.getSharedSession(context)

        player.addListener(playerListener)

        player.playbackParameters =
            PlaybackParameters(_uiState.value.playbackSpeed)

        if (player.currentMediaItem != null) {
            syncWithActivePlayer(player)
        } else {
            restoreSavedPlayback(player)
        }
    }

    private fun restoreSavedPlayback(player: ExoPlayer) {
        val surah = getSavedSurah()
        val reciter = getSavedReciter()

        val savedUrl = preferences.getString(KEY_AUDIO_URL, null)
            ?.takeIf { it.isNotBlank() }
            ?: surah.audioUrl

        val savedPosition =
            getSavedPositionForSurah(surah.number)

        val metadata = MediaMetadata.Builder()
            .setTitle("سورة ${surah.nameArabic}")
            .setArtist(reciter.nameArabic)
            .setAlbumTitle("القرآن الكريم")
            .setDisplayTitle("سورة ${surah.nameArabic}")
            .setSubtitle(reciter.nameArabic)
            .build()

        val mediaItem = MediaItem.Builder()
            .setMediaId(surah.number.toString())
            .setUri(Uri.parse(savedUrl))
            .setMediaMetadata(metadata)
            .build()

        try {
            player.setMediaItem(mediaItem, savedPosition)
            player.prepare()

            player.playbackParameters =
                PlaybackParameters(_uiState.value.playbackSpeed)

            _uiState.update {
                it.copy(
                    currentSurah = surah,
                    currentReciter = reciter,
                    currentPositionMs = savedPosition,
                    playbackStatus = PlaybackStatus.BUFFERING,
                    isPlaying = false,
                    isBuffering = true,
                    errorMessage = null
                )
            }

            Log.i(
                TAG,
                "Restored ${surah.nameArabic} at $savedPosition ms without autoplay"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Could not restore saved playback", e)

            _uiState.update {
                it.copy(
                    playbackStatus = PlaybackStatus.ERROR,
                    isPlaying = false,
                    isBuffering = false,
                    errorMessage = "تعذر استعادة التلاوة المحفوظة."
                )
            }
        }
    }

    private fun syncWithActivePlayer(player: ExoPlayer) {
        val mediaItem = player.currentMediaItem ?: return
        val number = mediaItem.mediaId.toIntOrNull() ?: return

        val matchingSurah = QuranData.surahs.firstOrNull {
            it.number == number
        } ?: return

        val savedReciter = getSavedReciter()
        val playing = player.isPlaying

        _uiState.update {
            it.copy(
                currentSurah = matchingSurah,
                currentReciter = savedReciter,
                isPlaying = playing,
                isBuffering =
                    player.playbackState == Player.STATE_BUFFERING,
                playbackStatus = when {
                    playing -> PlaybackStatus.PLAYING

                    player.playbackState == Player.STATE_BUFFERING ->
                        PlaybackStatus.BUFFERING

                    player.playbackState == Player.STATE_READY ->
                        PlaybackStatus.PAUSED

                    else -> PlaybackStatus.IDLE
                },
                currentPositionMs =
                    player.currentPosition.coerceAtLeast(0L),
                durationMs = player.duration.coerceAtLeast(0L),
                playbackSpeed = player.playbackParameters.speed
            )
        }

        if (playing) {
            startProgressPolling()
        }

        savePlaybackState(
            positionOverride = player.currentPosition.coerceAtLeast(0L)
        )
    }

    private fun savePlaybackState(
        audioUrlOverride: String? = null,
        positionOverride: Long? = null
    ) {
        val state = _uiState.value
        val player = exoPlayer

        val actualUrl = audioUrlOverride
            ?: player?.currentMediaItem
                ?.localConfiguration?.uri?.toString()
            ?: preferences.getString(KEY_AUDIO_URL, null)
            ?: state.currentSurah.audioUrl

        val position = positionOverride
            ?: player?.currentPosition?.coerceAtLeast(0L)
            ?: state.currentPositionMs.coerceAtLeast(0L)

        preferences.edit()
            .putInt(KEY_SURAH_NUMBER, state.currentSurah.number)
            .putString(
                KEY_RECITER_ID,
                state.currentReciter.id.toString()
            )
            .putString(KEY_AUDIO_URL, actualUrl)
            .putLong(KEY_POSITION, position)
            .putLong(
                positionKey(state.currentSurah.number),
                position
            )
            .putFloat(KEY_SPEED, state.playbackSpeed)
            .putBoolean(KEY_REPEAT, state.isRepeatOne)
            .apply()
    }

    fun playSurah(surah: Surah, reciter: Reciter? = null) {
        val context = getApplication<Application>()
        val player = exoPlayer ?: return

        // Save the previous Surah's position before switching.
        savePlaybackState()

        val targetReciter = reciter ?: _uiState.value.currentReciter

        // Fix: always use the selected reciter's URL.
        val audioUrl = targetReciter.getAudioUrlForSurah(surah.number)

        val startPosition =
            getSavedPositionForSurah(surah.number)

        Log.i(
            TAG,
            "Playing ${surah.nameArabic} via ${targetReciter.nameArabic} from $startPosition ms"
        )

        try {
            val serviceIntent =
                Intent(context, QuranPlaybackService::class.java)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } catch (e: Exception) {
            Log.w(
                TAG,
                "Could not start playback service: ${e.message}"
            )
        }

        _uiState.update {
            it.copy(
                currentSurah = surah,
                currentReciter = targetReciter,
                currentPositionMs = startPosition,
                playbackStatus = PlaybackStatus.BUFFERING,
                isBuffering = true,
                isPlaying = false,
                errorMessage = null
            )
        }

        val metadata = MediaMetadata.Builder()
            .setTitle("سورة ${surah.nameArabic}")
            .setArtist(targetReciter.nameArabic)
            .setAlbumTitle("القرآن الكريم")
            .setDisplayTitle("سورة ${surah.nameArabic}")
            .setSubtitle(targetReciter.nameArabic)
            .build()

        val mediaItem = MediaItem.Builder()
            .setMediaId(surah.number.toString())
            .setUri(Uri.parse(audioUrl))
            .setMediaMetadata(metadata)
            .build()

        try {
            player.setMediaItem(mediaItem, startPosition)
            player.prepare()

            player.playbackParameters =
                PlaybackParameters(_uiState.value.playbackSpeed)

            player.play()

            savePlaybackState(
                audioUrlOverride = audioUrl,
                positionOverride = startPosition
            )
        } catch (e: Exception) {
            Log.e(TAG, "Could not play surah", e)

            _uiState.update {
                it.copy(
                    playbackStatus = PlaybackStatus.ERROR,
                    isPlaying = false,
                    isBuffering = false,
                    errorMessage =
                        "تعذر تشغيل التلاوة. يرجى التأكد من الاتصال بالإنترنت."
                )
            }

            savePlaybackState(
                audioUrlOverride = audioUrl,
                positionOverride = startPosition
            )
        }
    }

    fun togglePlayPause() {
        val player = exoPlayer ?: return

        if (player.isPlaying) {
            player.pause()
        } else {
            if (player.currentMediaItem == null) {
                playSurah(
                    _uiState.value.currentSurah,
                    _uiState.value.currentReciter
                )
            } else {
                player.play()
            }
        }
    }

    fun play() {
        exoPlayer?.play()
    }

    fun pause() {
        exoPlayer?.pause()
        savePlaybackState()
    }

    fun seekTo(positionMs: Long) {
        val position = positionMs.coerceAtLeast(0L)

        exoPlayer?.seekTo(position)

        _uiState.update {
            it.copy(currentPositionMs = position)
        }

        savePlaybackState(positionOverride = position)
    }

    fun skipForward(seconds: Int = 10) {
        val player = exoPlayer ?: return

        val newPosition =
            (player.currentPosition + seconds * 1000L)
                .coerceAtMost(player.duration.coerceAtLeast(0L))

        player.seekTo(newPosition)

        _uiState.update {
            it.copy(currentPositionMs = newPosition)
        }

        savePlaybackState(positionOverride = newPosition)
    }

    fun skipBackward(seconds: Int = 10) {
        val player = exoPlayer ?: return

        val newPosition =
            (player.currentPosition - seconds * 1000L)
                .coerceAtLeast(0L)

        player.seekTo(newPosition)

        _uiState.update {
            it.copy(currentPositionMs = newPosition)
        }

        savePlaybackState(positionOverride = newPosition)
    }

    fun playNext() {
        val currentNumber = _uiState.value.currentSurah.number

        val nextNumber =
            if (currentNumber >= 114) 1 else currentNumber + 1

        val nextSurah = QuranData.surahs.firstOrNull {
            it.number == nextNumber
        } ?: QuranData.surahs.first()

        playSurah(nextSurah)
    }

    fun playPrevious() {
        val currentNumber = _uiState.value.currentSurah.number

        val previousNumber =
            if (currentNumber <= 1) 114 else currentNumber - 1

        val previousSurah = QuranData.surahs.firstOrNull {
            it.number == previousNumber
        } ?: QuranData.surahs.last()

        playSurah(previousSurah)
    }

    fun setReciter(reciter: Reciter) {
        if (_uiState.value.currentReciter.id == reciter.id) {
            return
        }

        // playSurah saves the current position before switching reciters.
        playSurah(_uiState.value.currentSurah, reciter)
    }

    fun setPlaybackSpeed(speed: Float) {
        if (speed !in 0.25f..3.0f) return

        _uiState.update {
            it.copy(playbackSpeed = speed)
        }

        exoPlayer?.playbackParameters =
            PlaybackParameters(speed)

        savePlaybackState()
    }

    fun toggleRepeatOne() {
        _uiState.update {
            it.copy(isRepeatOne = !it.isRepeatOne)
        }

        savePlaybackState()
    }

    fun retry() {
        _uiState.update {
            it.copy(
                errorMessage = null,
                isBuffering = true
            )
        }

        playSurah(
            _uiState.value.currentSurah,
            _uiState.value.currentReciter
        )
    }

    fun setPlayerExpanded(expanded: Boolean) {
        _uiState.update {
            it.copy(isPlayerExpanded = expanded)
        }
    }

    private fun startProgressPolling() {
        progressJob?.cancel()

        progressJob = viewModelScope.launch {
            var lastSaveTime = 0L

            while (isActive) {
                exoPlayer?.let { player ->
                    val position =
                        player.currentPosition.coerceAtLeast(0L)

                    val duration =
                        player.duration.coerceAtLeast(0L)

                    _uiState.update {
                        it.copy(
                            currentPositionMs = position,
                            durationMs = if (duration > 0L) {
                                duration
                            } else {
                                it.durationMs
                            }
                        )
                    }

                    val now =
                        android.os.SystemClock.elapsedRealtime()

                    if (now - lastSaveTime >= SAVE_INTERVAL_MS) {
                        savePlaybackState(
                            positionOverride = position
                        )

                        lastSaveTime = now
                    }
                }

                delay(300L)
            }
        }
    }

    private fun stopProgressPolling() {
        progressJob?.cancel()
        progressJob = null
    }

    val isSleepTimerActive: StateFlow<Boolean> =
        com.example.timer.SleepTimerManager.isTimerActive

    val sleepTimerRemainingSeconds: StateFlow<Long> =
        com.example.timer.SleepTimerManager.remainingSeconds

    fun startSleepTimer(minutes: Int) {
        val context = getApplication<Application>()

        com.example.timer.SleepTimerManager.startTimer(
            context,
            minutes
        )
    }

    fun startSleepTimerSeconds(seconds: Long) {
        val context = getApplication<Application>()

        com.example.timer.SleepTimerManager.startTimerWithSeconds(
            context,
            seconds
        )
    }

    fun cancelSleepTimer() {
        com.example.timer.SleepTimerManager.cancelTimer()
    }

    override fun onCleared() {
        savePlaybackState()
        stopProgressPolling()
        exoPlayer?.removeListener(playerListener)

        // Do not release the shared player or MediaSession.
        Log.i(
            TAG,
            "ViewModel cleared; shared background player remains available"
        )

        super.onCleared()
    }
}
