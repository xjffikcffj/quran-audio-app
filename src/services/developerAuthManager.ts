const KEY_DEV_EMAIL = 'quran_dev_email';
const KEY_DEV_HASH = 'quran_dev_hash';
const KEY_DEV_IS_SETUP = 'quran_dev_is_setup';
const SALT = 'QuranAudio_DevSecurity_Salt_2026';

type Listener = () => void;

async function sha256Hex(input: string): Promise<string> {
  if (typeof crypto !== 'undefined' && crypto.subtle) {
    const data = new TextEncoder().encode(input);
    const hashBuffer = await crypto.subtle.digest('SHA-256', data);
    const hashArray = Array.from(new Uint8Array(hashBuffer));
    return hashArray.map((b) => b.toString(16).padStart(2, '0')).join('');
  }

  // Fallback pure-JS SHA-256 if crypto.subtle is unavailable
  const bytes = new TextEncoder().encode(input);
  const K = new Uint32Array([
    0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
    0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
    0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
    0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
    0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
    0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
    0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
    0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2,
  ]);
  const H = new Uint32Array([
    0x6a09e667, 0xbb67ae85, 0x3c6ef372, 0xa54ff53a,
    0x510e527f, 0x9b05688c, 0x1f83d9ab, 0x5be0cd19,
  ]);
  const l = bytes.length;
  const paddedLen = Math.ceil((l + 9) / 64) * 64;
  const padded = new Uint8Array(paddedLen);
  padded.set(bytes);
  padded[l] = 0x80;
  const view = new DataView(padded.buffer);
  view.setUint32(paddedLen - 4, (l * 8) >>> 0, false);
  view.setUint32(paddedLen - 8, Math.floor((l * 8) / 0x100000000), false);

  const w = new Uint32Array(64);
  const rotr = (x: number, n: number) => (x >>> n) | (x << (32 - n));

  for (let i = 0; i < paddedLen; i += 64) {
    for (let j = 0; j < 16; j++) w[j] = view.getUint32(i + j * 4, false);
    for (let j = 16; j < 64; j++) {
      const s0 = rotr(w[j - 15], 7) ^ rotr(w[j - 15], 18) ^ (w[j - 15] >>> 3);
      const s1 = rotr(w[j - 2], 17) ^ rotr(w[j - 2], 19) ^ (w[j - 2] >>> 10);
      w[j] = (w[j - 16] + s0 + w[j - 7] + s1) >>> 0;
    }
    let [a, b, c, d, e, f, g, h] = H;
    for (let j = 0; j < 64; j++) {
      const S1 = rotr(e, 6) ^ rotr(e, 11) ^ rotr(e, 25);
      const ch = (e & f) ^ (~e & g);
      const temp1 = (h + S1 + ch + K[j] + w[j]) >>> 0;
      const S0 = rotr(a, 2) ^ rotr(a, 13) ^ rotr(a, 22);
      const maj = (a & b) ^ (a & c) ^ (b & c);
      const temp2 = (S0 + maj) >>> 0;
      h = g;
      g = f;
      f = e;
      e = (d + temp1) >>> 0;
      d = c;
      c = b;
      b = a;
      a = (temp1 + temp2) >>> 0;
    }
    H[0] = (H[0] + a) >>> 0;
    H[1] = (H[1] + b) >>> 0;
    H[2] = (H[2] + c) >>> 0;
    H[3] = (H[3] + d) >>> 0;
    H[4] = (H[4] + e) >>> 0;
    H[5] = (H[5] + f) >>> 0;
    H[6] = (H[6] + g) >>> 0;
    H[7] = (H[7] + h) >>> 0;
  }
  return Array.from(H)
    .map((n) => n.toString(16).padStart(8, '0'))
    .join('');
}

class DeveloperAuthManager {
  private isLoggedIn = false;
  private isAccountSetup = false;
  private developerEmail = '';
  private listeners: Set<Listener> = new Set();

  constructor() {
    this.load();
  }

  private load() {
    try {
      this.isAccountSetup = localStorage.getItem(KEY_DEV_IS_SETUP) === 'true';
      this.developerEmail = localStorage.getItem(KEY_DEV_EMAIL) ?? '';
    } catch {
      this.isAccountSetup = false;
      this.developerEmail = '';
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
      isLoggedIn: this.isLoggedIn,
      isAccountSetup: this.isAccountSetup,
      developerEmail: this.developerEmail,
    };
  }

  async setupDeveloperAccount(email: string, password: string): Promise<boolean> {
    const trimmedEmail = email.trim();
    if (!trimmedEmail || password.length < 6) return false;

    const hash = await sha256Hex(SALT + password);
    try {
      localStorage.setItem(KEY_DEV_EMAIL, trimmedEmail);
      localStorage.setItem(KEY_DEV_HASH, hash);
      localStorage.setItem(KEY_DEV_IS_SETUP, 'true');
    } catch {
      // ignore
    }

    this.isAccountSetup = true;
    this.developerEmail = trimmedEmail;
    this.isLoggedIn = true;
    this.notify();
    return true;
  }

  async login(email: string, password: string): Promise<boolean> {
    let savedEmail = '';
    let savedHash = '';
    try {
      savedEmail = localStorage.getItem(KEY_DEV_EMAIL) ?? '';
      savedHash = localStorage.getItem(KEY_DEV_HASH) ?? '';
    } catch {
      return false;
    }

    const hash = await sha256Hex(SALT + password);
    if (
      savedEmail.toLowerCase() === email.trim().toLowerCase() &&
      savedHash === hash
    ) {
      this.isLoggedIn = true;
      this.notify();
      return true;
    }
    return false;
  }

  logout() {
    this.isLoggedIn = false;
    this.notify();
  }
}

export const developerAuthManager = new DeveloperAuthManager();
