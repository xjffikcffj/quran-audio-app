package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * Manages Google Mobile Ads integration.
 *
 * - Uses Google's official test ad unit IDs during development.
 * - Shows an interstitial after the configured selection threshold.
 * - Continues audio playback after the ad is dismissed or fails.
 * - Never blocks playback while an ad fails to load.
 */
object AdManager {

    private const val TAG = "AdManager"

    // Google's official test ad unit IDs.
    const val TEST_APP_OPEN_ID =
        "ca-app-pub-3940256099942544/9257390708"

    const val TEST_INTERSTITIAL_ID =
        "ca-app-pub-3940256099942544/1033173712"

    const val TEST_BANNER_ID =
        "ca-app-pub-3940256099942544/6300978111"

    private var surahSelectionCount = 0
    private var lastInterstitialTime = 0L

    private const val MIN_SURAH_CLICKS_BEFORE_INTERSTITIAL = 4
    private const val MIN_TIME_BETWEEN_INTERSTITIALS_MS = 120_000L

    @Volatile
    private var isInitialized = false

    var isAdsEnabled: Boolean = true
    var showDevAdBanners: Boolean = true

    /**
     * Initializes the Mobile Ads SDK.
     * Call this from the application startup.
     */
    fun initialize(context: Context) {
        if (isInitialized) return

        synchronized(this) {
            if (isInitialized) return

            try {
                MobileAds.initialize(context.applicationContext) {
                    isInitialized = true
                    Log.d(TAG, "Google Mobile Ads SDK initialized.")
                }
            } catch (exception: Exception) {
                Log.e(TAG, "Failed to initialize Google Mobile Ads.", exception)
            }
        }
    }

    /**
     * Shows an interstitial when the selection threshold and cooldown allow it.
     * Playback continues if the ad is unavailable, fails to load, or is dismissed.
     */
    fun onSurahSelected(
        activity: Activity,
        onProceedToPlayback: () -> Unit
    ) {
        if (!isAdsEnabled || activity.isFinishing || activity.isDestroyed) {
            onProceedToPlayback()
            return
        }

        if (!isInitialized) {
            initialize(activity.applicationContext)
        }

        surahSelectionCount++

        val now = System.currentTimeMillis()
        val canShowAd =
            surahSelectionCount >= MIN_SURAH_CLICKS_BEFORE_INTERSTITIAL &&
            (
                lastInterstitialTime == 0L ||
                now - lastInterstitialTime >= MIN_TIME_BETWEEN_INTERSTITIALS_MS
            )

        if (!canShowAd) {
            onProceedToPlayback()
            return
        }

        // Reset the count only when attempting to load an eligible ad.
        surahSelectionCount = 0

        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            activity,
            TEST_INTERSTITIAL_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {

                override fun onAdLoaded(ad: InterstitialAd) {
                    if (activity.isFinishing || activity.isDestroyed) {
                        Log.d(TAG, "Activity unavailable; skipping the ad.")
                        onProceedToPlayback()
                        return
                    }

                    ad.fullScreenContentCallback =
                        object : FullScreenContentCallback() {

                            override fun onAdDismissedFullScreenContent() {
                                Log.d(TAG, "Interstitial dismissed.")
                                onProceedToPlayback()
                            }

                            override fun onAdFailedToShowFullScreenContent(
                                adError: AdError
                            ) {
                                Log.e(
                                    TAG,
                                    "Interstitial failed to show: ${adError.message}"
                                )
                                onProceedToPlayback()
                            }

                            override fun onAdShowedFullScreenContent() {
                                lastInterstitialTime =
                                    System.currentTimeMillis()

                                Log.d(TAG, "Interstitial shown.")
                            }
                        }

                    try {
                        ad.show(activity)
                    } catch (exception: Exception) {
                        Log.e(TAG, "Could not show interstitial.", exception)
                        onProceedToPlayback()
                    }
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(
                        TAG,
                        "Interstitial failed to load: ${error.message}"
                    )
                    onProceedToPlayback()
                }
            }
        )
    }
}
