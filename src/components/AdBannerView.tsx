import { useEffect, useState } from 'react';
import { developerContentManager } from '../services/developerContentManager';

export function AdBannerView() {
  const [isBannerEnabled, setIsBannerEnabled] = useState(
    () => developerContentManager.getState().isBannerEnabled,
  );

  useEffect(() => {
    return developerContentManager.subscribe(() => {
      setIsBannerEnabled(developerContentManager.getState().isBannerEnabled);
    });
  }, []);

  if (!isBannerEnabled) {
    return null;
  }

  return (
    <div
      data-testid="ad_banner_slot"
      className="w-full px-3 pb-1.5 pt-0.5 flex items-center justify-center"
    >
      <div
        className="w-full h-10 rounded-lg border flex items-center justify-center px-3 text-[11px] font-cairo"
        style={{
          backgroundColor: 'var(--islamic-surface)',
          borderColor: 'var(--islamic-border)',
          color: 'var(--islamic-text-muted)',
        }}
      >
        <span>مساحة إعلانية هادئة (وضع الويب)</span>
      </div>
    </div>
  );
}
