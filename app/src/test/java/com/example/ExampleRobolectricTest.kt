package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.QuranData
import com.example.service.QuranPlaybackService
import com.example.viewmodel.AudioPlayerViewModel
import com.example.viewmodel.PlaybackStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Quran Audio – قرآن صوتي", appName)
  }

  @Test
  fun `verify all 114 surahs are loaded with required properties`() {
    val surahs = QuranData.surahs
    assertEquals(114, surahs.size)

    val first = surahs.first()
    assertEquals(1, first.id)
    assertEquals(1, first.number)
    assertEquals("الفاتحة", first.name)
    assertEquals(7, first.verseCount)
    assertEquals(7, first.versesCount)
    assertEquals("https://server8.mp3quran.net/afs/001.mp3", first.audioUrl)

    val last = surahs.last()
    assertEquals(114, last.id)
    assertEquals(114, last.number)
    assertEquals("الناس", last.name)
    assertEquals(6, last.verseCount)
    assertEquals(6, last.versesCount)
    assertEquals("https://server8.mp3quran.net/afs/114.mp3", last.audioUrl)

    // Check all surahs have valid positive ids, non-empty names, verseCount > 0, and non-empty audioUrl
    surahs.forEach { surah ->
      assertTrue(surah.id in 1..114)
      assertTrue(surah.name.isNotEmpty())
      assertTrue(surah.verseCount > 0)
      assertTrue(surah.audioUrl.startsWith("http"))
    }
  }

  @Test
  fun `verify reciter audio url format`() {
    val reciter = QuranData.reciters.first()
    val url = reciter.getAudioUrlForSurah(1)
    assertEquals("https://server8.mp3quran.net/afs/001.mp3", url)
  }

  @Test
  fun `verify AudioPlayerViewModel initialization and default state`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = AudioPlayerViewModel(app)

    val state = viewModel.uiState.value
    assertNotNull(state)
    assertEquals(1, state.currentSurah.number)
    assertEquals(PlaybackStatus.IDLE, state.playbackStatus)
    assertFalse(state.isPlaying)
    assertFalse(state.isBuffering)
    assertEquals(1.0f, state.playbackSpeed, 0.001f)
  }

  @Test
  fun `verify AudioPlayerViewModel playSurah changes state to buffering`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = AudioPlayerViewModel(app)

    val baqarah = QuranData.surahs[1] // Surah Al-Baqarah
    viewModel.playSurah(baqarah)

    val state = viewModel.uiState.value
    assertEquals(2, state.currentSurah.number)
    assertEquals("البقرة", state.currentSurah.name)
    assertEquals(PlaybackStatus.BUFFERING, state.playbackStatus)
    assertTrue(state.isBuffering)
  }

  @Test
  fun `verify AudioPlayerViewModel next and previous navigation`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = AudioPlayerViewModel(app)

    viewModel.playNext()
    assertEquals(2, viewModel.uiState.value.currentSurah.number)

    viewModel.playPrevious()
    assertEquals(1, viewModel.uiState.value.currentSurah.number)
  }

  @Test
  fun `verify ViewModel lifecycle preserves shared background playback engine`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = AudioPlayerViewModel(app)

    val playerBefore = QuranPlaybackService.getSharedPlayer(app)
    assertNotNull(playerBefore)

    viewModel.playSurah(QuranData.surahs.first())

    val playerAfter = QuranPlaybackService.getSharedPlayer(app)
    assertNotNull(playerAfter)
    assertEquals(playerBefore, playerAfter)
  }

  @Test
  fun `verify SleepTimerManager formatTime`() {
    assertEquals("00:30", com.example.timer.SleepTimerManager.formatTime(30))
    assertEquals("05:00", com.example.timer.SleepTimerManager.formatTime(300))
    assertEquals("30:00", com.example.timer.SleepTimerManager.formatTime(1800))
    assertEquals("60:00", com.example.timer.SleepTimerManager.formatTime(3600))
  }

  @Test
  fun `verify SleepTimerManager start and cancel operations`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val timer = com.example.timer.SleepTimerManager

    timer.startTimer(context, 15)
    assertTrue(timer.isTimerActive.value)
    assertTrue(timer.remainingSeconds.value in 890L..900L)

    timer.cancelTimer()
    assertFalse(timer.isTimerActive.value)
    assertEquals(0L, timer.remainingSeconds.value)
  }

  @Test
  fun `verify AudioPlayerViewModel sleep timer integration`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = AudioPlayerViewModel(app)

    viewModel.startSleepTimer(30)
    assertTrue(viewModel.isSleepTimerActive.value)
    assertTrue(viewModel.sleepTimerRemainingSeconds.value in 1790L..1800L)

    viewModel.cancelSleepTimer()
    assertFalse(viewModel.isSleepTimerActive.value)
    assertEquals(0L, viewModel.sleepTimerRemainingSeconds.value)
  }

  @Test
  fun `verify FavoritesManager surah and reciter toggle`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.data.FavoritesManager.initialize(context)

    val manager = com.example.data.FavoritesManager
    assertFalse(manager.isSurahFavorite(1))

    manager.toggleSurahFavorite(1)
    assertTrue(manager.isSurahFavorite(1))
    assertTrue(manager.favoriteSurahs.value.contains(1))

    manager.toggleSurahFavorite(1)
    assertFalse(manager.isSurahFavorite(1))

    // Reciter favorite test
    assertFalse(manager.isReciterFavorite("afs"))
    manager.toggleReciterFavorite("afs")
    assertTrue(manager.isReciterFavorite("afs"))
    manager.toggleReciterFavorite("afs")
    assertFalse(manager.isReciterFavorite("afs"))
  }

  @Test
  fun `verify ThemeManager modes and cycling`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.ui.theme.ThemeManager.initialize(context)
    val manager = com.example.ui.theme.ThemeManager

    manager.setThemeMode(com.example.ui.theme.AppThemeMode.PURE_NIGHT)
    assertEquals(com.example.ui.theme.AppThemeMode.PURE_NIGHT, manager.themeMode.value)

    manager.toggleNextMode()
    assertEquals(com.example.ui.theme.AppThemeMode.LIGHT, manager.themeMode.value)

    manager.toggleNextMode()
    assertEquals(com.example.ui.theme.AppThemeMode.DARK, manager.themeMode.value)
  }

  @Test
  fun `verify DeveloperContentManager adding reciters and backup export`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.data.DeveloperContentManager.initialize(context)
    val manager = com.example.data.DeveloperContentManager

    val initialCount = manager.recitersList.value.size
    manager.addCustomReciter("الشيخ المنشاوي", "مرتل", "https://server10.mp3quran.net/minsh/")
    assertEquals(initialCount + 1, manager.recitersList.value.size)

    val backupJson = manager.exportBackupJson()
    assertTrue(backupJson.contains("الشيخ المنشاوي"))
    assertTrue(backupJson.contains("isBannerEnabled"))
  }

  @Test
  fun `verify DeveloperAuthManager setup and login security`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.data.DeveloperAuthManager.initialize(context)
    val auth = com.example.data.DeveloperAuthManager

    assertTrue(auth.setupDeveloperAccount("dev@example.com", "strongPassword123"))
    assertTrue(auth.isLoggedIn.value)

    auth.logout()
    assertFalse(auth.isLoggedIn.value)

    // Wrong password fails
    assertFalse(auth.login("dev@example.com", "wrongPass"))
    assertFalse(auth.isLoggedIn.value)

    // Correct password succeeds
    assertTrue(auth.login("dev@example.com", "strongPassword123"))
    assertTrue(auth.isLoggedIn.value)
  }
}
