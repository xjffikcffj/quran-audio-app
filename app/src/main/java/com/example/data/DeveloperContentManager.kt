package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.Reciter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

/**
 * Manages dynamic reciters, recitations, ad preferences, and backup exports for the Developer Dashboard.
 * Persists all configuration in SharedPreferences and allows 1-click JSON backup/restore.
 */
object DeveloperContentManager {

    private const val PREFS_NAME = "quran_developer_content_prefs"
    private const val KEY_CUSTOM_RECITERS = "key_custom_reciters"
    private const val KEY_HIDDEN_RECITERS = "key_hidden_reciters"
    private const val KEY_BANNER_ENABLED = "key_banner_enabled"
    private const val KEY_FRIDAY_REMINDER_ENABLED = "key_friday_reminder_enabled"

    private var prefs: SharedPreferences? = null

    private val _recitersList = MutableStateFlow<List<Reciter>>(QuranData.reciters)
    val recitersList: StateFlow<List<Reciter>> = _recitersList.asStateFlow()

    private val _hiddenReciterIds = MutableStateFlow<Set<String>>(emptySet())
    val hiddenReciterIds: StateFlow<Set<String>> = _hiddenReciterIds.asStateFlow()

    private val _isBannerEnabled = MutableStateFlow(true)
    val isBannerEnabled: StateFlow<Boolean> = _isBannerEnabled.asStateFlow()

    private val _isFridayReminderEnabled = MutableStateFlow(true)
    val isFridayReminderEnabled: StateFlow<Boolean> = _isFridayReminderEnabled.asStateFlow()

    fun initialize(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            loadContent()
        }
    }

    private fun loadContent() {
        prefs?.let { p ->
            _isBannerEnabled.value = p.getBoolean(KEY_BANNER_ENABLED, true)
            _isFridayReminderEnabled.value = p.getBoolean(KEY_FRIDAY_REMINDER_ENABLED, true)

            val hidden = p.getStringSet(KEY_HIDDEN_RECITERS, emptySet()) ?: emptySet()
            _hiddenReciterIds.value = hidden

            val customJson = p.getString(KEY_CUSTOM_RECITERS, null)
            val customReciters = mutableListOf<Reciter>()
            if (!customJson.isNullOrEmpty()) {
                try {
                    val array = JSONArray(customJson)
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        customReciters.add(
                            Reciter(
                                id = obj.getString("id"),
                                nameArabic = obj.getString("nameArabic"),
                                riwayah = obj.optString("riwayah", "حفص عن عاصم"),
                                serverBaseUrl = obj.getString("serverBaseUrl")
                            )
                        )
                    }
                } catch (e: Exception) {
                    // Fallback to default
                }
            }

            val combined = (QuranData.reciters + customReciters).distinctBy { it.id }
            _recitersList.value = combined
        }
    }

    fun getVisibleReciters(): List<Reciter> {
        val hidden = _hiddenReciterIds.value
        return _recitersList.value.filterNot { hidden.contains(it.id) }
    }

    fun addCustomReciter(name: String, riwayah: String, serverBaseUrl: String): Boolean {
        val cleanUrl = if (serverBaseUrl.endsWith("/")) serverBaseUrl else "$serverBaseUrl/"
        val id = "custom_" + System.currentTimeMillis()
        val newReciter = Reciter(
            id = id,
            nameArabic = name.trim(),
            riwayah = riwayah.trim().ifEmpty { "حفص عن عاصم" },
            serverBaseUrl = cleanUrl.trim()
        )

        val updated = _recitersList.value + newReciter
        _recitersList.value = updated
        persistCustomReciters()
        return true
    }

    fun toggleHideReciter(reciterId: String) {
        val current = _hiddenReciterIds.value.toMutableSet()
        if (current.contains(reciterId)) {
            current.remove(reciterId)
        } else {
            current.add(reciterId)
        }
        _hiddenReciterIds.value = current
        prefs?.edit()?.putStringSet(KEY_HIDDEN_RECITERS, current)?.apply()
    }

    fun setBannerEnabled(enabled: Boolean) {
        _isBannerEnabled.value = enabled
        prefs?.edit()?.putBoolean(KEY_BANNER_ENABLED, enabled)?.apply()
    }

    fun setFridayReminderEnabled(enabled: Boolean) {
        _isFridayReminderEnabled.value = enabled
        prefs?.edit()?.putBoolean(KEY_FRIDAY_REMINDER_ENABLED, enabled)?.apply()
    }

    private fun persistCustomReciters() {
        val customOnly = _recitersList.value.filter { it.id.startsWith("custom_") }
        val array = JSONArray()
        for (r in customOnly) {
            val obj = JSONObject()
            obj.put("id", r.id)
            obj.put("nameArabic", r.nameArabic)
            obj.put("riwayah", r.riwayah)
            obj.put("serverBaseUrl", r.serverBaseUrl)
            array.put(obj)
        }
        prefs?.edit()?.putString(KEY_CUSTOM_RECITERS, array.toString())?.apply()
    }

    /**
     * Exports full app configuration to a JSON string for offline developer backup.
     */
    fun exportBackupJson(): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("timestamp", System.currentTimeMillis())
        root.put("isBannerEnabled", _isBannerEnabled.value)
        root.put("isFridayReminderEnabled", _isFridayReminderEnabled.value)

        val recitersArray = JSONArray()
        for (r in _recitersList.value) {
            val obj = JSONObject()
            obj.put("id", r.id)
            obj.put("nameArabic", r.nameArabic)
            obj.put("riwayah", r.riwayah)
            obj.put("serverBaseUrl", r.serverBaseUrl)
            obj.put("isHidden", _hiddenReciterIds.value.contains(r.id))
            recitersArray.put(obj)
        }
        root.put("reciters", recitersArray)
        return root.toString(2)
    }
}
