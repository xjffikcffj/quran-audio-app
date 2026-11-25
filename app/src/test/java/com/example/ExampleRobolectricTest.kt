package com.example

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.media3.common.Player
import androidx.test.core.app.ApplicationProvider
import com.example.data.PlaybackStateStore
import com.example.data.QuranData
import com.example.service.QuranPlaybackService
import com.example.viewmodel.AudioPlayerViewModel
import com.example.viewmodel.PlaybackStatus
import org.robolectric.Robolectric
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Before
  fun setUp() {
    com.example.timer.SleepTimerManager.cancelTimer()
    QuranPlaybackService.releaseSharedEngine()
  }

  @After
  fun tearDown() {
    com.example.timer.SleepTimerManager.cancelTimer()
    QuranPlaybackService.releaseSharedEngine()
  }

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

  @Test
  fun `verify cold restore loads saved surah reciter and position without autoplay`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val prefs = app.getSharedPreferences(PlaybackStateStore.PREFS_NAME, Context.MODE_PRIVATE)
    val savedUrl = "https://server7.mp3quran.net/basit/018.mp3"
    val savedPos = 123_456L

    prefs.edit()
      .putInt(PlaybackStateStore.KEY_SURAH_NUMBER, 18)
      .putString(PlaybackStateStore.KEY_RECITER_ID, "basit")
      .putString(PlaybackStateStore.KEY_AUDIO_URL, savedUrl)
      .putLong(PlaybackStateStore.KEY_POSITION, savedPos)
      .putLong(PlaybackStateStore.positionKey(18), savedPos)
      .putFloat(PlaybackStateStore.KEY_SPEED, 1.25f)
      .commit()

    val viewModel = AudioPlayerViewModel(app)
    val state = viewModel.uiState.value
    val player = QuranPlaybackService.getSharedPlayer(app)

    assertEquals(18, state.currentSurah.number)
    assertEquals("basit", state.currentReciter.id)
    assertEquals(savedPos, state.currentPositionMs)
    assertEquals(1.25f, state.playbackSpeed, 0.001f)
    assertEquals(PlaybackStatus.IDLE, state.playbackStatus)
    assertFalse(state.isPlaying)
    assertFalse(state.isBuffering)

    assertEquals(Player.STATE_IDLE, player.playbackState)
    assertFalse(player.playWhenReady)
    assertFalse(player.isPlaying)
    assertEquals("18", player.currentMediaItem?.mediaId)
    assertEquals(savedUrl, player.currentMediaItem?.localConfiguration?.uri?.toString())
  }

  @Test
  fun `verify saved position is preserved on close and on switching surah before playback`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val prefs = app.getSharedPreferences(PlaybackStateStore.PREFS_NAME, Context.MODE_PRIVATE)
    val surah18Pos = 123_456L
    val surah36Pos = 45_000L

    prefs.edit()
      .putInt(PlaybackStateStore.KEY_SURAH_NUMBER, 18)
      .putString(PlaybackStateStore.KEY_RECITER_ID, "basit")
      .putString(PlaybackStateStore.KEY_AUDIO_URL, "https://server7.mp3quran.net/basit/018.mp3")
      .putLong(PlaybackStateStore.KEY_POSITION, surah18Pos)
      .putLong(PlaybackStateStore.positionKey(18), surah18Pos)
      .putLong(PlaybackStateStore.positionKey(36), surah36Pos)
      .commit()

    // Case A: Closing ViewModel (onCleared) before pressing play must not overwrite saved position with 0.
    val store = ViewModelStore()
    val factory = ViewModelProvider.AndroidViewModelFactory(app)
    val vmBeforeClose = ViewModelProvider(store, factory)[AudioPlayerViewModel::class.java]
    assertEquals(PlaybackStatus.IDLE, vmBeforeClose.uiState.value.playbackStatus)
    assertEquals(surah18Pos, vmBeforeClose.uiState.value.currentPositionMs)

    store.clear()

    assertEquals(surah18Pos, prefs.getLong(PlaybackStateStore.positionKey(18), -1L))
    assertEquals(surah18Pos, prefs.getLong(PlaybackStateStore.KEY_POSITION, -1L))

    // Case B: Switching to another Surah while still IDLE must preserve Surah 18's saved position.
    val vmAfterReopen = AudioPlayerViewModel(app)
    val surah36 = QuranData.surahs.first { it.number == 36 }
    vmAfterReopen.playSurah(surah36)

    assertEquals(surah18Pos, prefs.getLong(PlaybackStateStore.positionKey(18), -1L))
    assertEquals(36, vmAfterReopen.uiState.value.currentSurah.number)
    assertEquals(surah36Pos, vmAfterReopen.uiState.value.currentPositionMs)
  }

  @Test
  fun `verify togglePlayPause and play start preparation and playback after cold restore`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val prefs = app.getSharedPreferences(PlaybackStateStore.PREFS_NAME, Context.MODE_PRIVATE)
    val savedPos = 95_000L

    prefs.edit()
      .putInt(PlaybackStateStore.KEY_SURAH_NUMBER, 18)
      .putString(PlaybackStateStore.KEY_RECITER_ID, "maher")
      .putString(PlaybackStateStore.KEY_AUDIO_URL, "https://server12.mp3quran.net/maher/018.mp3")
      .putLong(PlaybackStateStore.KEY_POSITION, savedPos)
      .putLong(PlaybackStateStore.positionKey(18), savedPos)
      .commit()

    // Sub-case 1: togglePlayPause()
    val vm1 = AudioPlayerViewModel(app)
    val player1 = QuranPlaybackService.getSharedPlayer(app)
    assertEquals(Player.STATE_IDLE, player1.playbackState)
    assertFalse(player1.playWhenReady)

    vm1.togglePlayPause()

    assertTrue(player1.playWhenReady)
    assertTrue(player1.playbackState != Player.STATE_IDLE)
    assertEquals("18", player1.currentMediaItem?.mediaId)
    assertEquals(savedPos, prefs.getLong(PlaybackStateStore.positionKey(18), -1L))

    // Sub-case 2: play() after fresh engine reset
    QuranPlaybackService.releaseSharedEngine()
    val vm2 = AudioPlayerViewModel(app)
    val player2 = QuranPlaybackService.getSharedPlayer(app)
    assertEquals(Player.STATE_IDLE, player2.playbackState)
    assertFalse(player2.playWhenReady)

    vm2.play()

    assertTrue(player2.playWhenReady)
    assertTrue(player2.playbackState != Player.STATE_IDLE)
    assertEquals("18", player2.currentMediaItem?.mediaId)
    assertEquals(savedPos, prefs.getLong(PlaybackStateStore.positionKey(18), -1L))
  }

  @Test
  fun `verify QuranPlaybackService restores saved media without autoplay on create and starts on ACTION_PLAY without ViewModel`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val prefs = app.getSharedPreferences(PlaybackStateStore.PREFS_NAME, Context.MODE_PRIVATE)
    val savedUrl = "https://server13.mp3quran.net/husr/036.mp3"
    val savedPos = 77_000L

    prefs.edit()
      .putInt(PlaybackStateStore.KEY_SURAH_NUMBER, 36)
      .putString(PlaybackStateStore.KEY_RECITER_ID, "husr")
      .putString(PlaybackStateStore.KEY_AUDIO_URL, savedUrl)
      .putLong(PlaybackStateStore.KEY_POSITION, savedPos)
      .putLong(PlaybackStateStore.positionKey(36), savedPos)
      .commit()

    // Step 1: Service created without ViewModel and without ACTION_PLAY restores item in IDLE without autoplay.
    val idleController = Robolectric.buildService(QuranPlaybackService::class.java).create()
    val playerIdle = QuranPlaybackService.getSharedPlayer(app)
    assertEquals("36", playerIdle.currentMediaItem?.mediaId)
    assertEquals(savedUrl, playerIdle.currentMediaItem?.localConfiguration?.uri?.toString())
    assertEquals(Player.STATE_IDLE, playerIdle.playbackState)
    assertFalse(playerIdle.playWhenReady)
    idleController.destroy()

    // Step 2: Simulate full process restart (shared engine cleared) + ACTION_PLAY arriving before any ViewModel.
    QuranPlaybackService.releaseSharedEngine()
    val playIntent = Intent(app, QuranPlaybackService::class.java).apply {
      action = QuranPlaybackService.ACTION_PLAY
    }
    val playController = Robolectric.buildService(QuranPlaybackService::class.java, playIntent)
      .create()
      .startCommand(0, 0)

    val playerAfterAction = QuranPlaybackService.getSharedPlayer(app)
    assertEquals("36", playerAfterAction.currentMediaItem?.mediaId)
    assertEquals(savedUrl, playerAfterAction.currentMediaItem?.localConfiguration?.uri?.toString())
    assertTrue(playerAfterAction.playbackState != Player.STATE_IDLE)
    assertTrue(playerAfterAction.playWhenReady)
    assertEquals(savedPos, playerAfterAction.currentPosition)
    playController.destroy()
  }

  @Test
  fun `verify skipForward before STATE_READY does not overwrite saved position`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val prefs = app.getSharedPreferences(PlaybackStateStore.PREFS_NAME, Context.MODE_PRIVATE)
    val savedPos = 123_456L

    prefs.edit()
      .putInt(PlaybackStateStore.KEY_SURAH_NUMBER, 18)
      .putString(PlaybackStateStore.KEY_RECITER_ID, "basit")
      .putString(PlaybackStateStore.KEY_AUDIO_URL, "https://server7.mp3quran.net/basit/018.mp3")
      .putLong(PlaybackStateStore.KEY_POSITION, savedPos)
      .putLong(PlaybackStateStore.positionKey(18), savedPos)
      .commit()

    val store = ViewModelStore()
    val factory = ViewModelProvider.AndroidViewModelFactory(app)
    val viewModel = ViewModelProvider(store, factory)[AudioPlayerViewModel::class.java]
    val player = QuranPlaybackService.getSharedPlayer(app)

    assertEquals(Player.STATE_IDLE, player.playbackState)
    viewModel.skipForward(10)
    assertEquals(savedPos, viewModel.uiState.value.currentPositionMs)
    assertEquals(savedPos, prefs.getLong(PlaybackStateStore.positionKey(18), -1L))

    viewModel.play()
    assertTrue(player.playbackState != Player.STATE_READY)
    viewModel.skipForward(10)
    assertEquals(savedPos, viewModel.uiState.value.currentPositionMs)
    assertEquals(savedPos, prefs.getLong(PlaybackStateStore.positionKey(18), -1L))

    store.clear()
    assertEquals(savedPos, prefs.getLong(PlaybackStateStore.positionKey(18), -1L))
    assertEquals(savedPos, prefs.getLong(PlaybackStateStore.KEY_POSITION, -1L))
  }

  @Test
  fun `verify closing ViewModel during STATE_BUFFERING preserves saved position`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val prefs = app.getSharedPreferences(PlaybackStateStore.PREFS_NAME, Context.MODE_PRIVATE)
    val savedPos = 88_000L

    prefs.edit()
      .putInt(PlaybackStateStore.KEY_SURAH_NUMBER, 18)
      .putString(PlaybackStateStore.KEY_RECITER_ID, "maher")
      .putString(PlaybackStateStore.KEY_AUDIO_URL, "https://server12.mp3quran.net/maher/018.mp3")
      .putLong(PlaybackStateStore.KEY_POSITION, savedPos)
      .putLong(PlaybackStateStore.positionKey(18), savedPos)
      .commit()

    val store = ViewModelStore()
    val factory = ViewModelProvider.AndroidViewModelFactory(app)
    val viewModel = ViewModelProvider(store, factory)[AudioPlayerViewModel::class.java]
    val player = QuranPlaybackService.getSharedPlayer(app)

    viewModel.togglePlayPause()
    assertEquals(Player.STATE_BUFFERING, player.playbackState)
    assertTrue(player.playWhenReady)

    store.clear()

    assertEquals(savedPos, prefs.getLong(PlaybackStateStore.positionKey(18), -1L))
    assertEquals(savedPos, prefs.getLong(PlaybackStateStore.KEY_POSITION, -1L))
  }

  @Test
  fun `verify ACTION_PLAY onStartCommand restores empty player independently of onCreate`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val prefs = app.getSharedPreferences(PlaybackStateStore.PREFS_NAME, Context.MODE_PRIVATE)
    val savedUrl = "https://server13.mp3quran.net/husr/036.mp3"
    val savedPos = 77_000L

    prefs.edit()
      .putInt(PlaybackStateStore.KEY_SURAH_NUMBER, 36)
      .putString(PlaybackStateStore.KEY_RECITER_ID, "husr")
      .putString(PlaybackStateStore.KEY_AUDIO_URL, savedUrl)
      .putLong(PlaybackStateStore.KEY_POSITION, savedPos)
      .putLong(PlaybackStateStore.positionKey(36), savedPos)
      .commit()

    val playIntent = Intent(app, QuranPlaybackService::class.java).apply {
      action = QuranPlaybackService.ACTION_PLAY
    }
    val controller = Robolectric.buildService(QuranPlaybackService::class.java, playIntent).create()

    // Clear the item populated by onCreate() so onStartCommand's own empty-player fallback runs.
    val playerBeforeCommand = QuranPlaybackService.getSharedPlayer(app)
    playerBeforeCommand.clearMediaItems()
    assertEquals(null, playerBeforeCommand.currentMediaItem)
    assertEquals(Player.STATE_IDLE, playerBeforeCommand.playbackState)

    controller.startCommand(0, 0)

    val playerAfterCommand = QuranPlaybackService.getSharedPlayer(app)
    assertEquals("36", playerAfterCommand.currentMediaItem?.mediaId)
    assertEquals(savedUrl, playerAfterCommand.currentMediaItem?.localConfiguration?.uri?.toString())
    assertTrue(playerAfterCommand.playbackState != Player.STATE_IDLE)
    assertTrue(playerAfterCommand.playWhenReady)
    assertEquals(savedPos, playerAfterCommand.currentPosition)
    controller.destroy()
  }
}
