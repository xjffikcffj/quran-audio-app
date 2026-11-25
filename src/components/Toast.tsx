import { useEffect } from 'react';

interface ToastProps {
  message: string | null;
  onDismiss: () => void;
}

export function Toast({ message, onDismiss }: ToastProps) {
  useEffect(() => {
    if (!message) return;
    const id = window.setTimeout(onDismiss, 3200);
    return () => window.clearTimeout(id);
  }, [message, onDismiss]);

  if (!message) return null;

  return (
    <div
      role="status"
      aria-live="polite"
      className="fixed top-4 left-1/2 -translate-x-1/2 z-[100] px-4 py-2.5 rounded-xl border shadow-lg text-sm font-cairo font-bold max-w-[90vw] text-center transition-opacity"
      style={{
        backgroundColor: 'var(--islamic-surface)',
        borderColor: 'var(--islamic-gold)',
        color: 'var(--islamic-gold-light)',
      }}
    >
      {message}
    </div>
  );
}
