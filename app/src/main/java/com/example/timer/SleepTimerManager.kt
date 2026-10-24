package com.example.timer

import android.content.Context
import android.util.Log
import com.example.service.QuranPlaybackService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Singleton SleepTimerManager that manages countdown timing independently of Activity lifecycle.
 * Automatically pauses the background ExoPlayer when the countdown reaches zero.
 */
object SleepTimerManager {
    private const val TAG = "SleepTimerManager"

    private val scope = CoroutineScope(Dispatchers.Main)
    private var timerJob: Job? = null

    private var targetEndTimeMs: Long = 0L

    private val _isTimerActive = MutableStateFlow(false)
    val isTimerActive: StateFlow<Boolean> = _isTimerActive.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(0L)
    val remainingSeconds: StateFlow<Long> = _remainingSeconds.asStateFlow()

    fun formatTime(totalSeconds: Long): String {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    /**
     * Starts sleep timer for specified minutes (e.g., 5, 10, 15, 30, 45, 60).
     */
    fun startTimer(context: Context, minutes: Int) {
        val seconds = minutes * 60L
        startTimerWithSeconds(context, seconds)
    }

    /**
     * Starts sleep timer with exact seconds (useful for custom periods or quick tests).
     */
    fun startTimerWithSeconds(context: Context, seconds: Long) {
        cancelTimer()
        targetEndTimeMs = System.currentTimeMillis() + (seconds * 1000L)
        _isTimerActive.value = true
        _remainingSeconds.value = seconds
        Log.i(TAG, "[SleepTimer] Started timer for $seconds seconds ($targetEndTimeMs)")

        timerJob = scope.launch {
            while (isActive) {
                val now = System.currentTimeMillis()
                val diffMs = targetEndTimeMs - now
                val remainingSec = (diffMs / 1000L).coerceAtLeast(0L)
                _remainingSeconds.value = remainingSec

                if (remainingSec <= 0L) {
                    Log.i(TAG, "[SleepTimer] Countdown finished. Pausing background recitation.")
                    try {
                        val player = QuranPlaybackService.getSharedPlayer(context)
                        player.pause()
                    } catch (e: Exception) {
                        Log.e(TAG, "[SleepTimer] Failed to pause player on timeout: ${e.message}", e)
                    }
                    cancelTimer()
                    break
                }
                delay(1000L)
            }
        }
    }

    /**
     * Cancels the active sleep timer.
     */
    fun cancelTimer() {
        if (_isTimerActive.value) {
            Log.i(TAG, "[SleepTimer] Sleep timer cancelled by user.")
        }
        timerJob?.cancel()
        timerJob = null
        targetEndTimeMs = 0L
        _isTimerActive.value = false
        _remainingSeconds.value = 0L
    }
}
