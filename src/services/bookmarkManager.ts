import type { AudioBookmark } from '../types/quran';

const KEY_BOOKMARKS = 'quran_saved_bookmarks';

export class BookmarkManager {
  getBookmarks(): AudioBookmark[] {
    try {
      const raw = localStorage.getItem(KEY_BOOKMARKS);
      if (!raw) return [];
      const parsed = JSON.parse(raw);
      if (!Array.isArray(parsed)) return [];
      return parsed.filter(
        (item): item is AudioBookmark =>
          typeof item?.id === 'string' &&
          item.id.trim().length > 0 &&
          typeof item?.surahNumber === 'number' &&
          item.surahNumber >= 1 &&
          item.surahNumber <= 114 &&
          typeof item?.positionMs === 'number' &&
          item.positionMs >= 0,
      );
    } catch {
      return [];
    }
  }

  addBookmark(
    surahNumber: number,
    positionMs: number,
    name: string,
  ): AudioBookmark | null {
    if (surahNumber < 1 || surahNumber > 114 || positionMs < 0) {
      return null;
    }

    const id =
      typeof crypto !== 'undefined' && crypto.randomUUID
        ? crypto.randomUUID()
        : `bm_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`;

    const bookmark: AudioBookmark = {
      id,
      surahNumber,
      positionMs,
      name: name.trim() || `علامة - سورة ${surahNumber}`,
      createdAt: Date.now(),
    };

    this.saveBookmarks([...this.getBookmarks(), bookmark]);
    return bookmark;
  }

  renameBookmark(id: string, newName: string): boolean {
    const trimmed = newName.trim();
    if (!trimmed) return false;

    const bookmarks = this.getBookmarks();
    if (!bookmarks.some((b) => b.id === id)) return false;

    this.saveBookmarks(
      bookmarks.map((b) => (b.id === id ? { ...b, name: trimmed } : b)),
    );
    return true;
  }

  deleteBookmark(id: string): boolean {
    const bookmarks = this.getBookmarks();
    const updated = bookmarks.filter((b) => b.id !== id);
    if (updated.length === bookmarks.length) return false;
    this.saveBookmarks(updated);
    return true;
  }

  private saveBookmarks(bookmarks: AudioBookmark[]) {
    try {
      localStorage.setItem(KEY_BOOKMARKS, JSON.stringify(bookmarks));
    } catch {
      // ignore
    }
  }
}

export const bookmarkManager = new BookmarkManager();
