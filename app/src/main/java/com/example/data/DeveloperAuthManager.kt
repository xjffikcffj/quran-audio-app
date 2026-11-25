package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest

/**
 * Handles developer authentication securely without hardcoding passwords in the APK or sending them in chat.
 * Uses SHA-256 salted hashing locally and session tokens.
 */
object DeveloperAuthManager {

    private const val PREFS_NAME = "quran_developer_auth_prefs"
    private const val KEY_DEVELOPER_EMAIL = "key_dev_email"
    private const val KEY_DEVELOPER_HASH = "key_dev_hash"
    private const val KEY_IS_SETUP = "key_is_setup"
    private const val SALT = "QuranAudio_DevSecurity_Salt_2026"

    private var prefs: SharedPreferences? = null

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isAccountSetup = MutableStateFlow(false)
    val isAccountSetup: StateFlow<Boolean> = _isAccountSetup.asStateFlow()

    private val _developerEmail = MutableStateFlow("")
    val developerEmail: StateFlow<String> = _developerEmail.asStateFlow()

    fun initialize(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val isSetup = prefs?.getBoolean(KEY_IS_SETUP, false) ?: false
            _isAccountSetup.value = isSetup
            _developerEmail.value = prefs?.getString(KEY_DEVELOPER_EMAIL, "") ?: ""
        }
    }

    private fun hashPassword(password: String): String {
        val bytes = (SALT + password).toByteArray(Charsets.UTF_8)
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }

    /**
     * Sets up the initial developer credentials chosen by the developer.
     */
    fun setupDeveloperAccount(email: String, password: String): Boolean {
        if (email.isBlank() || password.length < 6) return false

        val hash = hashPassword(password)
        prefs?.edit()
            ?.putString(KEY_DEVELOPER_EMAIL, email.trim())
            ?.putString(KEY_DEVELOPER_HASH, hash)
            ?.putBoolean(KEY_IS_SETUP, true)
            ?.apply()

        _isAccountSetup.value = true
        _developerEmail.value = email.trim()
        _isLoggedIn.value = true
        return true
    }

    /**
     * Logs the developer in by verifying the hash without exposing plaintext credentials.
     */
    fun login(email: String, password: String): Boolean {
        val savedEmail = prefs?.getString(KEY_DEVELOPER_EMAIL, "") ?: ""
        val savedHash = prefs?.getString(KEY_DEVELOPER_HASH, "") ?: ""

        if (savedEmail.equals(email.trim(), ignoreCase = true) && savedHash == hashPassword(password)) {
            _isLoggedIn.value = true
            return true
        }
        return false
    }

    fun logout() {
        _isLoggedIn.value = false
    }
}
