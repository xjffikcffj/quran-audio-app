import { useState } from 'react';
import {
  ChevronDown,
  Gauge,
  Loader2,
  MoonStar,
  Pause,
  Play,
  RefreshCw,
  Repeat,
  Repeat1,
  RotateCcw,
  RotateCw,
  SkipBack,
  SkipForward,
  Star,
  User,
} from 'lucide-react';
import { formatTimerSeconds } from '../services/sleepTimerManager';
import type { AudioPlayerState } from '../types/quran';

interface FullPlayerSheetProps {
  state: AudioPlayerState;
  onDismiss: () => void;
  onTogglePlayPause: () => void;
  onSeekTo: (ms: number) => void;
  onSkipForward: () => void;
  onSkipBackward: () => void;
  onPlayNext: () => void;
  onPlayPrevious: () => void;
  onToggleSpeed: () => void;
  onToggleRepeat: () => void;
  onOpenReciterSelector: () => void;
  onRetry: () => void;
  isTimerActive: boolean;
  remainingSeconds: number;
  onOpenSleepTimer: () => void;
  isFavorite: boolean;
  onToggleFavorite: () => void;
}

function formatMs(ms: number): string {
  const totalSeconds = Math.max(0, Math.floor(ms / 1000));
  const minutes = Math.floor(totalSeconds / 60);
  const seconds = totalSeconds % 60;
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
}

