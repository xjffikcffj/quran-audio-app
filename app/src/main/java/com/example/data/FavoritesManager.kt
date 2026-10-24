package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages local persistent favorites for Quran Surahs and Reciters without requiring cloud sign-in.
 * Persists data permanently in SharedPreferences across app restarts.
 */
object FavoritesManager {

    private const val PREFS_NAME = "quran_favorites_prefs"
    private const val KEY_FAVORITE_SURAHS = "key_favorite_surahs"
    private const val KEY_FAVORITE_RECITERS = "key_favorite_reciters"

    private var prefs: SharedPreferences? = null

    private val _favoriteSurahs = MutableStateFlow<Set<Int>>(emptySet())
    val favoriteSurahs: StateFlow<Set<Int>> = _favoriteSurahs.asStateFlow()

    private val _favoriteReciters = MutableStateFlow<Set<String>>(emptySet())
    val favoriteReciters: StateFlow<Set<String>> = _favoriteReciters.asStateFlow()

    fun initialize(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            loadFavorites()
        }
    }

    private fun loadFavorites() {
        prefs?.let { p ->
            val savedSurahs = p.getStringSet(KEY_FAVORITE_SURAHS, emptySet()) ?: emptySet()
            _favoriteSurahs.value = savedSurahs.mapNotNull { it.toIntOrNull() }.toSet()

            val savedReciters = p.getStringSet(KEY_FAVORITE_RECITERS, emptySet()) ?: emptySet()
            _favoriteReciters.value = savedReciters
        }
    }

    fun isSurahFavorite(surahNumber: Int): Boolean {
        return _favoriteSurahs.value.contains(surahNumber)
    }

    fun toggleSurahFavorite(surahNumber: Int) {
        val current = _favoriteSurahs.value.toMutableSet()
        if (current.contains(surahNumber)) {
            current.remove(surahNumber)
        } else {
            current.add(surahNumber)
        }
        _favoriteSurahs.value = current
        saveSurahs(current)
    }

    fun isReciterFavorite(reciterId: String): Boolean {
        return _favoriteReciters.value.contains(reciterId)
    }

    fun toggleReciterFavorite(reciterId: String) {
        val current = _favoriteReciters.value.toMutableSet()
        if (current.contains(reciterId)) {
            current.remove(reciterId)
        } else {
            current.add(reciterId)
        }
        _favoriteReciters.value = current
        saveReciters(current)
    }

    private fun saveSurahs(surahs: Set<Int>) {
        prefs?.edit()?.putStringSet(KEY_FAVORITE_SURAHS, surahs.map { it.toString() }.toSet())?.apply()
    }

    private fun saveReciters(reciters: Set<String>) {
        prefs?.edit()?.putStringSet(KEY_FAVORITE_RECITERS, reciters)?.apply()
    }
}
