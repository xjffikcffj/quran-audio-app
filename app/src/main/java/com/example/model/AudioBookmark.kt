package com.example.model

/**
 * A manually saved audio position in a Surah.
 * Bookmarks are independent of the selected reciter.
 */
data class AudioBookmark(
    val id: String,
    val surahNumber: Int,
    val positionMs: Long,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)
