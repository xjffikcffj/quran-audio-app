export interface Surah {
  number: number;
  nameArabic: string;
  versesCount: number;
  audioUrl: string;
  nameEnglish: string;
  isMeccan: boolean;
  revelationTypeArabic: string;
  paddedNumber: string;
}

export interface Reciter {
  id: string;
  nameArabic: string;
  riwayah: string;
  serverBaseUrl: string;
}

export interface AudioBookmark {
  id: string;
  surahNumber: number;
  positionMs: number;
  name: string;
  createdAt: number;
}

export enum PlaybackStatus {
  IDLE = 'IDLE',
  BUFFERING = 'BUFFERING',
  PLAYING = 'PLAYING',
  PAUSED = 'PAUSED',
  ERROR = 'ERROR',
}

export interface AudioPlayerState {
  currentSurah: Surah;
  currentReciter: Reciter;
  playbackStatus: PlaybackStatus;
  isPlaying: boolean;
  isBuffering: boolean;
  currentPositionMs: number;
  durationMs: number;
  playbackSpeed: number;
  isRepeatOne: boolean;
  errorMessage: string | null;
  isPlayerExpanded: boolean;
}

export enum SurahFilter {
  ALL = 'ALL',
  FAVORITES = 'FAVORITES',
  MECCAN = 'MECCAN',
  MEDINAN = 'MEDINAN',
}

export const SURAH_FILTER_LABELS: Record<SurahFilter, string> = {
  [SurahFilter.ALL]: 'جميع السور (114)',
  [SurahFilter.FAVORITES]: 'المفضلة ⭐',
  [SurahFilter.MECCAN]: 'مكية (86)',
  [SurahFilter.MEDINAN]: 'مدنية (28)',
};

export enum AppThemeMode {
  DARK = 'DARK',
  PURE_NIGHT = 'PURE_NIGHT',
  LIGHT = 'LIGHT',
}

export const THEME_MODE_META: Record<
  AppThemeMode,
  { titleArabic: string; descriptionArabic: string }
> = {
  [AppThemeMode.DARK]: {
    titleArabic: 'الوضع الداكن الكلاسيكي',
    descriptionArabic: 'ألوان زمردية وذهبية فاخرة متوازنة',
  },
  [AppThemeMode.PURE_NIGHT]: {
    titleArabic: 'الوضع الليلي الكامل (تعتيم فائق)',
    descriptionArabic:
      'أسود خالص موفر للطاقة ومريح للعين عند الاستماع في الظلام',
  },
  [AppThemeMode.LIGHT]: {
    titleArabic: 'الوضع الفاتح الأنيق',
    descriptionArabic: 'واجهة ناصعة بلمسات إسلامية مشرقة وواضحة',
  },
};

export function getReciterAudioUrlForSurah(
  reciter: Reciter,
  surahNumber: number,
): string {
  const padded = String(surahNumber).padStart(3, '0');
  const base = reciter.serverBaseUrl.endsWith('/')
    ? reciter.serverBaseUrl
    : `${reciter.serverBaseUrl}/`;
  return `${base}${padded}.mp3`;
}
