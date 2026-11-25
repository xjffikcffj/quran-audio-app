package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.AudioPlayerUiState
import com.example.viewmodel.PlaybackStatus
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.IslamicBorder
import com.example.ui.theme.IslamicEmeraldLight
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.IslamicGoldLight
import com.example.ui.theme.IslamicRed
import com.example.ui.theme.IslamicSurface
import com.example.ui.theme.IslamicSurfaceVariant
import com.example.ui.theme.IslamicTextMuted
import com.example.ui.theme.IslamicTextPrimary
import com.example.ui.theme.IslamicTextSecondary

typealias PlayerUiState = AudioPlayerUiState

@Composable
fun MiniPlayer(
    state: AudioPlayerUiState,
    onTogglePlayPause: () -> Unit,
    onPlayNext: () -> Unit,
    onPlayPrevious: () -> Unit,
    onClick: () -> Unit,
    onRetry: () -> Unit = {},
    isTimerActive: Boolean = false,
    remainingSeconds: Long = 0L,
    onTimerClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        // Clear Error Notification Banner if stream fails
        AnimatedVisibility(
            visible = state.errorMessage != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(IslamicRed.copy(alpha = 0.18f))
                    .border(1.dp, IslamicRed, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("playback_error_banner"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "تنبيه",
                        tint = IslamicRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = state.errorMessage ?: "حدث خطأ في تحميل التلاوة",
                        fontFamily = CairoFontFamily,
                        fontSize = 11.sp,
                        color = Color(0xFFFFCDD2),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(IslamicGold.copy(alpha = 0.2f))
                        .clickable(onClick = onRetry)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "إعادة المحاولة",
                        tint = IslamicGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "إعادة المحاولة",
                        fontFamily = CairoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = IslamicGold
                    )
                }
            }
        }

        // Mini Player Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(IslamicSurface)
                .border(1.dp, IslamicBorder, RoundedCornerShape(16.dp))
                .clickable(onClick = onClick)
                .testTag("mini_player")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Thin continuous progress indicator on top
                LinearProgressIndicator(
                    progress = { state.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = IslamicGold,
                    trackColor = IslamicSurfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Surah info & Playback Status Badge
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IslamicStarBadge(
                            number = state.currentSurah.number,
                            isCurrent = true,
                            modifier = Modifier.size(38.dp)
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "سورة ${state.currentSurah.nameArabic}",
                                    fontFamily = AmiriFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = IslamicGoldLight,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                // Status Badge
                                val (statusText, statusBg, statusColor) = when {
                                    state.errorMessage != null -> Triple("خطأ", IslamicRed.copy(alpha = 0.2f), IslamicRed)
                                    state.isBuffering -> Triple("جاري التحميل...", IslamicGold.copy(alpha = 0.2f), IslamicGold)
                                    state.isPlaying -> Triple("يتلو الآن", IslamicEmeraldLight.copy(alpha = 0.2f), IslamicEmeraldLight)
                                    else -> Triple("متوقف مؤقتاً", IslamicSurfaceVariant, IslamicTextMuted)
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(statusBg)
                                        .padding(horizontal = 6.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = statusText,
                                        fontFamily = CairoFontFamily,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor
                                    )
                                }

                                if (isTimerActive) {
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(IslamicGold.copy(alpha = 0.2f))
                                            .border(1.dp, IslamicGold.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                            .clickable(onClick = onTimerClick)
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                            .testTag("mini_player_timer_badge"),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Bedtime,
                                            contentDescription = null,
                                            tint = IslamicGold,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Text(
                                            text = com.example.timer.SleepTimerManager.formatTime(remainingSeconds),
                                            fontFamily = CairoFontFamily,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = IslamicGold
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "${state.currentReciter.nameArabic} • ${state.currentSurah.revelationTypeArabic}",
                                fontFamily = CairoFontFamily,
                                fontSize = 12.sp,
                                color = IslamicTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Playback Controls (Previous, Play/Pause/Resume, Next)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Previous Surah button
                        IconButton(
                            onClick = onPlayPrevious,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("mini_player_previous")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = "السورة السابقة",
                                tint = IslamicTextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Play / Pause / Resume button
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(IslamicGold)
                                .clickable(onClick = onTogglePlayPause)
                                .testTag("mini_player_play_pause"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (state.isBuffering) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = Color(0xFF1E1700),
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Icon(
                                    imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (state.isPlaying) "إيقاف مؤقت" else "استئناف التلاوة",
                                    tint = Color(0xFF1E1700),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        // Next Surah button
                        IconButton(
                            onClick = onPlayNext,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("mini_player_next")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "السورة التالية",
                                tint = IslamicTextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
