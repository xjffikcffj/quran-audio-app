import { useCallback, useEffect, useRef, useState } from 'react';
import { QURAN_SURAHS } from '../data/quranData';
import { developerContentManager } from '../services/developerContentManager';
import { sleepTimerManager } from '../services/sleepTimerManager';
import {
  type AudioPlayerState,
  getReciterAudioUrlForSurah,
  PlaybackStatus,
  type Reciter,
  type Surah,
} from '../types/quran';

const KEY_SURAH_NUMBER = 'quran_last_surah_number';
const KEY_RECITER_ID = 'quran_last_reciter_id';
const KEY_POSITION = 'quran_last_position_ms';
const KEY_SPEED = 'quran_last_playback_speed';
const KEY_REPEAT = 'quran_last_repeat_one';
const SURAH_POSITION_PREFIX = 'quran_surah_position_';
const SAVE_INTERVAL_MS = 2000;

function getSavedSurah(): Surah {
  try {
    const num = Number(localStorage.getItem(KEY_SURAH_NUMBER) || '1');
    return QURAN_SURAHS.find((s) => s.number === num) ?? QURAN_SURAHS[0];
  } catch {
    return QURAN_SURAHS[0];
  }
}

function getSavedReciter(): Reciter {
  const all = developerContentManager.getState().recitersList;
  try {
    const savedId = localStorage.getItem(KEY_RECITER_ID);
    return all.find((r) => r.id === savedId) ?? all[0];
  } catch {
    return all[0];
  }
}

function getSavedPositionForSurah(surahNumber: number): number {
  try {
    const raw = localStorage.getItem(`${SURAH_POSITION_PREFIX}${surahNumber}`);
    if (raw !== null) {
      const val = Number(raw);
      return Number.isFinite(val) && val > 0 ? val : 0;
    }
    const oldSurah = Number(localStorage.getItem(KEY_SURAH_NUMBER) || '1');
    if (oldSurah === surahNumber) {
      const oldPos = Number(localStorage.getItem(KEY_POSITION) || '0');
      return Number.isFinite(oldPos) && oldPos > 0 ? oldPos : 0;
    }
  } catch {
    // ignore
  }
  return 0;
}

function getSavedSpeed(): number {
  try {
    const val = Number(localStorage.getItem(KEY_SPEED) || '1');
    return val >= 0.25 && val <= 3.0 ? val : 1.0;
  } catch {
    return 1.0;
  }
}

function getSavedRepeat(): boolean {
  try {
    return localStorage.getItem(KEY_REPEAT) === 'true';
  } catch {
    return false;
  }
}

