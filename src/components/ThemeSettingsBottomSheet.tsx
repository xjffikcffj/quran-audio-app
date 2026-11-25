import { BedDouble, Check, Moon, Sun } from 'lucide-react';
import { AppThemeMode, THEME_MODE_META } from '../types/quran';

interface ThemeSettingsBottomSheetProps {
  currentMode: AppThemeMode;
  onSelectMode: (mode: AppThemeMode) => void;
  onDismiss: () => void;
}

const MODES = [AppThemeMode.DARK, AppThemeMode.PURE_NIGHT, AppThemeMode.LIGHT];

export function ThemeSettingsBottomSheet({
  currentMode,
  onSelectMode,
  onDismiss,
}: ThemeSettingsBottomSheetProps) {
  const renderIcon = (mode: AppThemeMode, color: string, size = 22) => {
    if (mode === AppThemeMode.PURE_NIGHT) {
      return <BedDouble size={size} style={{ color }} />;
    }
    if (mode === AppThemeMode.LIGHT) {
      return <Sun size={size} style={{ color }} />;
    }
    return <Moon size={size} style={{ color }} />;
  };

  return (
    <div
      className="fixed inset-0 z-50 flex items-end sm:items-center justify-center bg-black/65 backdrop-blur-sm"
      onClick={onDismiss}
    >
      <div
        data-testid="theme_settings_sheet"
        className="w-full sm:max-w-md rounded-t-3xl sm:rounded-3xl border px-5 pb-8 pt-3 shadow-2xl"
        style={{
          backgroundColor: 'var(--islamic-bg)',
          borderColor: 'var(--islamic-border)',
        }}
        onClick={(e) => e.stopPropagation()}
      >
        <div
          className="w-11 h-1 rounded-full mx-auto mb-3"
          style={{ backgroundColor: 'var(--islamic-border)' }}
        />

        <div className="flex items-center justify-between mb-4">
          <div>
            <h2
              className="font-amiri font-bold text-[22px]"
              style={{ color: 'var(--islamic-gold-light)' }}
            >
              المظهر والوضع الليلي
            </h2>
            <p
              className="font-cairo text-[13px]"
              style={{ color: 'var(--islamic-text-secondary)' }}
            >
              اختر المظهر المناسب للعين أثناء التلاوة
            </p>
          </div>

          <div
            className="w-11 h-11 rounded-full border flex items-center justify-center shrink-0"
            style={{
              backgroundColor: 'var(--islamic-card-hover)',
              borderColor:
                'color-mix(in srgb, var(--islamic-gold) 50%, transparent)',
            }}
          >
            {renderIcon(currentMode, 'var(--islamic-gold)', 22)}
          </div>
        </div>

        <div className="flex flex-col gap-3">
          {MODES.map((mode) => {
            const isSelected = mode === currentMode;
            const meta = THEME_MODE_META[mode];
            const previewBg =
              mode === AppThemeMode.PURE_NIGHT
                ? '#000000'
                : mode === AppThemeMode.DARK
                  ? '#0D1B16'
                  : '#E2EBE6';

            return (
              <button
                key={mode}
                type="button"
                onClick={() => onSelectMode(mode)}
                data-testid={`theme_mode_option_${mode.toLowerCase()}`}
                className="w-full rounded-[14px] border px-4 py-3.5 flex items-center justify-between gap-3 text-right cursor-pointer transition-colors"
                style={{
                  backgroundColor: isSelected
                    ? 'var(--islamic-card-hover)'
                    : 'var(--islamic-card)',
                  borderColor: isSelected
                    ? 'var(--islamic-gold)'
                    : 'var(--islamic-border)',
                  borderWidth: isSelected ? '1.5px' : '1px',
                }}
              >
                <div className="flex items-center gap-3.5 min-w-0 flex-1">
                  <div
                    className="w-10 h-10 rounded-[10px] border flex items-center justify-center shrink-0"
                    style={{
                      backgroundColor: previewBg,
                      borderColor: 'var(--islamic-border)',
                    }}
                  >
                    {renderIcon(
                      mode,
                      isSelected
                        ? 'var(--islamic-gold)'
                        : mode === AppThemeMode.LIGHT
                          ? '#4A6357'
                          : '#A8C2B7',
                    )}
                  </div>

                  <div className="min-w-0">
                    <div
                      className="font-cairo font-bold text-[15px]"
                      style={{
                        color: isSelected
                          ? 'var(--islamic-gold-light)'
                          : 'var(--islamic-text-primary)',
                      }}
                    >
                      {meta.titleArabic}
                    </div>
                    <div
                      className="font-cairo text-xs"
                      style={{ color: 'var(--islamic-text-secondary)' }}
                    >
                      {meta.descriptionArabic}
                    </div>
                  </div>
                </div>

                {isSelected && (
                  <div
                    className="w-6 h-6 rounded-full flex items-center justify-center shrink-0"
                    style={{
                      backgroundColor: 'var(--islamic-gold)',
                      color: 'var(--islamic-on-gold)',
                    }}
                  >
                    <Check size={14} strokeWidth={3} />
                  </div>
                )}
              </button>
            );
          })}
        </div>
      </div>
    </div>
  );
}
