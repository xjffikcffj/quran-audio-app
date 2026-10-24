package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log

/**
 * AdManager manages AdMob integration in a structured, policy-compliant manner.
 * 
 * Rules strictly followed:
 * 1. Test unit IDs are used during development.
 * 2. Interstitial ads follow frequency capping (e.g. at least 4 surahs and 120s cooldown).
 * 3. Audio playback is never muted or terminated by ads.
 * 4. Banner ads are positioned neatly above or below controls without overlapping.
 */
object AdManager {

    private const val TAG = "AdManager"

    // Google AdMob Official Test Unit IDs
    const val TEST_APP_OPEN_ID = "ca-app-pub-3940256099942544/9257390708"
    const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"

    private var surahSelectionCount = 0
    private var lastInterstitialTime = 0L
    private const val MIN_SURAH_CLICKS_BEFORE_INTERSTITIAL = 4
    private const val MIN_TIME_BETWEEN_INTERSTITIALS_MS = 120_000L // 2 minutes

    var isAdsEnabled: Boolean = true
    var showDevAdBanners: Boolean = true

    fun initialize(context: Context) {
        Log.d(TAG, "AdManager initialized with test IDs.")
    }

    /**
     * Checks if interstitial ad should be displayed according to frequency capping
     */
    fun onSurahSelected(activity: Activity, onProceedToPlayback: () -> Unit) {
        surahSelectionCount++
        val now = System.currentTimeMillis()

        val canShowAd = isAdsEnabled &&
                surahSelectionCount >= MIN_SURAH_CLICKS_BEFORE_INTERSTITIAL &&
                (now - lastInterstitialTime >= MIN_TIME_BETWEEN_INTERSTITIALS_MS)

        if (canShowAd) {
            lastInterstitialTime = now
            surahSelectionCount = 0
            Log.d(TAG, "Interstitial ad eligible to display. Simulating compliant test presentation.")
            // In live setup with MobileAds SDK loaded, show InterstitialAd here.
            // When dismissed, continue playback.
            onProceedToPlayback()
        } else {
            // Respect user experience and immediately begin recitation
            onProceedToPlayback()
        }
    }
}
