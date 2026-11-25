import { useEffect, useMemo, useState } from 'react';
import {
  BedDouble,
  ChevronDown,
  Moon,
  Search,
  SearchX,
  ShieldCheck,
  Star,
  Sun,
  User,
  X,
} from 'lucide-react';
import { QURAN_SURAHS } from '../data/quranData';
import { useAudioPlayer } from '../hooks/useAudioPlayer';
import { favoritesManager } from '../services/favoritesManager';
import { sleepTimerManager } from '../services/sleepTimerManager';
import { themeManager } from '../services/themeManager';
import {
  AppThemeMode,
  SURAH_FILTER_LABELS,
  SurahFilter,
} from '../types/quran';
import { normalizeSearchText } from '../utils/searchTextNormalizer';
import { AdBannerView } from '../components/AdBannerView';
import { DeveloperDashboardDialog } from '../components/DeveloperDashboardDialog';
import { FullPlayerSheet } from '../components/FullPlayerSheet';
import { MiniPlayer } from '../components/MiniPlayer';
import { ReciterSelectionSheet } from '../components/ReciterSelectionSheet';
import { SleepTimerBottomSheet } from '../components/SleepTimerBottomSheet';
import { SurahCard } from '../components/SurahCard';
import { ThemeSettingsBottomSheet } from '../components/ThemeSettingsBottomSheet';
import { Toast } from '../components/Toast';

const FILTERS: SurahFilter[] = [
  SurahFilter.ALL,
  SurahFilter.FAVORITES,
  SurahFilter.MECCAN,
  SurahFilter.MEDINAN,
];

