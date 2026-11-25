import { Clock, MoonStar, TimerOff, X } from 'lucide-react';
import { formatTimerSeconds } from '../services/sleepTimerManager';

interface SleepTimerBottomSheetProps {
  isTimerActive: boolean;
  remainingSeconds: number;
  onStartTimerMinutes: (minutes: number) => void;
  onStartTimerSeconds: (seconds: number) => void;
  onCancelTimer: () => void;
  onDismiss: () => void;
}

const DURATION_OPTIONS = [5, 10, 15, 30, 45, 60];

export function SleepTimerBottomSheet({
  isTimerActive,
  remainingSeconds,
  onStartTimerMinutes,
  onStartTimerSeconds,
  onCancelTimer,
  onDismiss,
}: SleepTimerBottomSheetProps) {
  return (
    <div
      className="fixed inset-0 z-50 flex items-end sm:items-center justify-center bg-black/65 backdrop-blur-sm"
      onClick={onDismiss}
    >
      <div
        data-testid="sleep_timer_sheet"
        className="w-full sm:max-w-md max-h-[90vh] overflow-y-auto rounded-t-3xl sm:rounded-3xl border px-6 pb-9 pt-3 shadow-2xl"
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

        {/* Header */}
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <MoonStar size={22} style={{ color: 'var(--islamic-gold)' }} />
            <h2
              className="font-cairo font-bold text-lg"
              style={{ color: 'var(--islamic-gold-light)' }}
            >
              مؤقت إيقاف التلاوة
            </h2>
          </div>

          <button
            type="button"
            onClick={onDismiss}
            aria-label="إغلاق"
            data-testid="close_timer_sheet"
            className="w-10 h-10 rounded-full flex items-center justify-center cursor-pointer"
            style={{ color: 'var(--islamic-text-secondary)' }}
          >
            <X size={20} />
          </button>
        </div>

        <p
          className="font-cairo text-[13px] mt-1 mb-4"
          style={{ color: 'var(--islamic-text-secondary)' }}
        >
          حدد مدة زمنية لإيقاف التلاوة الصوتية تلقائيًا عند انتهاء الوقت
          لمساعدتك على النوم والراحة
        </p>

        {/* Active Countdown Card */}
        {isTimerActive && (
          <div
            data-testid="active_timer_card"
            className="w-full rounded-2xl border p-4 flex flex-col items-center gap-2 mb-5"
            style={{
              backgroundColor: 'var(--islamic-card-hover)',
              borderColor: 'var(--islamic-gold)',
            }}
          >
            <span
              className="font-cairo text-[13px]"
              style={{ color: 'var(--islamic-text-secondary)' }}
            >
              الوقت المتبقي حتى إيقاف التلاوة:
            </span>

            <span
              className="font-cairo font-bold text-4xl tabular-nums"
              style={{ color: 'var(--islamic-gold-light)' }}
            >
              {formatTimerSeconds(remainingSeconds)}
            </span>

            <span
              className="font-cairo text-[11px] text-center"
              style={{ color: 'var(--islamic-emerald-light)' }}
            >
              ستتوقف التلاوة تلقائياً عند الوصول إلى 00:00
            </span>

            <button
              type="button"
              onClick={onCancelTimer}
              data-testid="cancel_sleep_timer_button"
              className="w-full mt-1 py-2.5 px-4 rounded-[10px] flex items-center justify-center gap-1.5 font-cairo font-bold text-sm cursor-pointer"
              style={{
                backgroundColor:
                  'color-mix(in srgb, var(--islamic-red) 20%, transparent)',
                color: 'var(--islamic-red)',
              }}
            >
              <TimerOff size={18} />
              <span>إلغاء المؤقت</span>
            </button>
          </div>
        )}

        {isTimerActive && (
          <p
            className="font-cairo text-[13px] mb-2.5"
            style={{ color: 'var(--islamic-text-muted)' }}
          >
            أو اختر مدة جديدة لإعادة ضبط المؤقت:
          </p>
        )}

        {/* Duration Grid */}
        <div className="grid grid-cols-3 gap-2.5">
          {DURATION_OPTIONS.map((minutes) => (
            <button
              key={minutes}
              type="button"
              onClick={() => {
                onStartTimerMinutes(minutes);
                onDismiss();
              }}
              data-testid={`timer_option_${minutes}_min`}
              className="rounded-xl border py-3.5 flex flex-col items-center justify-center gap-0.5 cursor-pointer transition-colors hover:opacity-90"
              style={{
                backgroundColor: 'var(--islamic-card)',
                borderColor: 'var(--islamic-border)',
              }}
            >
              <span
                className="font-cairo font-bold text-lg tabular-nums"
                style={{ color: 'var(--islamic-gold-light)' }}
              >
                {minutes}
              </span>
              <span
                className="font-cairo text-xs"
                style={{ color: 'var(--islamic-text-secondary)' }}
              >
                دقيقة
              </span>
            </button>
          ))}
        </div>

        {/* 30-second quick test option */}
        <button
          type="button"
          onClick={() => {
            onStartTimerSeconds(30);
            onDismiss();
          }}
          data-testid="timer_option_30_sec"
          className="mt-2.5 w-full rounded-xl border py-2.5 px-3.5 flex items-center justify-center gap-1.5 cursor-pointer"
          style={{
            backgroundColor: 'var(--islamic-surface-variant)',
            borderColor: 'var(--islamic-border)',
            color: 'var(--islamic-gold-light)',
          }}
        >
          <Clock size={16} style={{ color: 'var(--islamic-gold)' }} />
          <span className="font-cairo text-xs font-medium">
            اختبار سريع للمؤقت (30 ثانية)
          </span>
        </button>
      </div>
    </div>
  );
}
