import { Pause, Play, Star } from 'lucide-react';
import type { Surah } from '../types/quran';
import { IslamicStarBadge, PlayingSoundWave } from './IslamicStarBadge';

interface SurahCardProps {
  surah: Surah;
  isCurrent: boolean;
  isPlaying: boolean;
  isFavorite: boolean;
  onToggleFavorite: () => void;
  onClick: () => void;
}

export function SurahCard({
  surah,
  isCurrent,
  isPlaying,
  isFavorite,
  onToggleFavorite,
  onClick,
}: SurahCardProps) {
  return (
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
      data-testid={`surah_card_${surah.number}`}
      className="w-full rounded-[14px] border px-3.5 py-3 flex items-center justify-between gap-3 cursor-pointer transition-colors duration-150 focus-visible:outline-2 focus-visible:outline-offset-2"
      style={{
        backgroundColor: isCurrent
          ? 'var(--islamic-card-hover)'
          : 'var(--islamic-card)',
        borderColor: isCurrent
          ? 'var(--islamic-gold)'
          : 'var(--islamic-border)',
        boxShadow: isCurrent
          ? '0 4px 12px rgba(0,0,0,0.22)'
          : '0 1px 3px rgba(0,0,0,0.12)',
      }}
    >
      <div className="flex items-center gap-3.5 min-w-0 flex-1">
        <IslamicStarBadge number={surah.number} isCurrent={isCurrent} />

        <div className="flex flex-col gap-0.5 min-w-0">
          <div className="flex items-center gap-2 flex-wrap">
            <span
              className="font-amiri font-bold text-xl leading-snug"
              style={{
                color: isCurrent
                  ? 'var(--islamic-gold)'
                  : 'var(--islamic-text-primary)',
              }}
            >
              سورة {surah.nameArabic}
            </span>

            <span
              className="font-cairo text-[10px] font-medium px-1.5 py-0.5 rounded-[4px] whitespace-nowrap shrink-0"
              style={{
                backgroundColor: surah.isMeccan
                  ? 'var(--islamic-meccan-chip-bg)'
                  : 'var(--islamic-medinan-chip-bg)',
                color: surah.isMeccan
                  ? 'var(--islamic-meccan-chip-text)'
                  : 'var(--islamic-medinan-chip-text)',
              }}
            >
              {surah.revelationTypeArabic}
            </span>
          </div>

          <div className="flex items-center gap-2 text-xs font-cairo">
            <span
              className="tabular-nums"
              style={{ color: 'var(--islamic-text-secondary)' }}
            >
              {surah.versesCount} آية
            </span>
            <span aria-hidden="true" style={{ color: 'var(--islamic-text-muted)' }}>
              •
            </span>
            <span
              className="truncate"
              style={{ color: 'var(--islamic-text-muted)' }}
            >
              {surah.nameEnglish}
            </span>
          </div>
        </div>
      </div>

      <div className="flex items-center gap-1 shrink-0">
        <button
          type="button"
          onClick={(e) => {
            e.stopPropagation();
            onToggleFavorite();
          }}
          aria-label={isFavorite ? 'إزالة من المفضلة' : 'إضافة للمفضلة'}
          data-testid={`favorite_star_${surah.number}`}
          className="w-11 h-11 rounded-full flex items-center justify-center transition-transform active:scale-95 cursor-pointer"
        >
          <Star
            size={22}
            fill={isFavorite ? 'var(--islamic-gold)' : 'none'}
            style={{
              color: isFavorite
                ? 'var(--islamic-gold)'
                : 'var(--islamic-text-muted)',
            }}
          />
        </button>

        {isCurrent && isPlaying ? (
          <PlayingSoundWave />
        ) : (
          <div
            className="w-9 h-9 rounded-full border flex items-center justify-center shrink-0"
            style={{
              backgroundColor: isCurrent
                ? 'color-mix(in srgb, var(--islamic-gold) 20%, transparent)'
                : 'transparent',
              borderColor: isCurrent
                ? 'var(--islamic-gold)'
                : 'var(--islamic-border)',
            }}
          >
            {isCurrent && isPlaying ? (
              <Pause
                size={18}
                style={{
                  color: isCurrent
                    ? 'var(--islamic-gold)'
                    : 'var(--islamic-text-secondary)',
                }}
              />
            ) : (
              <Play
                size={18}
                style={{
                  color: isCurrent
                    ? 'var(--islamic-gold)'
                    : 'var(--islamic-text-secondary)',
                }}
              />
            )}
          </div>
        )}
      </div>
    </div>
  );
}
