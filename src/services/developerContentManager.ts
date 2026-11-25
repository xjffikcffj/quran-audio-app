import { DEFAULT_RECITERS } from '../data/quranData';
import type { Reciter } from '../types/quran';

const KEY_CUSTOM_RECITERS = 'quran_dev_custom_reciters';
const KEY_HIDDEN_RECITERS = 'quran_dev_hidden_reciters';
const KEY_BANNER_ENABLED = 'quran_dev_banner_enabled';
const KEY_FRIDAY_REMINDER_ENABLED = 'quran_dev_friday_reminder_enabled';

type Listener = () => void;

class DeveloperContentManager {
  private recitersList: Reciter[] = [...DEFAULT_RECITERS];
  private hiddenReciterIds: Set<string> = new Set();
  private isBannerEnabled = true;
  private isFridayReminderEnabled = true;
  private listeners: Set<Listener> = new Set();

  constructor() {
    this.load();
  }

  private load() {
    try {
      const bannerRaw = localStorage.getItem(KEY_BANNER_ENABLED);
      this.isBannerEnabled = bannerRaw === null ? true : bannerRaw === 'true';

      const fridayRaw = localStorage.getItem(KEY_FRIDAY_REMINDER_ENABLED);
      this.isFridayReminderEnabled =
        fridayRaw === null ? true : fridayRaw === 'true';

      const hiddenRaw = localStorage.getItem(KEY_HIDDEN_RECITERS);
      if (hiddenRaw) {
        const parsed = JSON.parse(hiddenRaw);
        if (Array.isArray(parsed)) {
          this.hiddenReciterIds = new Set(parsed.map(String));
        }
      }

      const customRaw = localStorage.getItem(KEY_CUSTOM_RECITERS);
      const customReciters: Reciter[] = [];
      if (customRaw) {
        const parsed = JSON.parse(customRaw);
        if (Array.isArray(parsed)) {
          for (const obj of parsed) {
            if (obj?.id && obj?.nameArabic && obj?.serverBaseUrl) {
              customReciters.push({
                id: String(obj.id),
                nameArabic: String(obj.nameArabic),
                riwayah: String(obj.riwayah || 'حفص عن عاصم'),
                serverBaseUrl: String(obj.serverBaseUrl),
              });
            }
          }
        }
      }

      const seen = new Set<string>();
      const combined: Reciter[] = [];
      for (const r of [...DEFAULT_RECITERS, ...customReciters]) {
        if (!seen.has(r.id)) {
          seen.add(r.id);
          combined.push(r);
        }
      }
      this.recitersList = combined;
    } catch {
      this.recitersList = [...DEFAULT_RECITERS];
    }
  }

  private notify() {
    this.listeners.forEach((fn) => fn());
  }

  subscribe(listener: Listener): () => void {
    this.listeners.add(listener);
    return () => {
      this.listeners.delete(listener);
    };
  }

  getState() {
    return {
      recitersList: this.recitersList,
      hiddenReciterIds: this.hiddenReciterIds,
      isBannerEnabled: this.isBannerEnabled,
      isFridayReminderEnabled: this.isFridayReminderEnabled,
    };
  }

  getVisibleReciters(): Reciter[] {
    return this.recitersList.filter((r) => !this.hiddenReciterIds.has(r.id));
  }

  addCustomReciter(
    name: string,
    riwayah: string,
    serverBaseUrl: string,
  ): boolean {
    const trimmedName = name.trim();
    const trimmedUrl = serverBaseUrl.trim();
    if (!trimmedName || !trimmedUrl) return false;

    const cleanUrl = trimmedUrl.endsWith('/') ? trimmedUrl : `${trimmedUrl}/`;
    const newReciter: Reciter = {
      id: `custom_${Date.now()}`,
      nameArabic: trimmedName,
      riwayah: riwayah.trim() || 'حفص عن عاصم',
      serverBaseUrl: cleanUrl,
    };

    this.recitersList = [...this.recitersList, newReciter];
    this.persistCustomReciters();
    this.notify();
    return true;
  }

  toggleHideReciter(reciterId: string) {
    const next = new Set(this.hiddenReciterIds);
    if (next.has(reciterId)) {
      next.delete(reciterId);
    } else {
      next.add(reciterId);
    }
    this.hiddenReciterIds = next;
    try {
      localStorage.setItem(KEY_HIDDEN_RECITERS, JSON.stringify([...next]));
    } catch {
      // ignore
    }
    this.notify();
  }

  setBannerEnabled(enabled: boolean) {
    this.isBannerEnabled = enabled;
    try {
      localStorage.setItem(KEY_BANNER_ENABLED, String(enabled));
    } catch {
      // ignore
    }
    this.notify();
  }

  setFridayReminderEnabled(enabled: boolean) {
    this.isFridayReminderEnabled = enabled;
    try {
      localStorage.setItem(KEY_FRIDAY_REMINDER_ENABLED, String(enabled));
    } catch {
      // ignore
    }
    this.notify();
  }

  private persistCustomReciters() {
    const customOnly = this.recitersList.filter((r) =>
      r.id.startsWith('custom_'),
    );
    try {
      localStorage.setItem(KEY_CUSTOM_RECITERS, JSON.stringify(customOnly));
    } catch {
      // ignore
    }
  }

  exportBackupJson(): string {
    const payload = {
      version: 1,
      timestamp: Date.now(),
      isBannerEnabled: this.isBannerEnabled,
      isFridayReminderEnabled: this.isFridayReminderEnabled,
      reciters: this.recitersList.map((r) => ({
        id: r.id,
        nameArabic: r.nameArabic,
        riwayah: r.riwayah,
        serverBaseUrl: r.serverBaseUrl,
        isHidden: this.hiddenReciterIds.has(r.id),
      })),
    };
    return JSON.stringify(payload, null, 2);
  }
}

export const developerContentManager = new DeveloperContentManager();
