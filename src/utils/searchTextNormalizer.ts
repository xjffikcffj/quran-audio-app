/**
 * Normalizes Arabic and Latin text for reliable search.
 * Handles diacritics, tatweel, common Arabic letter variants,
 * Arabic-Indic digits, and Persian digits.
 */
export function normalizeSearchText(value: string): string {
  const decomposed = value.toLowerCase().normalize('NFD');
  let out = '';

  for (const ch of decomposed) {
    const code = ch.charCodeAt(0);

    // Skip combining marks (Mn, Mc, Me) and Arabic tatweel (\u0640)
    if (
      /[\p{Mn}\p{Mc}\p{Me}]/u.test(ch) ||
      code === 0x0640
    ) {
      continue;
    }

    // Arabic-Indic digits (٠-٩)
    if (code >= 0x0660 && code <= 0x0669) {
      out += String.fromCharCode(48 + (code - 0x0660));
      continue;
    }

    // Persian digits (۰-۹)
    if (code >= 0x06f0 && code <= 0x06f9) {
      out += String.fromCharCode(48 + (code - 0x06f0));
      continue;
    }

    switch (ch) {
      case '\u0622': // آ
      case '\u0623': // أ
      case '\u0625': // إ
      case '\u0671': // ٱ
        out += '\u0627'; // ا
        break;
      case '\u0649': // ى
      case '\u0626': // ئ
        out += '\u064a'; // ي
        break;
      case '\u0624': // ؤ
        out += '\u0648'; // و
        break;
      case '\u0629': // ة
        out += '\u0647'; // ه
        break;
      default:
        out += ch;
        break;
    }
  }

  return out.replace(/\s+/g, ' ').trim();
}
