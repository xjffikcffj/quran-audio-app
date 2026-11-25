type Listener = () => void;

export function formatTimerSeconds(totalSeconds: number): string {
  const safe = Math.max(0, Math.floor(totalSeconds));
  const minutes = Math.floor(safe / 60);
  const seconds = safe % 60;
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
}

class SleepTimerManager {
  private isTimerActive = false;
  private remainingSeconds = 0;
  private remainingTimeMs = 0;
  private segmentStartTimeMs = 0;
  private timerPaused = false;
  private intervalId: number | null = null;
  private onFinishCallback: (() => void) | null = null;
  private listeners: Set<Listener> = new Set();

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
      isTimerActive: this.isTimerActive,
      remainingSeconds: this.remainingSeconds,
    };
  }

  setOnFinishCallback(cb: () => void) {
    this.onFinishCallback = cb;
  }

  startTimerMinutes(minutes: number, isCurrentlyPlaying: boolean) {
    this.startTimerWithSeconds(Math.max(0, minutes) * 60, isCurrentlyPlaying);
  }

  startTimerWithSeconds(seconds: number, isCurrentlyPlaying: boolean) {
    this.cancelTimer();
    if (seconds <= 0) return;

    this.remainingTimeMs = seconds * 1000;
    this.timerPaused = !isCurrentlyPlaying;
    this.isTimerActive = true;
    this.remainingSeconds = seconds;
    this.notify();

    if (!this.timerPaused) {
      this.startCountdown();
    }
  }

  pauseTimer() {
    if (!this.isTimerActive || this.timerPaused) return;
    const elapsed = Date.now() - this.segmentStartTimeMs;
    this.remainingTimeMs = Math.max(0, this.remainingTimeMs - elapsed);
    this.remainingSeconds = Math.ceil(this.remainingTimeMs / 1000);
    this.timerPaused = true;
    if (this.intervalId !== null) {
      window.clearInterval(this.intervalId);
      this.intervalId = null;
    }
    this.notify();
  }

  resumeTimer() {
    if (!this.isTimerActive || !this.timerPaused) return;
    if (this.remainingTimeMs <= 0) {
      this.finishTimer();
      return;
    }
    this.timerPaused = false;
    this.startCountdown();
    this.notify();
  }

  private startCountdown() {
    if (this.intervalId !== null) {
      window.clearInterval(this.intervalId);
    }
    this.segmentStartTimeMs = Date.now();
    this.intervalId = window.setInterval(() => {
      if (!this.isTimerActive || this.timerPaused) return;
      const elapsed = Date.now() - this.segmentStartTimeMs;
      const remaining = Math.max(0, this.remainingTimeMs - elapsed);
      const nextSeconds = Math.ceil(remaining / 1000);

      if (nextSeconds !== this.remainingSeconds) {
        this.remainingSeconds = nextSeconds;
        this.notify();
      }

      if (remaining <= 0) {
        this.finishTimer();
      }
    }, 250);
  }

  private finishTimer() {
    if (!this.isTimerActive) return;
    this.remainingTimeMs = 0;
    this.remainingSeconds = 0;
    if (this.onFinishCallback) {
      this.onFinishCallback();
    }
    this.cancelTimer();
  }

  cancelTimer() {
    if (this.intervalId !== null) {
      window.clearInterval(this.intervalId);
      this.intervalId = null;
    }
    this.remainingTimeMs = 0;
    this.segmentStartTimeMs = 0;
    this.timerPaused = false;
    this.isTimerActive = false;
    this.remainingSeconds = 0;
    this.notify();
  }
}

export const sleepTimerManager = new SleepTimerManager();
