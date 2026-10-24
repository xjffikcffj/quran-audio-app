package com.example.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
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

/**
 * Represents the distinct playback states of the audio player.
 */
enum class PlaybackStatus {
    IDLE,
    BUFFERING,
    PLAYING,
    PAUSED,
    ERROR
}

/**
 * UI State holding the full audio player presentation state.
 */
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
            (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
        } else 0f
}

/**
 * ViewModel managing Media3 ExoPlayer interaction, Surah playback, and UI state updates.
 * Crucially does NOT release the shared player onCleared so background playback continues uninterrupted.
 */
class AudioPlayerViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "AudioPlayerViewModel"
    }

    private val _uiState = MutableStateFlow(AudioPlayerUiState())
    val uiState: StateFlow<AudioPlayerUiState> = _uiState.asStateFlow()

    private var exoPlayer: ExoPlayer? = null
    private var mediaSession: MediaSession? = null
    private var progressJob: Job? = null

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            val stateName = when (playbackState) {
                Player.STATE_BUFFERING -> "BUFFERING"
                Player.STATE_READY -> "READY"
                Player.STATE_ENDED -> "ENDED"
                Player.STATE_IDLE -> "IDLE"
                else -> "UNKNOWN"
            }
            Log.d(TAG, "[ViewModelListener] onPlaybackStateChanged: $stateName")

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
                    val isPlaying = exoPlayer?.isPlaying == true
                    _uiState.update {
                        it.copy(
                            playbackStatus = if (isPlaying) PlaybackStatus.PLAYING else PlaybackStatus.PAUSED,
                            isPlaying = isPlaying,
                            isBuffering = false,
                            durationMs = (exoPlayer?.duration ?: 0L).coerceAtLeast(0L),
                            errorMessage = null
                        )
                    }
                }
                Player.STATE_ENDED -> {
                    Log.d(TAG, "[ViewModelListener] onPlaybackStateChanged: STATE_ENDED (isRepeatOne=${_uiState.value.isRepeatOne})")
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
                            playbackStatus = if (it.errorMessage != null) PlaybackStatus.ERROR else PlaybackStatus.IDLE,
                            isBuffering = false,
                            isPlaying = false
                        )
                    }
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            Log.d(TAG, "[ViewModelListener] onIsPlayingChanged: $isPlaying")
            _uiState.update {
                it.copy(
                    isPlaying = isPlaying,
                    playbackStatus = if (isPlaying) PlaybackStatus.PLAYING else {
                        if (it.playbackStatus == PlaybackStatus.BUFFERING) PlaybackStatus.BUFFERING else PlaybackStatus.PAUSED
                    }
                )
            }
            if (isPlaying) {
                startProgressPolling()
            } else {
                stopProgressPolling()
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            Log.e(TAG, "[ViewModelListener] onPlayerError: ${error.errorCodeName} - ${error.message}", error)
            _uiState.update {
                it.copy(
                    playbackStatus = PlaybackStatus.ERROR,
                    isPlaying = false,
                    isBuffering = false,
                    errorMessage = "تعذر تحميل التلاوة. يرجى التحقق من اتصال الإنترنت والمحاولة مرة أخرى."
                )
            }
            stopProgressPolling()
        }
    }

    init {
        initializeExoPlayer()
    }

    /**
     * Connects to the shared background ExoPlayer and MediaSession.
     */
    private fun initializeExoPlayer() {
        val context = getApplication<Application>()
        Log.i(TAG, "Initializing AudioPlayerViewModel with shared QuranPlaybackService engine")
        val player = QuranPlaybackService.getSharedPlayer(context)

        player.addListener(playerListener)
        exoPlayer = player
        mediaSession = QuranPlaybackService.getSharedSession(context)

        // Sync state if already streaming in background
        syncWithActivePlayer(player)
    }

    private fun syncWithActivePlayer(player: ExoPlayer) {
        val mediaId = player.currentMediaItem?.mediaId?.toIntOrNull()
        if (mediaId != null) {
            val matchingSurah = QuranData.surahs.find { it.number == mediaId }
            if (matchingSurah != null) {
                Log.i(TAG, "Syncing UI with already active background recitation: ${matchingSurah.nameArabic} (isPlaying=${player.isPlaying})")
                _uiState.update {
                    it.copy(
                        currentSurah = matchingSurah,
                        isPlaying = player.isPlaying,
                        isBuffering = player.playbackState == Player.STATE_BUFFERING,
                        playbackStatus = when {
                            player.isPlaying -> PlaybackStatus.PLAYING
                            player.playbackState == Player.STATE_BUFFERING -> PlaybackStatus.BUFFERING
                            player.playbackState == Player.STATE_READY -> PlaybackStatus.PAUSED
                            else -> PlaybackStatus.IDLE
                        },
                        currentPositionMs = player.currentPosition.coerceAtLeast(0L),
                        durationMs = player.duration.coerceAtLeast(0L)
                    )
                }
                if (player.isPlaying) {
                    startProgressPolling()
                }
            }
        }
    }

    /**
     * Plays a selected Surah using Media3 ExoPlayer and starts the foreground service.
     */
    fun playSurah(surah: Surah, reciter: Reciter? = null) {
        val context = getApplication<Application>()
        val targetReciter = reciter ?: _uiState.value.currentReciter
        val audioUrl = if (reciter != null) {
            targetReciter.getAudioUrlForSurah(surah.number)
        } else {
            surah.audioUrl
        }

        Log.i(TAG, "playSurah: ${surah.nameArabic} (${surah.number}) via ${targetReciter.nameArabic} -> $audioUrl")

        try {
            val serviceIntent = Intent(context, QuranPlaybackService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } catch (e: Exception) {
            Log.w(TAG, "startForegroundService failed: ${e.message}")
        }

        _uiState.update {
            it.copy(
                currentSurah = surah,
                currentReciter = targetReciter,
                playbackStatus = PlaybackStatus.BUFFERING,
                isBuffering = true,
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

        exoPlayer?.let { player ->
            try {
                player.setMediaItem(mediaItem)
                player.prepare()
                player.playbackParameters = PlaybackParameters(_uiState.value.playbackSpeed)
                player.play()
            } catch (e: Exception) {
                Log.e(TAG, "Exception preparing ExoPlayer: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        playbackStatus = PlaybackStatus.ERROR,
                        errorMessage = "تعذر تشغيل التلاوة. يرجى التأكد من الاتصال بالإنترنت."
                    )
                }
            }
        }
    }

    /**
     * Toggles between playing and paused states.
     */
    fun togglePlayPause() {
        val player = exoPlayer ?: return
        Log.d(TAG, "togglePlayPause: isPlaying=${player.isPlaying}, playbackState=${player.playbackState}")
        if (player.isPlaying) {
            player.pause()
        } else {
            if (player.playbackState == Player.STATE_IDLE || player.currentMediaItem == null) {
                playSurah(_uiState.value.currentSurah, _uiState.value.currentReciter)
            } else {
                player.play()
            }
        }
    }

    fun play() {
        Log.d(TAG, "play() called")
        exoPlayer?.play()
    }

    fun pause() {
        Log.d(TAG, "pause() called")
        exoPlayer?.pause()
    }

    fun seekTo(positionMs: Long) {
        Log.d(TAG, "seekTo: $positionMs ms")
        exoPlayer?.seekTo(positionMs)
        _uiState.update { it.copy(currentPositionMs = positionMs) }
    }

    fun skipForward(seconds: Int = 10) {
        val player = exoPlayer ?: return
        val newPos = (player.currentPosition + seconds * 1000L).coerceAtMost(player.duration.coerceAtLeast(0L))
        Log.d(TAG, "skipForward by $seconds sec to $newPos ms")
        player.seekTo(newPos)
        _uiState.update { it.copy(currentPositionMs = newPos) }
    }

    fun skipBackward(seconds: Int = 10) {
        val player = exoPlayer ?: return
        val newPos = (player.currentPosition - seconds * 1000L).coerceAtLeast(0L)
        Log.d(TAG, "skipBackward by $seconds sec to $newPos ms")
        player.seekTo(newPos)
        _uiState.update { it.copy(currentPositionMs = newPos) }
    }

    fun playNext() {
        val currentNumber = _uiState.value.currentSurah.number
        val nextNumber = if (currentNumber >= 114) 1 else currentNumber + 1
        val nextSurah = QuranData.surahs.firstOrNull { it.number == nextNumber } ?: QuranData.surahs.first()
        Log.d(TAG, "playNext: from $currentNumber to $nextNumber (${nextSurah.nameArabic})")
        playSurah(nextSurah)
    }

    fun playPrevious() {
        val currentNumber = _uiState.value.currentSurah.number
        val prevNumber = if (currentNumber <= 1) 114 else currentNumber - 1
        val prevSurah = QuranData.surahs.firstOrNull { it.number == prevNumber } ?: QuranData.surahs.last()
        Log.d(TAG, "playPrevious: from $currentNumber to $prevNumber (${prevSurah.nameArabic})")
        playSurah(prevSurah)
    }

    fun setReciter(reciter: Reciter) {
        if (_uiState.value.currentReciter.id == reciter.id) return
        Log.i(TAG, "setReciter: switched to ${reciter.nameArabic}")
        _uiState.update { it.copy(currentReciter = reciter) }
        playSurah(_uiState.value.currentSurah, reciter)
    }

    fun setPlaybackSpeed(speed: Float) {
        Log.d(TAG, "setPlaybackSpeed: $speed")
        _uiState.update { it.copy(playbackSpeed = speed) }
        exoPlayer?.playbackParameters = PlaybackParameters(speed)
    }

    fun toggleRepeatOne() {
        val newRepeat = !_uiState.value.isRepeatOne
        Log.d(TAG, "toggleRepeatOne: $newRepeat")
        _uiState.update { it.copy(isRepeatOne = newRepeat) }
    }

    fun retry() {
        Log.i(TAG, "retry: re-requesting current surah ${uiState.value.currentSurah.nameArabic}")
        _uiState.update { it.copy(errorMessage = null, isBuffering = true) }
        playSurah(_uiState.value.currentSurah, _uiState.value.currentReciter)
    }

    fun setPlayerExpanded(expanded: Boolean) {
        _uiState.update { it.copy(isPlayerExpanded = expanded) }
    }

    private fun startProgressPolling() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (isActive) {
                exoPlayer?.let { player ->
                    val pos = player.currentPosition.coerceAtLeast(0L)
                    val dur = player.duration.coerceAtLeast(0L)
                    _uiState.update {
                        it.copy(
                            currentPositionMs = pos,
                            durationMs = if (dur > 0) dur else it.durationMs
                        )
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

    val isSleepTimerActive: StateFlow<Boolean> = com.example.timer.SleepTimerManager.isTimerActive
    val sleepTimerRemainingSeconds: StateFlow<Long> = com.example.timer.SleepTimerManager.remainingSeconds

    fun startSleepTimer(minutes: Int) {
        val context = getApplication<Application>()
        com.example.timer.SleepTimerManager.startTimer(context, minutes)
    }

    fun startSleepTimerSeconds(seconds: Long) {
        val context = getApplication<Application>()
        com.example.timer.SleepTimerManager.startTimerWithSeconds(context, seconds)
    }

    fun cancelSleepTimer() {
        com.example.timer.SleepTimerManager.cancelTimer()
    }

    override fun onCleared() {
        super.onCleared()
        Log.i(TAG, "onCleared: Detaching UI listener while QuranPlaybackService continues streaming in background")
        stopProgressPolling()
        exoPlayer?.removeListener(playerListener)
        // CRITICAL: Do NOT release exoPlayer or mediaSession here!
        // They belong to the Foreground Service (QuranPlaybackService) and keep playing when the screen turns off.
    }
}
