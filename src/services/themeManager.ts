import { AppThemeMode } from '../types/quran';

const KEY_THEME_MODE = 'quran_theme_mode';

type Listener = () => void;

class ThemeManager {
  private mode: AppThemeMode = AppThemeMode.DARK;
  private listeners: Set<Listener> = new Set();

  constructor() {
    this.load();
    this.applyToDom();
  }

  private load() {
    try {
      const saved = localStorage.getItem(KEY_THEME_MODE);
      if (
        saved === AppThemeMode.DARK ||
        saved === AppThemeMode.PURE_NIGHT ||
        saved === AppThemeMode.LIGHT
      ) {
        this.mode = saved;
      }
    } catch {
      this.mode = AppThemeMode.DARK;
    }
  }

  private applyToDom() {
    if (typeof document === 'undefined') return;
    const attr =
      this.mode === AppThemeMode.PURE_NIGHT
        ? 'pure-night'
        : this.mode === AppThemeMode.LIGHT
          ? 'light'
          : 'dark';
    document.documentElement.setAttribute('data-theme', attr);
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

  getThemeMode(): AppThemeMode {
    return this.mode;
  }

  setThemeMode(mode: AppThemeMode) {
    this.mode = mode;
    this.applyToDom();
    try {
      localStorage.setItem(KEY_THEME_MODE, mode);
    } catch {
      // ignore
    }
    this.notify();
  }

  toggleNextMode() {
    const next =
      this.mode === AppThemeMode.DARK
        ? AppThemeMode.PURE_NIGHT
        : this.mode === AppThemeMode.PURE_NIGHT
          ? AppThemeMode.LIGHT
          : AppThemeMode.DARK;
    this.setThemeMode(next);
  }
}

export const themeManager = new ThemeManager();
