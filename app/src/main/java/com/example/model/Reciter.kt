package com.example.model

data class Reciter(
    val id: String,
    val nameArabic: String,
    val riwayah: String,
    val serverBaseUrl: String
) {
    fun getAudioUrlForSurah(surahNumber: Int): String {
        val padded = String.format("%03d", surahNumber)
        val base = if (serverBaseUrl.endsWith("/")) serverBaseUrl else "$serverBaseUrl/"
        return "$base$padded.mp3"
    }
}
