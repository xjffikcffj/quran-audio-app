package com.example.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ads.AdManager
import com.example.data.DeveloperContentManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun AdBannerView(
    modifier: Modifier = Modifier
) {
    val isBannerEnabled by
        DeveloperContentManager.isBannerEnabled.collectAsState()

    if (!isBannerEnabled || !AdManager.showDevAdBanners) {
        return
    }

    AdMobBanner(
        modifier = modifier
    )
}

@Composable
private fun AdMobBanner(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val adView = remember(context) {
        AdView(context).apply {
            adUnitId = AdManager.TEST_BANNER_ID
            setAdSize(AdSize.BANNER)
            loadAd(AdRequest.Builder().build())
        }
    }

    DisposableEffect(adView) {
        onDispose {
            adView.destroy()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("ad_banner_slot"),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = {
                adView
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        )
    }
}
