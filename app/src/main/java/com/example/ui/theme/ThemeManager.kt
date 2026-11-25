package com.example.ui.theme

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode(val titleArabic: String, val descriptionArabic: String) {
    DARK("الوضع الداكن الكلاسيكي", "ألوان زمردية وذهبية فاخرة متوازنة"),
    PURE_NIGHT("الوضع الليلي الكامل (تعتيم فائق)", "أسود خالص موفر للطاقة ومريح للعين عند الاستماع في الظلام"),
    LIGHT("الوضع الفاتح الأنيق", "واجهة ناصعة بلمسات إسلامية مشرقة وواضحة")
}

/**
 * Manages theme selection (Dark, Pure Night / Full Dimming, Light) and persists choice locally.
 */
object ThemeManager {

    private const val PREFS_NAME = "quran_theme_prefs"
    private const val KEY_THEME_MODE = "key_theme_mode"

    private var prefs: SharedPreferences? = null

    private val _themeMode = MutableStateFlow(AppThemeMode.DARK)
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    fun initialize(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val savedName = prefs?.getString(KEY_THEME_MODE, AppThemeMode.DARK.name)
            val mode = runCatching { AppThemeMode.valueOf(savedName ?: AppThemeMode.DARK.name) }
                .getOrDefault(AppThemeMode.DARK)
            _themeMode.value = mode
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
        prefs?.edit()?.putString(KEY_THEME_MODE, mode.name)?.apply()
    }

    fun toggleNextMode() {
        val next = when (_themeMode.value) {
            AppThemeMode.DARK -> AppThemeMode.PURE_NIGHT
            AppThemeMode.PURE_NIGHT -> AppThemeMode.LIGHT
            AppThemeMode.LIGHT -> AppThemeMode.DARK
        }
        setThemeMode(next)
    }
}
