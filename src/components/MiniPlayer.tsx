import {
  AlertTriangle,
  Loader2,
  MoonStar,
  Pause,
  Play,
  RefreshCw,
  SkipBack,
  SkipForward,
} from 'lucide-react';
import { formatTimerSeconds } from '../services/sleepTimerManager';
import type { AudioPlayerState } from '../types/quran';
import { IslamicStarBadge } from './IslamicStarBadge';

interface MiniPlayerProps {
  state: AudioPlayerState;
  onTogglePlayPause: () => void;
  onPlayNext: () => void;
  onPlayPrevious: () => void;
  onClick: () => void;
  onRetry: () => void;
  isTimerActive: boolean;
  remainingSeconds: number;
  onTimerClick: () => void;
}

export function MiniPlayer({
  state,
  onTogglePlayPause,
  onPlayNext,
  onPlayPrevious,
  onClick,
  onRetry,
  isTimerActive,
  remainingSeconds,
  onTimerClick,
}: MiniPlayerProps) {
  const progressPct =
    state.durationMs > 0
      ? Math.min(100, Math.max(0, (state.currentPositionMs / state.durationMs) * 100))
      : 0;

  let statusText = 'متوقف مؤقتاً';
  let statusBg = 'var(--islamic-surface-variant)';
  let statusColor = 'var(--islamic-text-muted)';

  if (state.errorMessage) {
    statusText = 'خطأ';
    statusBg = 'color-mix(in srgb, var(--islamic-red) 20%, transparent)';
    statusColor = 'var(--islamic-red)';
  } else if (state.isBuffering) {
    statusText = 'جاري التحميل...';
    statusBg = 'color-mix(in srgb, var(--islamic-gold) 20%, transparent)';
    statusColor = 'var(--islamic-gold)';
  } else if (state.isPlaying) {
    statusText = 'يتلو الآن';
    statusBg =
      'color-mix(in srgb, var(--islamic-emerald-light) 20%, transparent)';
    statusColor = 'var(--islamic-emerald-light)';
  }

  return (
    <div className="w-full px-3 py-1">
      {state.errorMessage && (
        <div
          data-testid="playback_error_banner"
          className="mb-1.5 w-full rounded-[10px] border px-3 py-1.5 flex items-center justify-between gap-2"
          style={{
            backgroundColor:
              'color-mix(in srgb, var(--islamic-red) 18%, transparent)',
            borderColor: 'var(--islamic-red)',
          }}
        >
          <div className="flex items-center gap-2 min-w-0 flex-1">
            <AlertTriangle
              size={16}
              className="shrink-0"
              style={{ color: 'var(--islamic-red)' }}
            />
            <span className="font-cairo text-[11px] truncate text-[#FFCDD2]">
              {state.errorMessage}
            </span>
          </div>
          <button
            type="button"
            onClick={onRetry}
            className="flex items-center gap-1 px-2 py-1 rounded-[6px] shrink-0 cursor-pointer"
            style={{
              backgroundColor:
                'color-mix(in srgb, var(--islamic-gold) 20%, transparent)',
              color: 'var(--islamic-gold)',
            }}
          >
            <RefreshCw size={13} />
            <span className="font-cairo text-[10px] font-bold">
              إعادة المحاولة
            </span>
          </button>
        </div>
      )}

      <div
        role="button"
        tabIndex={0}
        onClick={onClick}
        onKeyDown={(e) => {
          if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            onClick();
          }
        }}
        data-testid="mini_player"
        className="w-full rounded-[16px] border overflow-hidden cursor-pointer transition-colors"
        style={{
          backgroundColor: 'var(--islamic-surface)',
          borderColor: 'var(--islamic-border)',
        }}
      >
        {/* Top continuous progress bar */}
        <div
          className="w-full h-[3px]"
          style={{ backgroundColor: 'var(--islamic-surface-variant)' }}
        >
          <div
            className="h-full transition-[width] duration-200"
            style={{
              width: `${progressPct}%`,
              backgroundColor: 'var(--islamic-gold)',
            }}
          />
        </div>

        <div className="px-3.5 py-2.5 flex items-center justify-between gap-2">
          <div className="flex items-center gap-3 min-w-0 flex-1">
            <IslamicStarBadge
              number={state.currentSurah.number}
              isCurrent
              size={38}
            />

            <div className="flex flex-col gap-0.5 min-w-0 flex-1">
              <div className="flex items-center gap-1.5 flex-wrap">
                <span
                  className="font-amiri font-bold text-[17px] leading-tight truncate"
                  style={{ color: 'var(--islamic-gold-light)' }}
                >
                  سورة {state.currentSurah.nameArabic}
                </span>

                <span
                  className="font-cairo text-[9px] font-bold px-1.5 py-0.5 rounded-[4px] whitespace-nowrap shrink-0"
                  style={{
                    backgroundColor: statusBg,
                    color: statusColor,
                  }}
                >
                  {statusText}
                </span>

                {isTimerActive && (
                  <button
                    type="button"
                    onClick={(e) => {
                      e.stopPropagation();
                      onTimerClick();
                    }}
                    data-testid="mini_player_timer_badge"
                    className="flex items-center gap-1 px-1.5 py-0.5 rounded-[4px] border whitespace-nowrap shrink-0 cursor-pointer"
                    style={{
                      backgroundColor:
                        'color-mix(in srgb, var(--islamic-gold) 20%, transparent)',
                      borderColor:
                        'color-mix(in srgb, var(--islamic-gold) 50%, transparent)',
                      color: 'var(--islamic-gold)',
                    }}
                  >
                    <MoonStar size={10} />
                    <span className="font-cairo text-[9px] font-bold tabular-nums">
                      {formatTimerSeconds(remainingSeconds)}
                    </span>
                  </button>
                )}
              </div>

              <span
                className="font-cairo text-xs truncate"
                style={{ color: 'var(--islamic-text-secondary)' }}
              >
                {state.currentReciter.nameArabic} •{' '}
                {state.currentSurah.revelationTypeArabic}
              </span>
            </div>
          </div>

          <div
            className="flex items-center gap-1.5 shrink-0"
            onClick={(e) => e.stopPropagation()}
          >
            <button
              type="button"
              onClick={onPlayPrevious}
              aria-label="السورة السابقة"
              data-testid="mini_player_previous"
              className="w-9 h-9 rounded-full flex items-center justify-center cursor-pointer hover:opacity-80"
              style={{ color: 'var(--islamic-text-secondary)' }}
            >
              <SkipForward size={20} />
            </button>

            <button
              type="button"
              onClick={onTogglePlayPause}
              aria-label={
                state.isPlaying ? 'إيقاف مؤقت' : 'استئناف التلاوة'
              }
              data-testid="mini_player_play_pause"
              className="w-[42px] h-[42px] rounded-full flex items-center justify-center cursor-pointer transition-transform active:scale-95"
              style={{
                backgroundColor: 'var(--islamic-gold)',
                color: 'var(--islamic-on-gold)',
              }}
            >
              {state.isBuffering ? (
                <Loader2 size={20} className="animate-spin" />
              ) : state.isPlaying ? (
                <Pause size={22} />
              ) : (
                <Play size={22} />
              )}
            </button>

            <button
              type="button"
              onClick={onPlayNext}
              aria-label="السورة التالية"
              data-testid="mini_player_next"
              className="w-9 h-9 rounded-full flex items-center justify-center cursor-pointer hover:opacity-80"
              style={{ color: 'var(--islamic-text-secondary)' }}
            >
              <SkipBack size={20} />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