export function FullPlayerSheet({
  state,
  onDismiss,
  onTogglePlayPause,
  onSeekTo,
  onSkipForward,
  onSkipBackward,
  onPlayNext,
  onPlayPrevious,
  onToggleSpeed,
  onToggleRepeat,
  onOpenReciterSelector,
  onRetry,
  isTimerActive,
  remainingSeconds,
  onOpenSleepTimer,
  isFavorite,
  onToggleFavorite,
}: FullPlayerSheetProps) {
  const [isSeeking, setIsSeeking] = useState(false);
  const [seekRatio, setSeekRatio] = useState(0);

  const sliderRatio = isSeeking
    ? seekRatio
    : state.durationMs > 0
      ? Math.min(1, Math.max(0, state.currentPositionMs / state.durationMs))
      : 0;

  const displayMs =
    isSeeking && state.durationMs > 0
      ? Math.floor(seekRatio * state.durationMs)
      : state.currentPositionMs;

  let statusText = 'متوقف مؤقتاً (اضغط للاستئناف)';
  let statusBg = 'var(--islamic-surface-variant)';
  let statusColor = 'var(--islamic-text-muted)';

  if (state.errorMessage) {
    statusText = 'تعذر الاتصال بالشبكة';
    statusBg = 'color-mix(in srgb, var(--islamic-red) 20%, transparent)';
    statusColor = 'var(--islamic-red)';
  } else if (state.isBuffering) {
    statusText = 'جاري تجهيز وبث التلاوة…';
    statusBg = 'color-mix(in srgb, var(--islamic-gold) 20%, transparent)';
    statusColor = 'var(--islamic-gold)';
  } else if (state.isPlaying) {
    statusText = 'تلاوة جارية الآن';
    statusBg =
      'color-mix(in srgb, var(--islamic-emerald-light) 20%, transparent)';
    statusColor = 'var(--islamic-emerald-light)';
  }

  return (
    <div
      className="fixed inset-0 z-50 flex items-end sm:items-center justify-center bg-black/65 backdrop-blur-sm"
      onClick={onDismiss}
      data-testid="full_player_sheet"
    >
      <div
        className="w-full sm:max-w-lg max-h-[94vh] overflow-y-auto rounded-t-3xl sm:rounded-3xl border px-6 pb-9 pt-3 shadow-2xl"
        style={{
          backgroundColor: 'var(--islamic-bg)',
          borderColor: 'var(--islamic-border)',
        }}
        onClick={(e) => e.stopPropagation()}
      >
        {/* Drag handle */}
        <div
          className="w-11 h-1 rounded-full mx-auto mb-3"
          style={{ backgroundColor: 'var(--islamic-border)' }}
        />

        {/* Top Bar */}
        <div className="flex items-center justify-between gap-2">
          <button
            type="button"
            onClick={onDismiss}
            aria-label="تصغير المشغل"
            data-testid="player_collapse_button"
            className="w-11 h-11 rounded-full flex items-center justify-center cursor-pointer"
            style={{ color: 'var(--islamic-gold-light)' }}
          >
            <ChevronDown size={28} />
          </button>

          <h2
            className="font-cairo font-bold text-[17px]"
            style={{ color: 'var(--islamic-gold-light)' }}
          >
            مشغل التلاوة
          </h2>

          <button
            type="button"
            onClick={onOpenReciterSelector}
            data-testid="player_reciter_pill"
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-full border cursor-pointer whitespace-nowrap shrink-0"
            style={{
              backgroundColor: 'var(--islamic-card-hover)',
              borderColor: 'var(--islamic-border)',
              color: 'var(--islamic-text-primary)',
            }}
          >
            <User size={15} style={{ color: 'var(--islamic-gold)' }} />
            <span className="font-cairo text-xs font-medium">القارئ</span>
          </button>
        </div>

        {/* Artwork Card */}
        <div
          className="relative w-full h-[220px] mt-4 rounded-3xl overflow-hidden border-2"
          style={{ borderColor: 'var(--islamic-border)' }}
        >
          <img
            src="/images/quran_hero_banner.jpg"
            alt="المصحف الشريف"
            referrerPolicy="no-referrer"
            className="w-full h-full object-cover"
          />
          <div
            className="absolute inset-0"
            style={{
              background:
                'linear-gradient(to bottom, transparent 25%, rgba(9,22,18,0.88) 100%)',
            }}
          />
          <div
            className="absolute top-3.5 left-3.5 px-2.5 py-1 rounded-lg border text-[11px] font-cairo font-medium"
            style={{
              backgroundColor:
                'color-mix(in srgb, var(--islamic-surface) 88%, transparent)',
              borderColor:
                'color-mix(in srgb, var(--islamic-gold) 50%, transparent)',
              color: 'var(--islamic-gold-light)',
            }}
          >
            {state.currentSurah.revelationTypeArabic} •{' '}
            {state.currentSurah.versesCount} آيات
          </div>
        </div>

        {/* Surah Title & Favorite */}
        <div className="mt-5 flex items-center justify-center gap-2">
          <h3
            className="font-amiri font-bold text-[32px] leading-tight text-center"
            style={{ color: 'var(--islamic-gold-light)' }}
          >
            سورة {state.currentSurah.nameArabic}
          </h3>
          <button
            type="button"
            onClick={onToggleFavorite}
            aria-label={isFavorite ? 'إزالة من المفضلة' : 'إضافة للمفضلة'}
            data-testid="full_player_favorite_button"
            className="w-11 h-11 rounded-full flex items-center justify-center cursor-pointer"
          >
            <Star
              size={26}
              fill={isFavorite ? 'var(--islamic-gold)' : 'none'}
              style={{
                color: isFavorite
                  ? 'var(--islamic-gold)'
                  : 'var(--islamic-text-muted)',
              }}
            />
          </button>
        </div>

        <p
          className="font-cairo text-sm text-center mt-1"
          style={{ color: 'var(--islamic-text-secondary)' }}
        >
          {state.currentReciter.nameArabic} ({state.currentReciter.riwayah})
        </p>

        {/* Playback Status */}
        <div className="flex justify-center mt-2.5">
          <span
            className="font-cairo text-[11px] font-bold px-3 py-1 rounded-lg border"
            style={{
              backgroundColor: statusBg,
              borderColor: `color-mix(in srgb, ${statusColor} 40%, transparent)`,
              color: statusColor,
            }}
          >
            {statusText}
          </span>
        </div>

        {/* Error banner */}
        {state.errorMessage && (
          <div
            className="mt-3 w-full rounded-lg border p-2.5 flex items-center justify-between gap-2"
            style={{
              backgroundColor:
                'color-mix(in srgb, var(--islamic-red) 15%, transparent)',
              borderColor: 'var(--islamic-red)',
            }}
          >
            <span className="font-cairo text-xs text-[#FFCDD2]">
              {state.errorMessage}
            </span>
            <button
              type="button"
              onClick={onRetry}
              aria-label="إعادة المحاولة"
              data-testid="retry_button"
              className="w-8 h-8 rounded-full flex items-center justify-center shrink-0 cursor-pointer"
              style={{ color: 'var(--islamic-gold)' }}
            >
              <RefreshCw size={18} />
            </button>
          </div>
        )}

        {/* Timeline Slider */}
        <div className="mt-5">
          <input
            type="range"
            min={0}
            max={1000}
            value={Math.round(sliderRatio * 1000)}
            aria-label="شريط تقدم التلاوة"
            data-testid="player_progress_slider"
            dir="ltr"
            onChange={(e) => {
              setIsSeeking(true);
              setSeekRatio(Number(e.target.value) / 1000);
            }}
            onMouseUp={(e) => {
              const ratio = Number(
                (e.target as HTMLInputElement).value,
              ) / 1000;
              if (state.durationMs > 0) {
                onSeekTo(Math.floor(ratio * state.durationMs));
              }
              setIsSeeking(false);
            }}
            onTouchEnd={(e) => {
              const ratio = Number(
                (e.target as HTMLInputElement).value,
              ) / 1000;
              if (state.durationMs > 0) {
                onSeekTo(Math.floor(ratio * state.durationMs));
              }
              setIsSeeking(false);
            }}
            className="quran-slider"
            style={{
              background: `linear-gradient(to right, var(--islamic-gold) ${sliderRatio * 100}%, var(--islamic-surface-variant) ${sliderRatio * 100}%)`,
            }}
          />

          <div
            className="flex items-center justify-between mt-1.5 text-xs font-cairo tabular-nums"
            dir="ltr"
          >
            <span style={{ color: 'var(--islamic-text-secondary)' }}>
              {formatMs(displayMs)}
            </span>
            <span style={{ color: 'var(--islamic-text-muted)' }}>
              {formatMs(state.durationMs)}
            </span>
          </div>
        </div>

        {/* Main Playback Controls */}
        <div className="mt-5 flex items-center justify-evenly">
          <button
            type="button"
            onClick={onPlayPrevious}
            aria-label="السورة السابقة"
            data-testid="player_previous_button"
            className="w-11 h-11 rounded-full flex items-center justify-center cursor-pointer"
            style={{ color: 'var(--islamic-text-primary)' }}
          >
            <SkipForward size={26} />
          </button>

          <button
            type="button"
            onClick={onSkipBackward}
            aria-label="تأخير 10 ثوان"
            data-testid="player_rewind_button"
            className="w-11 h-11 rounded-full flex flex-col items-center justify-center cursor-pointer relative"
            style={{ color: 'var(--islamic-gold-light)' }}
          >
            <RotateCcw size={24} />
            <span className="text-[9px] font-bold absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 mt-[1px]">
              10
            </span>
          </button>

          <button
            type="button"
            onClick={onTogglePlayPause}
            aria-label={state.isPlaying ? 'إيقاف مؤقت' : 'تشغيل'}
            data-testid="player_play_pause_button"
            className="w-[72px] h-[72px] rounded-full flex items-center justify-center cursor-pointer shadow-lg transition-transform active:scale-95"
            style={{
              background:
                'linear-gradient(135deg, var(--islamic-gold-light), var(--islamic-gold), var(--islamic-gold-dark))',
              color: 'var(--islamic-on-gold)',
            }}
          >
            {state.isBuffering ? (
              <Loader2 size={32} className="animate-spin" />
            ) : state.isPlaying ? (
              <Pause size={34} />
            ) : (
              <Play size={34} />
            )}
          </button>

          <button
            type="button"
            onClick={onSkipForward}
            aria-label="تقديم 10 ثوان"
            data-testid="player_forward_button"
            className="w-11 h-11 rounded-full flex flex-col items-center justify-center cursor-pointer relative"
            style={{ color: 'var(--islamic-gold-light)' }}
          >
            <RotateCw size={24} />
            <span className="text-[9px] font-bold absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 mt-[1px]">
              10
            </span>
          </button>

          <button
            type="button"
            onClick={onPlayNext}
            aria-label="السورة التالية"
            data-testid="player_next_button"
            className="w-11 h-11 rounded-full flex items-center justify-center cursor-pointer"
            style={{ color: 'var(--islamic-text-primary)' }}
          >
            <SkipBack size={26} />
          </button>
        </div>

        {/* Extra Controls: Speed, Repeat, Sleep Timer */}
        <div className="mt-6 flex items-center justify-center gap-3 flex-wrap">
          <button
            type="button"
            onClick={onToggleSpeed}
            data-testid="player_speed_toggle"
            className="flex items-center gap-1.5 px-3.5 py-2 rounded-xl border cursor-pointer whitespace-nowrap"
            style={{
              backgroundColor: 'var(--islamic-card)',
              borderColor: 'var(--islamic-border)',
              color: 'var(--islamic-text-primary)',
            }}
          >
            <Gauge size={17} style={{ color: 'var(--islamic-gold)' }} />
            <span className="font-cairo text-[13px] font-bold tabular-nums">
              {state.playbackSpeed}x
            </span>
          </button>

          <button
            type="button"
            onClick={onToggleRepeat}
            data-testid="player_repeat_toggle"
            className="flex items-center gap-1.5 px-3.5 py-2 rounded-xl border cursor-pointer whitespace-nowrap"
            style={{
              backgroundColor: state.isRepeatOne
                ? 'var(--islamic-card-hover)'
                : 'var(--islamic-card)',
              borderColor: state.isRepeatOne
                ? 'var(--islamic-gold)'
                : 'var(--islamic-border)',
              color: state.isRepeatOne
                ? 'var(--islamic-gold-light)'
                : 'var(--islamic-text-secondary)',
            }}
          >
            {state.isRepeatOne ? (
              <Repeat1 size={17} style={{ color: 'var(--islamic-gold)' }} />
            ) : (
              <Repeat
                size={17}
                style={{ color: 'var(--islamic-text-secondary)' }}
              />
            )}
            <span className="font-cairo text-[13px] font-medium">
              {state.isRepeatOne ? 'تكرار السورة' : 'تتابع تلقائي'}
            </span>
          </button>

          <button
            type="button"
            onClick={onOpenSleepTimer}
            data-testid="player_sleep_timer_button"
            className="flex items-center gap-1.5 px-3.5 py-2 rounded-xl border cursor-pointer whitespace-nowrap"
            style={{
              backgroundColor: isTimerActive
                ? 'var(--islamic-card-hover)'
                : 'var(--islamic-card)',
              borderColor: isTimerActive
                ? 'var(--islamic-gold)'
                : 'var(--islamic-border)',
              color: isTimerActive
                ? 'var(--islamic-gold-light)'
                : 'var(--islamic-text-secondary)',
            }}
          >
            <MoonStar
              size={17}
              style={{
                color: isTimerActive
                  ? 'var(--islamic-gold)'
                  : 'var(--islamic-text-secondary)',
              }}
            />
            <span className="font-cairo text-[13px] font-medium tabular-nums">
              {isTimerActive
                ? formatTimerSeconds(remainingSeconds)
                : 'المؤقت'}
            </span>
          </button>
        </div>
      </div>
    </div>
  );
}
