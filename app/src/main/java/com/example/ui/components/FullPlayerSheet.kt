package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.player.PlayerUiState
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.IslamicBorder
import com.example.ui.theme.IslamicCard
import com.example.ui.theme.IslamicCardHover
import com.example.ui.theme.IslamicDarkBg
import com.example.ui.theme.IslamicEmeraldDark
import com.example.ui.theme.IslamicEmeraldLight
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.IslamicGoldDark
import com.example.ui.theme.IslamicGoldLight
import com.example.ui.theme.IslamicRed
import com.example.ui.theme.IslamicSurface
import com.example.ui.theme.IslamicSurfaceVariant
import com.example.ui.theme.IslamicTextMuted
import com.example.ui.theme.IslamicTextPrimary
import com.example.ui.theme.IslamicTextSecondary
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPlayerSheet(
    state: PlayerUiState,
    onDismiss: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSkipForward: () -> Unit,
    onSkipBackward: () -> Unit,
    onPlayNext: () -> Unit,
    onPlayPrevious: () -> Unit,
    onToggleSpeed: () -> Unit,
    onToggleRepeat: () -> Unit,
    onOpenReciterSelector: () -> Unit,
    onRetry: () -> Unit,
    isTimerActive: Boolean = false,
    remainingSeconds: Long = 0L,
    onOpenSleepTimer: () -> Unit = {},
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = IslamicDarkBg,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 44.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(IslamicBorder)
            )
        },
        modifier = modifier.testTag("full_player_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("player_collapse_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "تصغير المشغل",
                        tint = IslamicGoldLight,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = "مشغل التلاوة",
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = IslamicGoldLight
                )

                // Reciter switcher pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(IslamicCardHover)
                        .border(1.dp, IslamicBorder, RoundedCornerShape(20.dp))
                        .clickable(onClick = onOpenReciterSelector)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("player_reciter_pill"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = IslamicGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "القارئ",
                        fontFamily = CairoFontFamily,
                        fontSize = 12.sp,
                        color = IslamicTextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Artwork Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(2.dp, IslamicBorder, RoundedCornerShape(24.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.quran_hero_banner_1792783860341),
                    contentDescription = "المصحف الشريف",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    IslamicDarkBg.copy(alpha = 0.85f)
                                )
                            )
                        )
                )

                // Revelation badge inside artwork
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(14.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(IslamicSurface.copy(alpha = 0.85f))
                        .border(1.dp, IslamicGold.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${state.currentSurah.revelationTypeArabic} • ${state.currentSurah.versesCount} آيات",
                        fontFamily = CairoFontFamily,
                        fontSize = 11.sp,
                        color = IslamicGoldLight,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Surah Title & Info with Favorite Star
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "سورة ${state.currentSurah.nameArabic}",
                    fontFamily = AmiriFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = IslamicGoldLight,
                    textAlign = TextAlign.Center
                )

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(44.dp)
                        .testTag("full_player_favorite_button")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = if (isFavorite) "إزالة من المفضلة" else "إضافة للمفضلة",
                        tint = if (isFavorite) IslamicGold else IslamicTextMuted,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Text(
                text = "${state.currentReciter.nameArabic} (${state.currentReciter.riwayah})",
                fontFamily = CairoFontFamily,
                fontSize = 14.sp,
                color = IslamicTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            // Explicit Playback Status Badge
            val (statusText, statusBg, statusColor) = when {
                state.errorMessage != null -> Triple("تعذر الاتصال بالشبكة", IslamicRed.copy(alpha = 0.2f), IslamicRed)
                state.isBuffering -> Triple("جاري تجهيز وبث التلاوة…", IslamicGold.copy(alpha = 0.2f), IslamicGold)
                state.isPlaying -> Triple("تلاوة جارية الآن", IslamicEmeraldLight.copy(alpha = 0.2f), IslamicEmeraldLight)
                else -> Triple("متوقف مؤقتاً (اضغط للاستئناف)", IslamicSurfaceVariant, IslamicTextMuted)
            }

            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(statusBg)
                    .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    text = statusText,
                    fontFamily = CairoFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }

            // Error display if any
            if (state.errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(IslamicRed.copy(alpha = 0.15f))
                        .border(1.dp, IslamicRed, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.errorMessage,
                        fontFamily = CairoFontFamily,
                        fontSize = 12.sp,
                        color = Color(0xFFFFCDD2),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onRetry,
                        modifier = Modifier.size(32.dp).testTag("retry_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "إعادة المحاولة",
                            tint = IslamicGold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Timeline Slider
            var isUserSeeking by remember { mutableStateOf(false) }
            var seekValue by remember { mutableFloatStateOf(0f) }

            val sliderPos = if (isUserSeeking) seekValue else {
                if (state.durationMs > 0L) {
                    (state.currentPositionMs.toFloat() / state.durationMs.toFloat()).coerceIn(0f, 1f)
                } else 0f
            }

            Slider(
                value = sliderPos,
                onValueChange = {
                    isUserSeeking = true
                    seekValue = it
                },
                onValueChangeFinished = {
                    if (state.durationMs > 0L) {
                        val targetMs = (seekValue * state.durationMs).toLong()
                        onSeekTo(targetMs)
                    }
                    isUserSeeking = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("player_progress_slider"),
                colors = SliderDefaults.colors(
                    thumbColor = IslamicGold,
                    activeTrackColor = IslamicGold,
                    inactiveTrackColor = IslamicSurfaceVariant
                )
            )

            // Time stamps
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val currentDisplayMs = if (isUserSeeking && state.durationMs > 0L) {
                    (seekValue * state.durationMs).toLong()
                } else state.currentPositionMs

                Text(
                    text = formatMs(currentDisplayMs),
                    fontFamily = CairoFontFamily,
                    fontSize = 12.sp,
                    color = IslamicTextSecondary
                )

                Text(
                    text = formatMs(state.durationMs),
                    fontFamily = CairoFontFamily,
                    fontSize = 12.sp,
                    color = IslamicTextMuted
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main Playback Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous Surah
                IconButton(
                    onClick = onPlayPrevious,
                    modifier = Modifier
                        .size(46.dp)
                        .testTag("player_previous_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "السورة السابقة",
                        tint = IslamicTextPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Rewind 10s
                IconButton(
                    onClick = onSkipBackward,
                    modifier = Modifier
                        .size(46.dp)
                        .testTag("player_rewind_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay10,
                        contentDescription = "تأخير 10 ثوان",
                        tint = IslamicGoldLight,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Big Golden Play / Pause
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(IslamicGoldLight, IslamicGold, IslamicGoldDark)
                            )
                        )
                        .clickable(onClick = onTogglePlayPause)
                        .testTag("player_play_pause_button"),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.isBuffering) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(36.dp),
                            color = Color(0xFF1E1700),
                            strokeWidth = 3.dp
                        )
                    } else {
                        Icon(
                            imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (state.isPlaying) "إيقاف مؤقت" else "تشغيل",
                            tint = Color(0xFF1E1700),
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

                // Forward 10s
                IconButton(
                    onClick = onSkipForward,
                    modifier = Modifier
                        .size(46.dp)
                        .testTag("player_forward_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Forward10,
                        contentDescription = "تقديم 10 ثوان",
                        tint = IslamicGoldLight,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Next Surah
                IconButton(
                    onClick = onPlayNext,
                    modifier = Modifier
                        .size(46.dp)
                        .testTag("player_next_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "السورة التالية",
                        tint = IslamicTextPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Extra Controls: Speed and Repeat
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Speed pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(IslamicCard)
                        .border(1.dp, IslamicBorder, RoundedCornerShape(12.dp))
                        .clickable(onClick = onToggleSpeed)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("player_speed_toggle"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = IslamicGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "${state.playbackSpeed}x",
                        fontFamily = CairoFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = IslamicTextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Repeat toggle
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (state.isRepeatOne) IslamicCardHover else IslamicCard)
                        .border(
                            1.dp,
                            if (state.isRepeatOne) IslamicGold else IslamicBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable(onClick = onToggleRepeat)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("player_repeat_toggle"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (state.isRepeatOne) Icons.Default.RepeatOne else Icons.Default.Repeat,
                        contentDescription = null,
                        tint = if (state.isRepeatOne) IslamicGold else IslamicTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (state.isRepeatOne) "تكرار السورة" else "تتابع تلقائي",
                        fontFamily = CairoFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (state.isRepeatOne) IslamicGoldLight else IslamicTextSecondary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Sleep Timer pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isTimerActive) IslamicCardHover else IslamicCard)
                        .border(
                            1.dp,
                            if (isTimerActive) IslamicGold else IslamicBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable(onClick = onOpenSleepTimer)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("player_sleep_timer_button"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bedtime,
                        contentDescription = "مؤقت النوم",
                        tint = if (isTimerActive) IslamicGold else IslamicTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (isTimerActive) com.example.timer.SleepTimerManager.formatTime(remainingSeconds) else "المؤقت",
                        fontFamily = CairoFontFamily,
                        fontSize = 13.sp,
                        fontWeight = if (isTimerActive) FontWeight.Bold else FontWeight.Medium,
                        color = if (isTimerActive) IslamicGoldLight else IslamicTextSecondary
                    )
                }
            }
        }
    }
}

private fun formatMs(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}
