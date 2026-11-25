
package com.example.timer

import android.content.Context
import android.util.Log
import com.example.service.QuranPlaybackService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

object SleepTimerManager {
    private const val TAG = "SleepTimerManager"

    private val scope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var timerJob: Job? = null
    private var appContext: Context? = null

    private var remainingTimeMs = 0L
    private var segmentStartTimeMs = 0L
    private var timerPaused = false

    private val _isTimerActive = MutableStateFlow(false)
    val isTimerActive: StateFlow<Boolean> =
        _isTimerActive.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(0L)
    val remainingSeconds: StateFlow<Long> =
        _remainingSeconds.asStateFlow()

    fun formatTime(totalSeconds: Long): String {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(
            Locale.US,
            "%02d:%02d",
            minutes,
            seconds
        )
    }

    fun startTimer(context: Context, minutes: Int) {
        startTimerWithSeconds(
            context,
            minutes.coerceAtLeast(0) * 60L
        )
    }

    fun startTimerWithSeconds(
        context: Context,
        seconds: Long
    ) {
        cancelTimer()

        if (seconds <= 0L) return

        appContext = context.applicationContext
        remainingTimeMs = seconds * 1000L

        timerPaused = try {
            !QuranPlaybackService
                .getSharedPlayer(appContext!!)
                .playWhenReady
        } catch (e: Exception) {
            Log.w(TAG, "Could not read player state: ${e.message}")
            false
        }

        _isTimerActive.value = true
        _remainingSeconds.value = seconds

        Log.i(
            TAG,
            "Timer started: $seconds seconds; paused=$timerPaused"
        )

        if (!timerPaused) {
            startCountdown()
        }
    }

    fun pauseTimer() {
        if (!_isTimerActive.value || timerPaused) return

        val elapsed =
            System.currentTimeMillis() - segmentStartTimeMs

        remainingTimeMs =
            (remainingTimeMs - elapsed).coerceAtLeast(0L)

        _remainingSeconds.value =
            (remainingTimeMs + 999L) / 1000L

        timerPaused = true

        timerJob?.cancel()
        timerJob = null

        Log.i(
            TAG,
            "Timer paused: ${_remainingSeconds.value} seconds left"
        )
    }

    fun resumeTimer() {
        if (!_isTimerActive.value || !timerPaused) return

        if (remainingTimeMs <= 0L) {
            finishTimer()
            return
        }

        timerPaused = false

        Log.i(TAG, "Timer resumed")

        startCountdown()
    }

    private fun startCountdown() {
        timerJob?.cancel()

        segmentStartTimeMs = System.currentTimeMillis()

        timerJob = scope.launch {
            while (
                isActive &&
                _isTimerActive.value &&
                !timerPaused
            ) {
                val elapsed =
                    System.currentTimeMillis() - segmentStartTimeMs

                val remaining =
                    (remainingTimeMs - elapsed).coerceAtLeast(0L)

                _remainingSeconds.value =
                    (remaining + 999L) / 1000L

                if (remaining <= 0L) {
                    finishTimer()
                    break
                }

                delay(minOf(1000L, remaining))
            }
        }
    }

    private fun finishTimer() {
        if (!_isTimerActive.value) return

        Log.i(TAG, "Timer finished; pausing Quran playback")

        remainingTimeMs = 0L
        _remainingSeconds.value = 0L

        try {
            appContext?.let { context ->
                QuranPlaybackService
                    .getSharedPlayer(context)
                    .pause()
            }
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Failed to pause playback: ${e.message}",
                e
            )
        }

        cancelTimer()
    }

    fun cancelTimer() {
        if (_isTimerActive.value) {
            Log.i(TAG, "Sleep timer cancelled")
        }

        timerJob?.cancel()
        timerJob = null

        remainingTimeMs = 0L
        segmentStartTimeMs = 0L
        timerPaused = false
        appContext = null

        _isTimerActive.value = false
        _remainingSeconds.value = 0L
    }
}
