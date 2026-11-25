const KEY_FAVORITE_SURAHS = 'quran_favorite_surahs';
const KEY_FAVORITE_RECITERS = 'quran_favorite_reciters';

type Listener = () => void;

class FavoritesManager {
  private favoriteSurahs: Set<number> = new Set();
  private favoriteReciters: Set<string> = new Set();
  private listeners: Set<Listener> = new Set();

  constructor() {
    this.load();
  }

  private load() {
    try {
      const rawSurahs = localStorage.getItem(KEY_FAVORITE_SURAHS);
      if (rawSurahs) {
        const parsed = JSON.parse(rawSurahs);
        if (Array.isArray(parsed)) {
          this.favoriteSurahs = new Set(
            parsed.map(Number).filter((n) => Number.isInteger(n) && n >= 1 && n <= 114),
          );
        }
      }
      const rawReciters = localStorage.getItem(KEY_FAVORITE_RECITERS);
      if (rawReciters) {
        const parsed = JSON.parse(rawReciters);
        if (Array.isArray(parsed)) {
          this.favoriteReciters = new Set(parsed.map(String));
        }
      }
    } catch {
      // ignore corrupted storage
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

  getFavoriteSurahs(): Set<number> {
    return this.favoriteSurahs;
  }

  getFavoriteReciters(): Set<string> {
    return this.favoriteReciters;
  }

  isSurahFavorite(surahNumber: number): boolean {
    return this.favoriteSurahs.has(surahNumber);
  }

  toggleSurahFavorite(surahNumber: number) {
    const next = new Set(this.favoriteSurahs);
    if (next.has(surahNumber)) {
      next.delete(surahNumber);
    } else {
      next.add(surahNumber);
    }
    this.favoriteSurahs = next;
    try {
      localStorage.setItem(KEY_FAVORITE_SURAHS, JSON.stringify([...next]));
    } catch {
      // ignore storage quota errors
    }
    this.notify();
  }

  isReciterFavorite(reciterId: string): boolean {
    return this.favoriteReciters.has(reciterId);
  }

  toggleReciterFavorite(reciterId: string) {
    const next = new Set(this.favoriteReciters);
    if (next.has(reciterId)) {
      next.delete(reciterId);
    } else {
      next.add(reciterId);
    }
    this.favoriteReciters = next;
    try {
      localStorage.setItem(KEY_FAVORITE_RECITERS, JSON.stringify([...next]));
    } catch {
      // ignore storage quota errors
    }
    this.notify();
  }
}

export const favoritesManager = new FavoritesManager();
