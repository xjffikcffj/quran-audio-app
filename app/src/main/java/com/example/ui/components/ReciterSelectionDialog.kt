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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.data.DeveloperContentManager
import com.example.data.FavoritesManager
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.QuranData
import com.example.model.Reciter
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.IslamicBorder
import com.example.ui.theme.IslamicCard
import com.example.ui.theme.IslamicCardHover
import com.example.ui.theme.IslamicDarkBg
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.IslamicGoldLight
import com.example.ui.theme.IslamicSurface
import com.example.ui.theme.IslamicTextMuted
import com.example.ui.theme.IslamicTextPrimary
import com.example.ui.theme.IslamicTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReciterSelectionBottomSheet(
    selectedReciter: Reciter,
    onSelectReciter: (Reciter) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = IslamicDarkBg,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(IslamicBorder)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .testTag("reciter_selection_sheet")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "اختيار القارئ",
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = IslamicGoldLight
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_reciter_sheet")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = IslamicTextSecondary
                    )
                }
            }

            Text(
                text = "اختر القارئ المفضل للاستماع إلى تلاوة القرآن الكريم",
                fontFamily = CairoFontFamily,
                fontSize = 13.sp,
                color = IslamicTextSecondary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val allReciters by DeveloperContentManager.recitersList.collectAsState()
            val hiddenIds by DeveloperContentManager.hiddenReciterIds.collectAsState()
            val favoriteReciterIds by FavoritesManager.favoriteReciters.collectAsState()
            val visibleReciters = allReciters.filterNot { hiddenIds.contains(it.id) }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(visibleReciters, key = { it.id }) { reciter ->
                    val isSelected = reciter.id == selectedReciter.id
                    val isFav = favoriteReciterIds.contains(reciter.id)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) IslamicCardHover else IslamicCard)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) IslamicGold else IslamicBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                onSelectReciter(reciter)
                                onDismiss()
                            }
                            .padding(14.dp)
                            .testTag("reciter_item_${reciter.id}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) IslamicGold.copy(alpha = 0.2f) else IslamicSurface
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) IslamicGold else IslamicBorder,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (isSelected) IslamicGold else IslamicTextSecondary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = reciter.nameArabic,
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = if (isSelected) IslamicGoldLight else IslamicTextPrimary
                                )
                                Text(
                                    text = reciter.riwayah,
                                    fontFamily = CairoFontFamily,
                                    fontSize = 12.sp,
                                    color = IslamicTextMuted
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = { FavoritesManager.toggleReciterFavorite(reciter.id) },
                                modifier = Modifier.size(36.dp).testTag("fav_reciter_${reciter.id}")
                            ) {
                                Icon(
                                    imageVector = if (isFav) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                    contentDescription = if (isFav) "إزالة من القراء المفضلين" else "إضافة للقراء المفضلين",
                                    tint = if (isFav) IslamicGold else IslamicTextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(IslamicGold),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "تم الاختيار",
                                        tint = Color(0xFF1E1700),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
