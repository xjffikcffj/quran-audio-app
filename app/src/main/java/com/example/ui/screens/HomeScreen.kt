package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ads.AdManager
import com.example.data.FavoritesManager
import com.example.data.QuranData
import com.example.model.Surah
import com.example.ui.components.AdBannerView
import com.example.ui.components.DeveloperDashboardDialog
import com.example.ui.components.FullPlayerSheet
import com.example.ui.components.MiniPlayer
import com.example.ui.components.ReciterSelectionBottomSheet
import com.example.ui.components.SleepTimerBottomSheet
import com.example.ui.components.SurahCard
import com.example.ui.components.ThemeSettingsBottomSheet
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.IslamicBorder
import com.example.ui.theme.IslamicCard
import com.example.ui.theme.IslamicCardHover
import com.example.ui.theme.IslamicDarkBg
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.IslamicGoldLight
import com.example.ui.theme.IslamicTextMuted
import com.example.ui.theme.IslamicTextPrimary
import com.example.ui.theme.IslamicTextSecondary
import com.example.ui.theme.ThemeManager
import com.example.viewmodel.AudioPlayerViewModel

enum class SurahFilter(val labelArabic: String) {
    ALL("جميع السور (114)"),
    FAVORITES("المفضلة ⭐"),
    MECCAN("مكية (86)"),
    MEDINAN("مدنية (28)")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: AudioPlayerViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val playerState by viewModel.uiState.collectAsState()
    val isTimerActive by viewModel.isSleepTimerActive.collectAsState()
    val remainingSeconds by viewModel.sleepTimerRemainingSeconds.collectAsState()
    val favoriteSurahs by FavoritesManager.favoriteSurahs.collectAsState()
    val themeMode by ThemeManager.themeMode.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(SurahFilter.ALL) }
    var showReciterSheet by remember { mutableStateOf(false) }
    var showSleepTimerSheet by remember { mutableStateOf(false) }
    var showThemeSheet by remember { mutableStateOf(false) }
    var showDeveloperDashboard by remember { mutableStateOf(false) }

    // Filter surahs based on query, revelation type, and favorites
    val filteredSurahs = remember(searchQuery, selectedFilter, favoriteSurahs) {
        QuranData.surahs.filter { surah ->
            val matchesFilter = when (selectedFilter) {
                SurahFilter.ALL -> true
                SurahFilter.FAVORITES -> favoriteSurahs.contains(surah.number)
                SurahFilter.MECCAN -> surah.isMeccan
                SurahFilter.MEDINAN -> !surah.isMeccan
            }

            val queryClean = searchQuery.trim()
            val matchesQuery = if (queryClean.isEmpty()) {
                true
            } else {
                surah.nameArabic.contains(queryClean, ignoreCase = true) ||
                        surah.nameEnglish.contains(queryClean, ignoreCase = true) ||
                        surah.number.toString() == queryClean
            }

            matchesFilter && matchesQuery
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    MiniPlayer(
                        state = playerState,
                        onTogglePlayPause = { viewModel.togglePlayPause() },
                        onPlayNext = { viewModel.playNext() },
                        onPlayPrevious = { viewModel.playPrevious() },
                        onClick = { viewModel.setPlayerExpanded(true) },
                        onRetry = { viewModel.retry() },
                        isTimerActive = isTimerActive,
                        remainingSeconds = remainingSeconds,
                        onTimerClick = { showSleepTimerSheet = true }
                    )

                    AdBannerView(
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            },
            modifier = modifier.fillMaxSize()
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header item
                item(key = "header") {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Quran Audio – قرآن صوتي",
                                    fontFamily = AmiriFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "المصحف المرتل برواية حفص عن عاصم",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Theme Mode Switcher
                                IconButton(
                                    onClick = { showThemeSheet = true },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(IslamicCard)
                                        .border(1.dp, IslamicBorder, CircleShape)
                                        .testTag("theme_toggle_button")
                                ) {
                                    Icon(
                                        imageVector = when (themeMode) {
                                            AppThemeMode.PURE_NIGHT -> Icons.Default.Bedtime
                                            AppThemeMode.DARK -> Icons.Default.DarkMode
                                            AppThemeMode.LIGHT -> Icons.Default.LightMode
                                        },
                                        contentDescription = "الوضع الليلي والمظهر",
                                        tint = IslamicGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // Developer Portal Button
                                IconButton(
                                    onClick = { showDeveloperDashboard = true },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(IslamicCard)
                                        .border(1.dp, IslamicBorder, CircleShape)
                                        .testTag("developer_portal_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AdminPanelSettings,
                                        contentDescription = "دخول المطوّر / إدارة التطبيق",
                                        tint = IslamicGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // Quick Reciter Button
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(IslamicCard)
                                        .border(1.dp, IslamicGold.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                        .clickable { showReciterSheet = true }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                        .testTag("reciter_quick_button"),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = IslamicGold,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = playerState.currentReciter.nameArabic.split(" ").firstOrNull() ?: "القارئ",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = IslamicGold,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Fast Search Box
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    text = "ابحث عن سورة بالاسم أو الرقم…",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 14.sp,
                                    color = IslamicTextMuted
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "بحث",
                                    tint = IslamicGold
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { searchQuery = "" },
                                        modifier = Modifier.testTag("clear_search")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "مسح البحث",
                                            tint = IslamicTextSecondary
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = IslamicCard,
                                unfocusedContainerColor = IslamicCard,
                                focusedBorderColor = IslamicGold,
                                unfocusedBorderColor = IslamicBorder,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_text_field")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Filter Chips (All, Favorites, Meccan, Medinan)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SurahFilter.values().forEach { filter ->
                                val isSelected = filter == selectedFilter
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(
                                            if (isSelected) IslamicGold else IslamicCard
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) IslamicGoldLight else IslamicBorder,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .clickable { selectedFilter = filter }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                        .testTag("filter_${filter.name.lowercase()}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = filter.labelArabic,
                                        fontFamily = CairoFontFamily,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color(0xFF1B1400) else IslamicTextSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                // Empty state if search finds nothing or favorites list is empty
                if (filteredSurahs.isEmpty()) {
                    item(key = "empty_state") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp, horizontal = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = if (selectedFilter == SurahFilter.FAVORITES) Icons.Default.Star else Icons.Outlined.SearchOff,
                                contentDescription = null,
                                tint = if (selectedFilter == SurahFilter.FAVORITES) IslamicGold else IslamicTextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = if (selectedFilter == SurahFilter.FAVORITES)
                                    "لم تقم بإضافة أي سورة إلى المفضلة بعد.\nاضغط على أيقونة النجمة ⭐ بجوار أي سورة لحفظها هنا والوصول إليها بسرعة."
                                else
                                    "لم يتم العثور على أي سورة مطابقة",
                                fontFamily = CairoFontFamily,
                                fontSize = 15.sp,
                                color = IslamicTextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(
                        items = filteredSurahs,
                        key = { it.number }
                    ) { surah ->
                        val isCurrent = surah.number == playerState.currentSurah.number
                        val isFav = favoriteSurahs.contains(surah.number)
                        SurahCard(
                            surah = surah,
                            isCurrent = isCurrent,
                            isPlaying = isCurrent && playerState.isPlaying,
                            isFavorite = isFav,
                            onToggleFavorite = { FavoritesManager.toggleSurahFavorite(surah.number) },
                            onClick = {
                                if (isCurrent) {
                                    viewModel.togglePlayPause()
                                } else {
                                    if (activity != null) {
                                        AdManager.onSurahSelected(activity) {
                                            viewModel.playSurah(surah)
                                        }
                                    } else {
                                        viewModel.playSurah(surah)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        // Full Player Modal
        if (playerState.isPlayerExpanded) {
            val isCurrentFav = favoriteSurahs.contains(playerState.currentSurah.number)
            FullPlayerSheet(
                state = playerState,
                onDismiss = { viewModel.setPlayerExpanded(false) },
                onTogglePlayPause = { viewModel.togglePlayPause() },
                onSeekTo = { viewModel.seekTo(it) },
                onSkipForward = { viewModel.skipForward(10) },
                onSkipBackward = { viewModel.skipBackward(10) },
                onPlayNext = { viewModel.playNext() },
                onPlayPrevious = { viewModel.playPrevious() },
                onToggleSpeed = {
                    val nextSpeed = when (playerState.playbackSpeed) {
                        0.75f -> 1.0f
                        1.0f -> 1.25f
                        1.25f -> 1.5f
                        else -> 0.75f
                    }
                    viewModel.setPlaybackSpeed(nextSpeed)
                },
                onToggleRepeat = { viewModel.toggleRepeatOne() },
                onOpenReciterSelector = { showReciterSheet = true },
                onRetry = { viewModel.retry() },
                isTimerActive = isTimerActive,
                remainingSeconds = remainingSeconds,
                onOpenSleepTimer = { showSleepTimerSheet = true },
                isFavorite = isCurrentFav,
                onToggleFavorite = { FavoritesManager.toggleSurahFavorite(playerState.currentSurah.number) }
            )
        }

        // Reciter Selection Sheet
        if (showReciterSheet) {
            ReciterSelectionBottomSheet(
                selectedReciter = playerState.currentReciter,
                onSelectReciter = { reciter ->
                    viewModel.setReciter(reciter)
                },
                onDismiss = { showReciterSheet = false }
            )
        }

        // Sleep Timer Sheet
        if (showSleepTimerSheet) {
            SleepTimerBottomSheet(
                isTimerActive = isTimerActive,
                remainingSeconds = remainingSeconds,
                onStartTimerMinutes = { minutes -> viewModel.startSleepTimer(minutes) },
                onStartTimerSeconds = { seconds -> viewModel.startSleepTimerSeconds(seconds) },
                onCancelTimer = { viewModel.cancelSleepTimer() },
                onDismiss = { showSleepTimerSheet = false }
            )
        }

        // Theme / Night Mode Settings Sheet
        if (showThemeSheet) {
            ThemeSettingsBottomSheet(
                currentMode = themeMode,
                onSelectMode = { mode ->
                    ThemeManager.setThemeMode(mode)
                    showThemeSheet = false
                },
                onDismiss = { showThemeSheet = false }
            )
        }

        // Developer Dashboard Dialog
        if (showDeveloperDashboard) {
            DeveloperDashboardDialog(
                onDismiss = { showDeveloperDashboard = false }
            )
        }
    }
}