export function HomeScreen() {
  const {
    state: playerState,
    playSurah,
    togglePlayPause,
    seekTo,
    skipForward,
    skipBackward,
    playNext,
    playPrevious,
    setReciter,
    setPlaybackSpeed,
    toggleRepeatOne,
    retry,
    setPlayerExpanded,
  } = useAudioPlayer();

  const [timerState, setTimerState] = useState(() =>
    sleepTimerManager.getState(),
  );
  const [favoriteSurahs, setFavoriteSurahs] = useState<Set<number>>(() =>
    favoritesManager.getFavoriteSurahs(),
  );
  const [themeMode, setThemeMode] = useState<AppThemeMode>(() =>
    themeManager.getThemeMode(),
  );

  const [searchQuery, setSearchQuery] = useState('');
  const [selectedFilter, setSelectedFilter] = useState<SurahFilter>(
    SurahFilter.ALL,
  );
  const [showReciterSheet, setShowReciterSheet] = useState(false);
  const [showSleepTimerSheet, setShowSleepTimerSheet] = useState(false);
  const [showThemeSheet, setShowThemeSheet] = useState(false);
  const [showDeveloperDashboard, setShowDeveloperDashboard] = useState(false);
  const [toastMessage, setToastMessage] = useState<string | null>(null);

  useEffect(() => {
    const unsubTimer = sleepTimerManager.subscribe(() => {
      setTimerState(sleepTimerManager.getState());
    });
    const unsubFav = favoritesManager.subscribe(() => {
      setFavoriteSurahs(favoritesManager.getFavoriteSurahs());
    });
    const unsubTheme = themeManager.subscribe(() => {
      setThemeMode(themeManager.getThemeMode());
    });
    return () => {
      unsubTimer();
      unsubFav();
      unsubTheme();
    };
  }, []);

  const queryClean = normalizeSearchText(searchQuery);

  const filteredSurahs = useMemo(() => {
    return QURAN_SURAHS.filter((surah) => {
      const matchesFilter =
        selectedFilter === SurahFilter.ALL
          ? true
          : selectedFilter === SurahFilter.FAVORITES
            ? favoriteSurahs.has(surah.number)
            : selectedFilter === SurahFilter.MECCAN
              ? surah.isMeccan
              : !surah.isMeccan;

      const matchesQuery =
        queryClean.length === 0
          ? true
          : normalizeSearchText(surah.nameArabic).includes(queryClean) ||
            normalizeSearchText(surah.nameEnglish).includes(queryClean) ||
            String(surah.number) === queryClean;

      return matchesFilter && matchesQuery;
    });
  }, [queryClean, selectedFilter, favoriteSurahs]);

  const currentReciterFirstName =
    playerState.currentReciter.nameArabic.split(' ')[0] || 'القارئ';

  const ThemeIcon =
    themeMode === AppThemeMode.PURE_NIGHT
      ? BedDouble
      : themeMode === AppThemeMode.LIGHT
        ? Sun
        : Moon;

  return (
    <div
      className="min-h-screen flex flex-col"
      style={{
        backgroundColor: 'var(--islamic-bg)',
        color: 'var(--islamic-text-primary)',
      }}
    >
      <Toast
        message={toastMessage}
        onDismiss={() => setToastMessage(null)}
      />

      {/* Main Content Container */}
      <main className="flex-1 w-full max-w-4xl mx-auto px-4 pt-3 pb-36 flex flex-col">
        {/* Header */}
        <header className="w-full py-2 flex items-center justify-between gap-3">
          <div className="min-w-0">
            <h1
              className="font-amiri font-bold text-[22px] sm:text-2xl leading-tight truncate"
              style={{ color: 'var(--islamic-gold)' }}
            >
              Quran Audio – قرآن صوتي
            </h1>
            <p
              className="font-cairo text-[11px] truncate"
              style={{ color: 'var(--islamic-text-secondary)' }}
            >
              المصحف المرتل برواية حفص عن عاصم
            </p>
          </div>

          <div className="flex items-center gap-1.5 shrink-0">
            <button
              type="button"
              onClick={() => setShowThemeSheet(true)}
              aria-label="الوضع الليلي والمظهر"
              data-testid="theme_toggle_button"
              className="w-[38px] h-[38px] rounded-full border flex items-center justify-center cursor-pointer transition-colors"
              style={{
                backgroundColor: 'var(--islamic-card)',
                borderColor: 'var(--islamic-border)',
                color: 'var(--islamic-gold)',
              }}
            >
              <ThemeIcon size={18} />
            </button>

            <button
              type="button"
              onClick={() => setShowDeveloperDashboard(true)}
              aria-label="دخول المطوّر / إدارة التطبيق"
              data-testid="developer_portal_button"
              className="w-[38px] h-[38px] rounded-full border flex items-center justify-center cursor-pointer transition-colors"
              style={{
                backgroundColor: 'var(--islamic-card)',
                borderColor: 'var(--islamic-border)',
                color: 'var(--islamic-gold)',
              }}
            >
              <ShieldCheck size={18} />
            </button>

            <button
              type="button"
              onClick={() => setShowReciterSheet(true)}
              data-testid="reciter_quick_button"
              className="h-[38px] px-2.5 rounded-full border flex items-center gap-1 cursor-pointer whitespace-nowrap shrink-0"
              style={{
                backgroundColor: 'var(--islamic-card)',
                borderColor:
                  'color-mix(in srgb, var(--islamic-gold) 50%, transparent)',
                color: 'var(--islamic-text-primary)',
              }}
            >
              <User size={15} style={{ color: 'var(--islamic-gold)' }} />
              <span className="font-cairo text-[11px] font-bold">
                {currentReciterFirstName}
              </span>
              <ChevronDown
                size={15}
                style={{ color: 'var(--islamic-gold)' }}
              />
            </button>
          </div>
        </header>

        {/* Search Input */}
        <div className="mt-2.5 relative">
          <Search
            size={18}
            className="absolute right-3.5 top-1/2 -translate-y-1/2 pointer-events-none"
            style={{ color: 'var(--islamic-gold)' }}
          />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="ابحث عن سورة بالاسم أو الرقم…"
            data-testid="search_text_field"
            className="w-full rounded-[14px] border pr-10 pl-10 py-3 font-cairo text-sm outline-none transition-colors"
            style={{
              backgroundColor: 'var(--islamic-card)',
              borderColor: 'var(--islamic-border)',
              color: 'var(--islamic-text-primary)',
            }}
          />
          {searchQuery.length > 0 && (
            <button
              type="button"
              onClick={() => setSearchQuery('')}
              aria-label="مسح البحث"
              data-testid="clear_search"
              className="w-9 h-9 rounded-full flex items-center justify-center absolute left-1.5 top-1/2 -translate-y-1/2 cursor-pointer"
              style={{ color: 'var(--islamic-text-secondary)' }}
            >
              <X size={18} />
            </button>
          )}
        </div>

        {/* Filter Chips Row */}
        <div className="mt-3 flex items-center gap-2 overflow-x-auto pb-1">
          {FILTERS.map((filter) => {
            const isSelected = filter === selectedFilter;
            return (
              <button
                key={filter}
                type="button"
                onClick={() => setSelectedFilter(filter)}
                data-testid={`filter_${filter.toLowerCase()}`}
                className="px-3.5 py-1.5 rounded-full border font-cairo text-xs whitespace-nowrap shrink-0 cursor-pointer transition-colors"
                style={{
                  backgroundColor: isSelected
                    ? 'var(--islamic-gold)'
                    : 'var(--islamic-card)',
                  borderColor: isSelected
                    ? 'var(--islamic-gold-light)'
                    : 'var(--islamic-border)',
                  color: isSelected
                    ? 'var(--islamic-on-gold)'
                    : 'var(--islamic-text-secondary)',
                  fontWeight: isSelected ? 700 : 400,
                }}
              >
                {SURAH_FILTER_LABELS[filter]}
              </button>
            );
          })}
        </div>

        {/* Surahs List or Empty State */}
        {filteredSurahs.length === 0 ? (
          <div className="py-12 px-5 flex flex-col items-center justify-center gap-2.5 text-center">
            {selectedFilter === SurahFilter.FAVORITES ? (
              <Star
                size={48}
                fill="var(--islamic-gold)"
                style={{ color: 'var(--islamic-gold)' }}
              />
            ) : (
              <SearchX
                size={48}
                style={{ color: 'var(--islamic-text-muted)' }}
              />
            )}
            <p
              className="font-cairo text-[15px] whitespace-pre-line max-w-md"
              style={{ color: 'var(--islamic-text-secondary)' }}
            >
              {selectedFilter === SurahFilter.FAVORITES
                ? 'لم تقم بإضافة أي سورة إلى المفضلة بعد.\nاضغط على أيقونة النجمة ⭐ بجوار أي سورة لحفظها هنا والوصول إليها بسرعة.'
                : 'لم يتم العثور على أي سورة مطابقة'}
            </p>
          </div>
        ) : (
          <div className="mt-2.5 grid grid-cols-1 md:grid-cols-2 gap-2.5">
            {filteredSurahs.map((surah) => {
              const isCurrent = surah.number === playerState.currentSurah.number;
              const isFav = favoriteSurahs.has(surah.number);

              return (
                <SurahCard
                  key={surah.number}
                  surah={surah}
                  isCurrent={isCurrent}
                  isPlaying={isCurrent && playerState.isPlaying}
                  isFavorite={isFav}
                  onToggleFavorite={() =>
                    favoritesManager.toggleSurahFavorite(surah.number)
                  }
                  onClick={() => {
                    if (isCurrent) {
                      togglePlayPause();
                    } else {
                      playSurah(surah);
                    }
                  }}
                />
              );
            })}
          </div>
        )}
      </main>

      {/* Fixed Bottom MiniPlayer + Ad Banner */}
      <div
        className="fixed bottom-0 left-0 right-0 z-30 border-t"
        style={{
          backgroundColor: 'var(--islamic-bg)',
          borderColor: 'var(--islamic-divider)',
        }}
      >
        <div className="w-full max-w-4xl mx-auto">
          <MiniPlayer
            state={playerState}
            onTogglePlayPause={togglePlayPause}
            onPlayNext={playNext}
            onPlayPrevious={playPrevious}
            onClick={() => setPlayerExpanded(true)}
            onRetry={retry}
            isTimerActive={timerState.isTimerActive}
            remainingSeconds={timerState.remainingSeconds}
            onTimerClick={() => setShowSleepTimerSheet(true)}
          />
          <AdBannerView />
        </div>
      </div>

      {/* Full Player Modal Sheet */}
      {playerState.isPlayerExpanded && (
        <FullPlayerSheet
          state={playerState}
          onDismiss={() => setPlayerExpanded(false)}
          onTogglePlayPause={togglePlayPause}
          onSeekTo={seekTo}
          onSkipForward={() => skipForward(10)}
          onSkipBackward={() => skipBackward(10)}
          onPlayNext={playNext}
          onPlayPrevious={playPrevious}
          onToggleSpeed={() => {
            const nextSpeed =
              playerState.playbackSpeed === 0.75
                ? 1.0
                : playerState.playbackSpeed === 1.0
                  ? 1.25
                  : playerState.playbackSpeed === 1.25
                    ? 1.5
                    : 0.75;
            setPlaybackSpeed(nextSpeed);
          }}
          onToggleRepeat={toggleRepeatOne}
          onOpenReciterSelector={() => setShowReciterSheet(true)}
          onRetry={retry}
          isTimerActive={timerState.isTimerActive}
          remainingSeconds={timerState.remainingSeconds}
          onOpenSleepTimer={() => setShowSleepTimerSheet(true)}
          isFavorite={favoriteSurahs.has(playerState.currentSurah.number)}
          onToggleFavorite={() =>
            favoritesManager.toggleSurahFavorite(
              playerState.currentSurah.number,
            )
          }
        />
      )}

      {/* Reciter Selection Sheet */}
      {showReciterSheet && (
        <ReciterSelectionSheet
          selectedReciter={playerState.currentReciter}
          onSelectReciter={setReciter}
          onDismiss={() => setShowReciterSheet(false)}
        />
      )}

      {/* Sleep Timer Sheet */}
      {showSleepTimerSheet && (
        <SleepTimerBottomSheet
          isTimerActive={timerState.isTimerActive}
          remainingSeconds={timerState.remainingSeconds}
          onStartTimerMinutes={(minutes) =>
            sleepTimerManager.startTimerMinutes(minutes, playerState.isPlaying)
          }
          onStartTimerSeconds={(seconds) =>
            sleepTimerManager.startTimerWithSeconds(
              seconds,
              playerState.isPlaying,
            )
          }
          onCancelTimer={() => sleepTimerManager.cancelTimer()}
          onDismiss={() => setShowSleepTimerSheet(false)}
        />
      )}

      {/* Theme Settings Sheet */}
      {showThemeSheet && (
        <ThemeSettingsBottomSheet
          currentMode={themeMode}
          onSelectMode={(mode) => {
            themeManager.setThemeMode(mode);
            setShowThemeSheet(false);
          }}
          onDismiss={() => setShowThemeSheet(false)}
        />
      )}

      {/* Developer Dashboard Dialog */}
      {showDeveloperDashboard && (
        <DeveloperDashboardDialog
          onDismiss={() => setShowDeveloperDashboard(false)}
          onShowToast={(msg) => setToastMessage(msg)}
        />
      )}
    </div>
  );
}
