package com.example.data

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import com.example.model.Reciter
import com.example.model.Surah

data class RestoredPlaybackState(
    val surah: Surah,
    val reciter: Reciter,
    val audioUrl: String,
    val positionMs: Long,
    val playbackSpeed: Float,
    val isRepeatOne: Boolean
)

object PlaybackStateStore {

    const val PREFS_NAME = "quran_playback_preferences"
    const val KEY_SURAH_NUMBER = "last_surah_number"
    const val KEY_RECITER_ID = "last_reciter_id"
    const val KEY_AUDIO_URL = "last_audio_url"
    const val KEY_POSITION = "last_position_ms"
    const val KEY_SPEED = "last_playback_speed"
    const val KEY_REPEAT = "last_repeat_one"
    const val SURAH_POSITION_PREFIX = "surah_position_"

    fun positionKey(surahNumber: Int): String = "$SURAH_POSITION_PREFIX$surahNumber"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getSavedSurah(context: Context): Surah {
        val number = prefs(context).getInt(KEY_SURAH_NUMBER, 1)
        return QuranData.surahs.firstOrNull { it.number == number }
            ?: QuranData.surahs.first()
    }

    fun getSavedReciter(context: Context): Reciter {
        DeveloperContentManager.initialize(context)
        val savedId = prefs(context).getString(KEY_RECITER_ID, null)
        return DeveloperContentManager.recitersList.value.firstOrNull { it.id == savedId }
            ?: QuranData.reciters.firstOrNull { it.id == savedId }
            ?: QuranData.reciters.first()
    }

    fun getSavedSpeed(context: Context): Float {
        val speed = prefs(context).getFloat(KEY_SPEED, 1.0f)
        return if (speed in 0.25f..3.0f) speed else 1.0f
    }

    fun isRepeatOne(context: Context): Boolean =
        prefs(context).getBoolean(KEY_REPEAT, false)

    fun getSavedAudioUrl(context: Context): String? =
        prefs(context).getString(KEY_AUDIO_URL, null)?.takeIf { it.isNotBlank() }

    fun getSavedPositionForSurah(context: Context, surahNumber: Int): Long {
        val preferences = prefs(context)
        val key = positionKey(surahNumber)
        return if (preferences.contains(key)) {
            preferences.getLong(key, 0L).coerceAtLeast(0L)
        } else {
            val oldSurahNumber = preferences.getInt(KEY_SURAH_NUMBER, 1)
            if (oldSurahNumber == surahNumber) {
                preferences.getLong(KEY_POSITION, 0L).coerceAtLeast(0L)
            } else {
                0L
            }
        }
    }

    fun loadRestoredState(context: Context): RestoredPlaybackState {
        val preferences = prefs(context)
        val surah = getSavedSurah(context)
        val reciter = getSavedReciter(context)
        val savedUrl = preferences.getString(KEY_AUDIO_URL, null)
            ?.takeIf { it.isNotBlank() }
            ?: reciter.getAudioUrlForSurah(surah.number)
        val savedPosition = getSavedPositionForSurah(context, surah.number)
        val speed = getSavedSpeed(context)
        val repeatOne = isRepeatOne(context)

        return RestoredPlaybackState(
            surah = surah,
            reciter = reciter,
            audioUrl = savedUrl,
            positionMs = savedPosition,
            playbackSpeed = speed,
            isRepeatOne = repeatOne
        )
    }

    fun buildMediaItem(surah: Surah, reciter: Reciter, audioUrl: String): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle("سورة ${surah.nameArabic}")
            .setArtist(reciter.nameArabic)
            .setAlbumTitle("القرآن الكريم")
            .setDisplayTitle("سورة ${surah.nameArabic}")
            .setSubtitle(reciter.nameArabic)
            .build()

        return MediaItem.Builder()
            .setMediaId(surah.number.toString())
            .setUri(Uri.parse(audioUrl))
            .setMediaMetadata(metadata)
            .build()
    }

    /**
     * Restores the last saved Surah, Reciter, URL, and position onto [player]
     * without calling prepare() or play() so playback never auto-starts.
     */
    fun restorePlayerIfNeeded(context: Context, player: Player): RestoredPlaybackState {
        val restored = loadRestoredState(context)
        if (player.currentMediaItem == null) {
            val mediaItem = buildMediaItem(
                surah = restored.surah,
                reciter = restored.reciter,
                audioUrl = restored.audioUrl
            )
            player.setMediaItem(mediaItem, restored.positionMs)
            player.playbackParameters = PlaybackParameters(restored.playbackSpeed)
        }
        return restored
    }

    fun saveState(
        context: Context,
        surah: Surah,
        reciter: Reciter,
        audioUrl: String,
        positionMs: Long,
        playbackSpeed: Float,
        isRepeatOne: Boolean
    ) {
        val safePosition = positionMs.coerceAtLeast(0L)
        prefs(context).edit()
            .putInt(KEY_SURAH_NUMBER, surah.number)
            .putString(KEY_RECITER_ID, reciter.id)
            .putString(KEY_AUDIO_URL, audioUrl)
            .putLong(KEY_POSITION, safePosition)
            .putLong(positionKey(surah.number), safePosition)
            .putFloat(KEY_SPEED, playbackSpeed)
            .putBoolean(KEY_REPEAT, isRepeatOne)
            .apply()
    }
}
