package com.example.model

/**
 * Data class representing a Quranic Surah.
 *
 * @param id The Surah number/ID (1 to 114)
 * @param name The Arabic name of the Surah (e.g., الفاتحة)
 * @param verseCount The number of verses (ayat) in the Surah
 * @param audioUrl Direct URL to the high-quality recitation audio file (MP3)
 * @param nameEnglish Optional English transliterated name
 * @param isMeccan True if revealed in Makkah, false if Madinah
 */
data class Surah(
    val id: Int,
    val name: String,
    val verseCount: Int,
    val audioUrl: String = String.format("https://server8.mp3quran.net/afs/%03d.mp3", id),
    val nameEnglish: String = "",
    val isMeccan: Boolean = true
) {
    // Backward compatibility aliases
    val number: Int get() = id
    val versesCount: Int get() = verseCount
    val nameArabic: String get() = name

    val revelationTypeArabic: String
        get() = if (isMeccan) "مكية" else "مدنية"

    val paddedNumber: String
        get() = String.format("%03d", id)
}
