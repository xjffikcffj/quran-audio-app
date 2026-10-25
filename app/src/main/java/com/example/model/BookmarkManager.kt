package com.example.model

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class BookmarkManager(context: Context) {

    private val preferences = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    @Synchronized
    fun getBookmarks(): List<AudioBookmark> {
        val json = preferences.getString(KEY_BOOKMARKS, null)
            ?: return emptyList()

        return try {
            val array = JSONArray(json)
            buildList {
                for (i in 0 until array.length()) {
                    val item = array.optJSONObject(i) ?: continue
                    val number = item.optInt("surahNumber", 0)
                    val position = item.optLong("positionMs", -1L)
                    val id = item.optString("id", "")
                    val name = item.optString("name", "")

                    if (number in 1..114 &&
                        position >= 0 &&
                        id.isNotBlank()
                    ) {
                        add(
                            AudioBookmark(
                                id = id,
                                surahNumber = number,
                                positionMs = position,
                                name = name,
                                createdAt = item.optLong(
                                    "createdAt",
                                    0L
                                )
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    @Synchronized
    fun addBookmark(
        surahNumber: Int,
        positionMs: Long,
        name: String
    ): AudioBookmark? {
        if (surahNumber !in 1..114 || positionMs < 0) {
            return null
        }

        val bookmark = AudioBookmark(
            id = UUID.randomUUID().toString(),
            surahNumber = surahNumber,
            positionMs = positionMs,
            name = name.trim().ifBlank {
                "علامة - سورة $surahNumber"
            }
        )

        saveBookmarks(getBookmarks() + bookmark)
        return bookmark
    }

    @Synchronized
    fun renameBookmark(id: String, newName: String): Boolean {
        val name = newName.trim()
        if (name.isBlank()) return false

        val bookmarks = getBookmarks()
        if (bookmarks.none { it.id == id }) return false

        saveBookmarks(
            bookmarks.map {
                if (it.id == id) it.copy(name = name) else it
            }
        )
        return true
    }

    @Synchronized
    fun deleteBookmark(id: String): Boolean {
        val bookmarks = getBookmarks()
        val updated = bookmarks.filterNot { it.id == id }

        if (updated.size == bookmarks.size) return false

        saveBookmarks(updated)
        return true
    }

    private fun saveBookmarks(bookmarks: List<AudioBookmark>) {
        val array = JSONArray()

        bookmarks.forEach { bookmark ->
            array.put(
                JSONObject().apply {
                    put("id", bookmark.id)
                    put("surahNumber", bookmark.surahNumber)
                    put("positionMs", bookmark.positionMs)
                    put("name", bookmark.name)
                    put("createdAt", bookmark.createdAt)
                }
            )
        }

        preferences.edit()
            .putString(KEY_BOOKMARKS, array.toString())
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "quran_bookmarks"
        private const val KEY_BOOKMARKS = "saved_bookmarks"
    }
}