export function useAudioPlayer() {
  const initialSurah = getSavedSurah();
  const initialReciter = getSavedReciter();
  const initialPosition = getSavedPositionForSurah(initialSurah.number);

  const [state, setState] = useState<AudioPlayerState>({
    currentSurah: initialSurah,
    currentReciter: initialReciter,
    playbackStatus: PlaybackStatus.IDLE,
    isPlaying: false,
    isBuffering: false,
    currentPositionMs: initialPosition,
    durationMs: 0,
    playbackSpeed: getSavedSpeed(),
    isRepeatOne: getSavedRepeat(),
    errorMessage: null,
    isPlayerExpanded: false,
  });

  const audioRef = useRef<HTMLAudioElement | null>(null);
  const stateRef = useRef<AudioPlayerState>(state);
  stateRef.current = state;
  const pendingSeekMsRef = useRef<number | null>(initialPosition > 0 ? initialPosition : null);
  const lastSaveTimeRef = useRef<number>(0);

  const savePlaybackState = useCallback((positionOverride?: number) => {
    const s = stateRef.current;
    const audio = audioRef.current;
    const pos =
      positionOverride ??
      (audio && Number.isFinite(audio.currentTime)
        ? Math.max(0, Math.floor(audio.currentTime * 1000))
        : s.currentPositionMs);

    try {
      localStorage.setItem(KEY_SURAH_NUMBER, String(s.currentSurah.number));
      localStorage.setItem(KEY_RECITER_ID, s.currentReciter.id);
      localStorage.setItem(KEY_POSITION, String(pos));
      localStorage.setItem(`${SURAH_POSITION_PREFIX}${s.currentSurah.number}`, String(pos));
      localStorage.setItem(KEY_SPEED, String(s.playbackSpeed));
      localStorage.setItem(KEY_REPEAT, String(s.isRepeatOne));
    } catch {
      // ignore
    }
  }, []);

  const updateMediaSession = useCallback((surah: Surah, reciter: Reciter) => {
    if (typeof navigator !== 'undefined' && 'mediaSession' in navigator) {
      navigator.mediaSession.metadata = new MediaMetadata({
        title: `سورة ${surah.nameArabic}`,
        artist: reciter.nameArabic,
        album: 'القرآن الكريم',
        artwork: [
          {
            src: '/images/ic_quran_app.jpg',
            sizes: '512x512',
            type: 'image/jpeg',
          },
        ],
      });
    }
  }, []);

  const playSurah = useCallback(
    (surah: Surah, reciter?: Reciter) => {
      const audio = audioRef.current;
      if (!audio) return;

      savePlaybackState();

      const targetReciter = reciter ?? stateRef.current.currentReciter;
      const audioUrl = getReciterAudioUrlForSurah(targetReciter, surah.number);
      const startPosMs = getSavedPositionForSurah(surah.number);

      setState((prev) => ({
        ...prev,
        currentSurah: surah,
        currentReciter: targetReciter,
        currentPositionMs: startPosMs,
        durationMs: 0,
        playbackStatus: PlaybackStatus.BUFFERING,
        isBuffering: true,
        isPlaying: false,
        errorMessage: null,
      }));

      pendingSeekMsRef.current = startPosMs > 0 ? startPosMs : null;

      audio.src = audioUrl;
      audio.playbackRate = stateRef.current.playbackSpeed;
      audio.load();

      updateMediaSession(surah, targetReciter);

      const playPromise = audio.play();
      if (playPromise !== undefined) {
        playPromise.catch((err: unknown) => {
          // Ignore AbortError caused by rapid surah switching
          if (err instanceof DOMException && err.name === 'AbortError') {
            return;
          }
          setState((prev) => ({
            ...prev,
            playbackStatus: PlaybackStatus.ERROR,
            isPlaying: false,
            isBuffering: false,
            errorMessage:
              'تعذر تشغيل التلاوة. يرجى التأكد من الاتصال بالإنترنت.',
          }));
        });
      }
    },
    [savePlaybackState, updateMediaSession],
  );

  const playNext = useCallback(() => {
    const currentNumber = stateRef.current.currentSurah.number;
    const nextNumber = currentNumber >= 114 ? 1 : currentNumber + 1;
    const nextSurah =
      QURAN_SURAHS.find((s) => s.number === nextNumber) ?? QURAN_SURAHS[0];
    playSurah(nextSurah);
  }, [playSurah]);

  const playPrevious = useCallback(() => {
    const currentNumber = stateRef.current.currentSurah.number;
    const prevNumber = currentNumber <= 1 ? 114 : currentNumber - 1;
    const prevSurah =
      QURAN_SURAHS.find((s) => s.number === prevNumber) ??
      QURAN_SURAHS[QURAN_SURAHS.length - 1];
    playSurah(prevSurah);
  }, [playSurah]);

  const togglePlayPause = useCallback(() => {
    const audio = audioRef.current;
    if (!audio) return;

    if (!audio.paused) {
      audio.pause();
    } else {
      if (!audio.src) {
        playSurah(
          stateRef.current.currentSurah,
          stateRef.current.currentReciter,
        );
      } else {
        setState((prev) => ({
          ...prev,
          isBuffering: true,
          errorMessage: null,
        }));
        audio.playbackRate = stateRef.current.playbackSpeed;
        audio.play().catch(() => {
          setState((prev) => ({
            ...prev,
            playbackStatus: PlaybackStatus.ERROR,
            isPlaying: false,
            isBuffering: false,
            errorMessage:
              'تعذر تحميل التلاوة. يرجى التحقق من اتصال الإنترنت والمحاولة مرة أخرى.',
          }));
        });
      }
    }
  }, [playSurah]);

  const pause = useCallback(() => {
    audioRef.current?.pause();
    savePlaybackState();
  }, [savePlaybackState]);

  const seekTo = useCallback(
    (positionMs: number) => {
      const audio = audioRef.current;
      const clampedMs = Math.max(0, positionMs);
      if (audio && Number.isFinite(audio.duration) && audio.duration > 0) {
        audio.currentTime = Math.min(audio.duration, clampedMs / 1000);
      }
      setState((prev) => ({ ...prev, currentPositionMs: clampedMs }));
      savePlaybackState(clampedMs);
    },
    [savePlaybackState],
  );

  const skipForward = useCallback(
    (seconds = 10) => {
      const audio = audioRef.current;
      if (!audio) return;
      const durMs = Number.isFinite(audio.duration)
        ? audio.duration * 1000
        : stateRef.current.durationMs;
      const curMs = audio.currentTime * 1000;
      const newPos = Math.min(durMs || Infinity, curMs + seconds * 1000);
      seekTo(newPos);
    },
    [seekTo],
  );

  const skipBackward = useCallback(
    (seconds = 10) => {
      const audio = audioRef.current;
      if (!audio) return;
      const curMs = audio.currentTime * 1000;
      const newPos = Math.max(0, curMs - seconds * 1000);
      seekTo(newPos);
    },
    [seekTo],
  );

  const setReciter = useCallback(
    (reciter: Reciter) => {
      if (stateRef.current.currentReciter.id === reciter.id) return;
      playSurah(stateRef.current.currentSurah, reciter);
    },
    [playSurah],
  );

  const setPlaybackSpeed = useCallback(
    (speed: number) => {
      if (speed < 0.25 || speed > 3.0) return;
      if (audioRef.current) {
        audioRef.current.playbackRate = speed;
      }
      setState((prev) => {
        const next = { ...prev, playbackSpeed: speed };
        stateRef.current = next;
        return next;
      });
      savePlaybackState();
    },
    [savePlaybackState],
  );

  const toggleRepeatOne = useCallback(() => {
    setState((prev) => {
      const next = { ...prev, isRepeatOne: !prev.isRepeatOne };
      stateRef.current = next;
      return next;
    });
    savePlaybackState();
  }, [savePlaybackState]);

  const retry = useCallback(() => {
    setState((prev) => ({
      ...prev,
      errorMessage: null,
      isBuffering: true,
    }));
    playSurah(
      stateRef.current.currentSurah,
      stateRef.current.currentReciter,
    );
  }, [playSurah]);

  const setPlayerExpanded = useCallback((expanded: boolean) => {
    setState((prev) => ({ ...prev, isPlayerExpanded: expanded }));
  }, []);

  // Initialize HTMLAudioElement & MediaSession once
  useEffect(() => {
    const audio = new Audio();
    audio.preload = 'metadata';
    audioRef.current = audio;

    // Restore initial audio source without autoplay
    const initUrl = getReciterAudioUrlForSurah(
      stateRef.current.currentReciter,
      stateRef.current.currentSurah.number,
    );
    audio.src = initUrl;
    audio.playbackRate = stateRef.current.playbackSpeed;
    updateMediaSession(
      stateRef.current.currentSurah,
      stateRef.current.currentReciter,
    );

    sleepTimerManager.setOnFinishCallback(() => {
      audio.pause();
      savePlaybackState();
    });

    const onLoadedMetadata = () => {
      const durMs = Number.isFinite(audio.duration)
        ? Math.max(0, Math.floor(audio.duration * 1000))
        : 0;
      if (pendingSeekMsRef.current !== null && durMs > 0) {
        const targetSec = Math.min(
          audio.duration,
          pendingSeekMsRef.current / 1000,
        );
        audio.currentTime = targetSec;
        pendingSeekMsRef.current = null;
      }
      setState((prev) => ({
        ...prev,
        durationMs: durMs,
        isBuffering: false,
      }));
    };

    const onWaiting = () => {
      setState((prev) => ({
        ...prev,
        isBuffering: true,
        playbackStatus: PlaybackStatus.BUFFERING,
      }));
    };

    const onCanPlay = () => {
      setState((prev) => ({
        ...prev,
        isBuffering: false,
        playbackStatus: audio.paused
          ? PlaybackStatus.PAUSED
          : PlaybackStatus.PLAYING,
      }));
    };

    const onPlaying = () => {
      sleepTimerManager.resumeTimer();
      setState((prev) => ({
        ...prev,
        isPlaying: true,
        isBuffering: false,
        playbackStatus: PlaybackStatus.PLAYING,
        errorMessage: null,
      }));
      if (typeof navigator !== 'undefined' && 'mediaSession' in navigator) {
        navigator.mediaSession.playbackState = 'playing';
      }
    };

    const onPause = () => {
      sleepTimerManager.pauseTimer();
      setState((prev) => ({
        ...prev,
        isPlaying: false,
        isBuffering: false,
        playbackStatus:
          prev.errorMessage !== null
            ? PlaybackStatus.ERROR
            : PlaybackStatus.PAUSED,
      }));
      if (typeof navigator !== 'undefined' && 'mediaSession' in navigator) {
        navigator.mediaSession.playbackState = 'paused';
      }
      savePlaybackState();
    };

    const onTimeUpdate = () => {
      const posMs = Math.max(0, Math.floor(audio.currentTime * 1000));
      const durMs = Number.isFinite(audio.duration)
        ? Math.max(0, Math.floor(audio.duration * 1000))
        : stateRef.current.durationMs;

      setState((prev) => ({
        ...prev,
        currentPositionMs: posMs,
        durationMs: durMs > 0 ? durMs : prev.durationMs,
      }));

      const now = Date.now();
      if (now - lastSaveTimeRef.current >= SAVE_INTERVAL_MS) {
        savePlaybackState(posMs);
        lastSaveTimeRef.current = now;
      }
    };

    const onEnded = () => {
      if (stateRef.current.isRepeatOne) {
        audio.currentTime = 0;
        audio.play().catch(() => {});
      } else {
        playNext();
      }
    };

    const onError = () => {
      sleepTimerManager.pauseTimer();
      setState((prev) => ({
        ...prev,
        playbackStatus: PlaybackStatus.ERROR,
        isPlaying: false,
        isBuffering: false,
        errorMessage:
          'تعذر تحميل التلاوة. يرجى التحقق من اتصال الإنترنت والمحاولة مرة أخرى.',
      }));
      savePlaybackState();
    };

    audio.addEventListener('loadedmetadata', onLoadedMetadata);
    audio.addEventListener('waiting', onWaiting);
    audio.addEventListener('canplay', onCanPlay);
    audio.addEventListener('playing', onPlaying);
    audio.addEventListener('pause', onPause);
    audio.addEventListener('timeupdate', onTimeUpdate);
    audio.addEventListener('ended', onEnded);
    audio.addEventListener('error', onError);

    if (typeof navigator !== 'undefined' && 'mediaSession' in navigator) {
      const ms = navigator.mediaSession;
      try {
        ms.setActionHandler('play', () => audio.play().catch(() => {}));
        ms.setActionHandler('pause', () => audio.pause());
        ms.setActionHandler('previoustrack', () => playPrevious());
        ms.setActionHandler('nexttrack', () => playNext());
        ms.setActionHandler('seekbackward', () => skipBackward(10));
        ms.setActionHandler('seekforward', () => skipForward(10));
      } catch {
        // some browsers restrict specific handlers
      }
    }

    return () => {
      savePlaybackState();
      audio.pause();
      audio.removeEventListener('loadedmetadata', onLoadedMetadata);
      audio.removeEventListener('waiting', onWaiting);
      audio.removeEventListener('canplay', onCanPlay);
      audio.removeEventListener('playing', onPlaying);
      audio.removeEventListener('pause', onPause);
      audio.removeEventListener('timeupdate', onTimeUpdate);
      audio.removeEventListener('ended', onEnded);
      audio.removeEventListener('error', onError);
    };
  }, [
    playNext,
    playPrevious,
    savePlaybackState,
    skipBackward,
    skipForward,
    updateMediaSession,
  ]);

  return {
    state,
    playSurah,
    togglePlayPause,
    pause,
    seekTo,
    skipForward,
    skipBackward,
    playNext,
    playPrevious,
    setReciter,
    setPlaybackSpeed,
    toggleRepeatOne,
    retry,
    setPlayerExpanded,
  };
}
