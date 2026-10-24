package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdManager
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.IslamicBorder
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.IslamicSurfaceVariant
import com.example.ui.theme.IslamicTextMuted
import com.example.ui.theme.IslamicTextSecondary

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.data.DeveloperContentManager

@Composable
fun AdBannerView(
    modifier: Modifier = Modifier
) {
    val isBannerEnabled by DeveloperContentManager.isBannerEnabled.collectAsState()
    if (!isBannerEnabled || !AdManager.showDevAdBanners) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(IslamicSurfaceVariant)
            .border(1.dp, IslamicBorder, RoundedCornerShape(8.dp))
            .testTag("ad_banner_slot"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(IslamicGold.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "إعلان تجريبي",
                        fontFamily = CairoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = IslamicGold
                    )
                }
                Text(
                    text = "Google AdMob Test Banner (320x50)",
                    fontFamily = CairoFontFamily,
                    fontSize = 11.sp,
                    color = IslamicTextSecondary
                )
            }

            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "معلومات الإعلان",
                tint = IslamicTextMuted,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
