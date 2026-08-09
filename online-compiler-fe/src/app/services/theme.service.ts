import { Injectable, signal } from '@angular/core';

export type ThemeMode = 'dark' | 'light';

const STORAGE_KEY = 'online-compiler-theme';

/**
 * Owns the app-wide dark/light preference. Persists to localStorage and
 * mirrors the choice onto {@code document.documentElement}'s {@code data-theme}
 * attribute so CSS variables in styles.scss switch instantly.
 */
@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly _mode = signal<ThemeMode>(this.readInitialMode());
  readonly mode = this._mode.asReadonly();

  constructor() {
    this.apply(this._mode());
  }

  isDark(): boolean {
    return this._mode() === 'dark';
  }

  toggle(): void {
    this.setMode(this._mode() === 'dark' ? 'light' : 'dark');
  }

  setMode(mode: ThemeMode): void {
    this._mode.set(mode);
    this.apply(mode);
    try {
      localStorage.setItem(STORAGE_KEY, mode);
    } catch {
      // Storage may be unavailable (private mode / blocked); ignore.
    }
  }

  private apply(mode: ThemeMode): void {
    document.documentElement.setAttribute('data-theme', mode);
  }

  private readInitialMode(): ThemeMode {
    try {
      const stored = localStorage.getItem(STORAGE_KEY);
      if (stored === 'light' || stored === 'dark') {
        return stored;
      }
    } catch {
      // fall through to system preference
    }

    if (typeof window !== 'undefined' && window.matchMedia?.('(prefers-color-scheme: light)').matches) {
      return 'light';
    }
    return 'dark';
  }
}
