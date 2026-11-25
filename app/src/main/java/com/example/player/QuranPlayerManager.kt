package com.example.player

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

import com.example.viewmodel.AudioPlayerUiState
import com.example.viewmodel.PlaybackStatus

typealias PlayerUiState = AudioPlayerUiState

class QuranPlayerManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main)
    private var progressJob: Job? = null

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var exoPlayer: ExoPlayer? = null
    private var mediaSession: MediaSession? = null

    init {
        initializePlayer()
    }

    private fun initializePlayer() {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
            .setUsage(C.USAGE_MEDIA)
            .build()

        val player = ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .build()

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        _uiState.update { it.copy(isBuffering = true, errorMessage = null) }
                    }
                    Player.STATE_READY -> {
                        _uiState.update {
                            it.copy(
                                isBuffering = false,
                                durationMs = player.duration.coerceAtLeast(0L),
                                errorMessage = null
                            )
                        }
                    }
                    Player.STATE_ENDED -> {
                        if (_uiState.value.isRepeatOne) {
                            player.seekTo(0)
                            player.play()
                        } else {
                            playNext()
                        }
                    }
                    Player.STATE_IDLE -> {
                        _uiState.update { it.copy(isBuffering = false) }
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) {
                    startProgressTracker()
                } else {
                    stopProgressTracker()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                _uiState.update {
                    it.copy(
                        isPlaying = false,
                        isBuffering = false,
                        errorMessage = "تعذر تحميل التلاوة، يرجى التحقق من اتصال الإنترنت والمحاولة مرة أخرى."
                    )
                }
                stopProgressTracker()
            }
        })

        exoPlayer = player

        mediaSession = try {
            MediaSession.Builder(context, player)
                .setId("QuranPlayerSession_${System.nanoTime()}")
                .build()
        } catch (_: Exception) {
            null
        }
    }

    fun playSurah(surah: Surah, reciter: Reciter? = null) {
        try {
            val serviceIntent = Intent(context, QuranPlaybackService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } catch (_: Exception) {
            // Handled gracefully in testing/emulator
        }
        val targetReciter = reciter ?: _uiState.value.currentReciter
        _uiState.update {
            it.copy(
                currentSurah = surah,
                currentReciter = targetReciter,
                errorMessage = null,
                isBuffering = true
            )
        }

        val audioUrl = targetReciter.getAudioUrlForSurah(surah.number)

        val metadata = MediaMetadata.Builder()
            .setTitle("سورة ${surah.nameArabic}")
            .setArtist(targetReciter.nameArabic)
            .setAlbumTitle("القرآن الكريم")
            .setDisplayTitle("سورة ${surah.nameArabic}")
            .setSubtitle(targetReciter.nameArabic)
            .build()

        val mediaItem = MediaItem.Builder()
            .setUri(Uri.parse(audioUrl))
            .setMediaMetadata(metadata)
            .build()

        exoPlayer?.let { player ->
            player.setMediaItem(mediaItem)
            player.prepare()
            player.playbackParameters = PlaybackParameters(_uiState.value.playbackSpeed)
            player.play()
        }
    }

    fun togglePlayPause() {
        val player = exoPlayer ?: return
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

    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
        _uiState.update { it.copy(currentPositionMs = positionMs) }
    }

    fun skipForward(seconds: Int = 10) {
        val player = exoPlayer ?: return
        val newPos = (player.currentPosition + seconds * 1000L).coerceAtMost(player.duration.coerceAtLeast(0L))
        player.seekTo(newPos)
        _uiState.update { it.copy(currentPositionMs = newPos) }
    }

    fun skipBackward(seconds: Int = 10) {
        val player = exoPlayer ?: return
        val newPos = (player.currentPosition - seconds * 1000L).coerceAtLeast(0L)
        player.seekTo(newPos)
        _uiState.update { it.copy(currentPositionMs = newPos) }
    }

    fun playNext() {
        val currentNumber = _uiState.value.currentSurah.number
        val nextNumber = if (currentNumber >= 114) 1 else currentNumber + 1
        val nextSurah = QuranData.surahs.firstOrNull { it.number == nextNumber } ?: QuranData.surahs.first()
        playSurah(nextSurah)
    }

    fun playPrevious() {
        val currentNumber = _uiState.value.currentSurah.number
        val prevNumber = if (currentNumber <= 1) 114 else currentNumber - 1
        val prevSurah = QuranData.surahs.firstOrNull { it.number == prevNumber } ?: QuranData.surahs.last()
        playSurah(prevSurah)
    }

    fun setReciter(reciter: Reciter) {
        if (_uiState.value.currentReciter.id == reciter.id) return
        _uiState.update { it.copy(currentReciter = reciter) }
        // Re-play current surah with new reciter
        playSurah(_uiState.value.currentSurah, reciter)
    }

    fun setPlaybackSpeed(speed: Float) {
        _uiState.update { it.copy(playbackSpeed = speed) }
        exoPlayer?.playbackParameters = PlaybackParameters(speed)
    }

    fun toggleRepeatOne() {
        _uiState.update { it.copy(isRepeatOne = !it.isRepeatOne) }
    }

    fun retry() {
        _uiState.update { it.copy(errorMessage = null, isBuffering = true) }
        playSurah(_uiState.value.currentSurah, _uiState.value.currentReciter)
    }

    fun setPlayerExpanded(expanded: Boolean) {
        _uiState.update { it.copy(isPlayerExpanded = expanded) }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
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

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    fun release() {
        stopProgressTracker()
        // Do not release shared player; background service handles engine lifecycle
    }
}
