package com.example.ui.components

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.timer.SleepTimerManager
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.IslamicBorder
import com.example.ui.theme.IslamicCard
import com.example.ui.theme.IslamicCardHover
import com.example.ui.theme.IslamicDarkBg
import com.example.ui.theme.IslamicEmeraldLight
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.IslamicGoldLight
import com.example.ui.theme.IslamicRed
import com.example.ui.theme.IslamicSurface
import com.example.ui.theme.IslamicSurfaceVariant
import com.example.ui.theme.IslamicTextMuted
import com.example.ui.theme.IslamicTextPrimary
import com.example.ui.theme.IslamicTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepTimerBottomSheet(
    isTimerActive: Boolean,
    remainingSeconds: Long,
    onStartTimerMinutes: (Int) -> Unit,
    onStartTimerSeconds: (Long) -> Unit,
    onCancelTimer: () -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    val durationOptions = listOf(5, 10, 15, 30, 45, 60)

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
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
                .testTag("sleep_timer_sheet"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bedtime,
                        contentDescription = null,
                        tint = IslamicGold,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "مؤقت إيقاف التلاوة",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = IslamicGoldLight
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_timer_sheet")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = IslamicTextSecondary
                    )
                }
            }

            Text(
                text = "حدد مدة زمنية لإيقاف التلاوة الصوتية تلقائيًا عند انتهاء الوقت لمساعدتك على النوم والراحة",
                fontFamily = CairoFontFamily,
                fontSize = 13.sp,
                color = IslamicTextSecondary,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 18.dp)
            )

            // Active Countdown Card if timer is running
            if (isTimerActive) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(IslamicCardHover)
                        .border(1.dp, IslamicGold, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                        .testTag("active_timer_card")
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "الوقت المتبقي حتى إيقاف التلاوة:",
                            fontFamily = CairoFontFamily,
                            fontSize = 13.sp,
                            color = IslamicTextSecondary
                        )

                        Text(
                            text = SleepTimerManager.formatTime(remainingSeconds),
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp,
                            color = IslamicGoldLight
                        )

                        Text(
                            text = "ستتوقف التلاوة في الخلفية فعليًا عند الوصول إلى 00:00",
                            fontFamily = CairoFontFamily,
                            fontSize = 11.sp,
                            color = IslamicEmeraldLight
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = onCancelTimer,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = IslamicRed.copy(alpha = 0.2f),
                                contentColor = IslamicRed
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("cancel_sleep_timer_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.TimerOff,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "إلغاء المؤقت",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "أو اختر مدة جديدة لإعادة ضبط المؤقت:",
                    fontFamily = CairoFontFamily,
                    fontSize = 13.sp,
                    color = IslamicTextMuted,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Options Grid (5, 10, 15, 30, 45, 60 minutes)
            val chunked = durationOptions.chunked(3)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                chunked.forEach { rowOptions ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowOptions.forEach { minutes ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(IslamicCard)
                                    .border(1.dp, IslamicBorder, RoundedCornerShape(12.dp))
                                    .clickable {
                                        onStartTimerMinutes(minutes)
                                        onDismiss()
                                    }
                                    .padding(vertical = 14.dp)
                                    .testTag("timer_option_${minutes}_min"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "$minutes",
                                        fontFamily = CairoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = IslamicGoldLight
                                    )
                                    Text(
                                        text = "دقيقة",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 12.sp,
                                        color = IslamicTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // Quick test option (30 seconds) for immediate verification
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(IslamicSurfaceVariant)
                        .border(1.dp, IslamicBorder, RoundedCornerShape(12.dp))
                        .clickable {
                            onStartTimerSeconds(30L)
                            onDismiss()
                        }
                        .padding(vertical = 10.dp, horizontal = 14.dp)
                        .testTag("timer_option_30_sec"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = IslamicGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "اختبار سريع للمؤقت (30 ثانية)",
                            fontFamily = CairoFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = IslamicGoldLight
                        )
                    }
                }
            }
        }
    }
}
