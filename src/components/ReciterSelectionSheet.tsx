import { useEffect, useState } from 'react';
import { Check, Star, User, X } from 'lucide-react';
import { developerContentManager } from '../services/developerContentManager';
import { favoritesManager } from '../services/favoritesManager';
import type { Reciter } from '../types/quran';

interface ReciterSelectionSheetProps {
  selectedReciter: Reciter;
  onSelectReciter: (reciter: Reciter) => void;
  onDismiss: () => void;
}

export function ReciterSelectionSheet({
  selectedReciter,
  onSelectReciter,
  onDismiss,
}: ReciterSelectionSheetProps) {
  const [contentState, setContentState] = useState(() =>
    developerContentManager.getState(),
  );
  const [favoriteReciters, setFavoriteReciters] = useState<Set<string>>(() =>
    favoritesManager.getFavoriteReciters(),
  );

  useEffect(() => {
    const unsub1 = developerContentManager.subscribe(() => {
      setContentState(developerContentManager.getState());
    });
    const unsub2 = favoritesManager.subscribe(() => {
      setFavoriteReciters(favoritesManager.getFavoriteReciters());
    });
    return () => {
      unsub1();
      unsub2();
    };
  }, []);

  const visibleReciters = contentState.recitersList.filter(
    (r) => !contentState.hiddenReciterIds.has(r.id),
  );

  return (
    <div
      className="fixed inset-0 z-50 flex items-end sm:items-center justify-center bg-black/65 backdrop-blur-sm"
      onClick={onDismiss}
    >
      <div
        data-testid="reciter_selection_sheet"
        className="w-full sm:max-w-md max-h-[85vh] flex flex-col rounded-t-3xl sm:rounded-3xl border px-5 pb-8 pt-3 shadow-2xl"
        style={{
          backgroundColor: 'var(--islamic-bg)',
          borderColor: 'var(--islamic-border)',
        }}
        onClick={(e) => e.stopPropagation()}
      >
        <div
          className="w-10 h-1 rounded-full mx-auto mb-3 shrink-0"
          style={{ backgroundColor: 'var(--islamic-border)' }}
        />

        <div className="flex items-center justify-between">
          <h2
            className="font-cairo font-bold text-xl"
            style={{ color: 'var(--islamic-gold-light)' }}
          >
            اختيار القارئ
          </h2>
          <button
            type="button"
            onClick={onDismiss}
            aria-label="إغلاق"
            data-testid="close_reciter_sheet"
            className="w-10 h-10 rounded-full flex items-center justify-center cursor-pointer"
            style={{ color: 'var(--islamic-text-secondary)' }}
          >
            <X size={20} />
          </button>
        </div>

        <p
          className="font-cairo text-[13px] mb-4"
          style={{ color: 'var(--islamic-text-secondary)' }}
        >
          اختر القارئ المفضل للاستماع إلى تلاوة القرآن الكريم
        </p>

        <div className="flex-1 overflow-y-auto flex flex-col gap-2.5 pr-0.5">
          {visibleReciters.map((reciter) => {
            const isSelected = reciter.id === selectedReciter.id;
            const isFav = favoriteReciters.has(reciter.id);

            return (
              <div
                key={reciter.id}
                role="button"
                tabIndex={0}
                onClick={() => {
                  onSelectReciter(reciter);
                  onDismiss();
                }}
                onKeyDown={(e) => {
                  if (e.key === 'Enter' || e.key === ' ') {
                    e.preventDefault();
                    onSelectReciter(reciter);
                    onDismiss();
                  }
                }}
                data-testid={`reciter_item_${reciter.id}`}
                className="w-full rounded-xl border p-3.5 flex items-center justify-between gap-3 cursor-pointer transition-colors"
                style={{
                  backgroundColor: isSelected
                    ? 'var(--islamic-card-hover)'
                    : 'var(--islamic-card)',
                  borderColor: isSelected
                    ? 'var(--islamic-gold)'
                    : 'var(--islamic-border)',
                }}
              >
                <div className="flex items-center gap-3.5 min-w-0 flex-1">
                  <div
                    className="w-10 h-10 rounded-full border flex items-center justify-center shrink-0"
                    style={{
                      backgroundColor: isSelected
                        ? 'color-mix(in srgb, var(--islamic-gold) 20%, transparent)'
                        : 'var(--islamic-surface)',
                      borderColor: isSelected
                        ? 'var(--islamic-gold)'
                        : 'var(--islamic-border)',
                    }}
                  >
                    <User
                      size={20}
                      style={{
                        color: isSelected
                          ? 'var(--islamic-gold)'
                          : 'var(--islamic-text-secondary)',
                      }}
                    />
                  </div>

                  <div className="min-w-0">
                    <div
                      className="font-cairo font-bold text-base truncate"
                      style={{
                        color: isSelected
                          ? 'var(--islamic-gold-light)'
                          : 'var(--islamic-text-primary)',
                      }}
                    >
                      {reciter.nameArabic}
                    </div>
                    <div
                      className="font-cairo text-xs truncate"
                      style={{ color: 'var(--islamic-text-muted)' }}
                    >
                      {reciter.riwayah}
                    </div>
                  </div>
                </div>

                <div className="flex items-center gap-1.5 shrink-0">
                  <button
                    type="button"
                    onClick={(e) => {
                      e.stopPropagation();
                      favoritesManager.toggleReciterFavorite(reciter.id);
                    }}
                    aria-label={
                      isFav
                        ? 'إزالة من القراء المفضلين'
                        : 'إضافة للقراء المفضلين'
                    }
                    data-testid={`fav_reciter_${reciter.id}`}
                    className="w-9 h-9 rounded-full flex items-center justify-center cursor-pointer"
                  >
                    <Star
                      size={18}
                      fill={isFav ? 'var(--islamic-gold)' : 'none'}
                      style={{
                        color: isFav
                          ? 'var(--islamic-gold)'
                          : 'var(--islamic-text-muted)',
                      }}
                    />
                  </button>

                  {isSelected && (
                    <div
                      className="w-6 h-6 rounded-full flex items-center justify-center"
                      style={{
                        backgroundColor: 'var(--islamic-gold)',
                        color: 'var(--islamic-on-gold)',
                      }}
                    >
                      <Check size={14} strokeWidth={3} />
                    </div>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}
