# Quran Audio – قرآن صوتي

تطبيق ويب مبني باستخدام React وTypeScript وVite لتصفح سور القرآن الكريم الـ 114 وتشغيل التلاوات الصوتية لكبار القراء، مع واجهة عربية كاملة ودعم التشغيل عبر Media Session ومؤقت النوم والمفضلة وتخصيص المظهر ولوحة تحكم المطوّر.

## البنية الأساسية
- `src/pages/HomeScreen.tsx`: الشاشة الرئيسية (البحث، التصفية، قائمة السور، أشرطة المشغل والنوافذ المنبثقة).
- `src/hooks/useAudioPlayer.ts`: محرك تشغيل الصوت عبر HTMLAudioElement وMedia Session API مع حفظ التقدم لكل سورة في `localStorage`.
- `src/services/sleepTimerManager.ts`: مؤقت النوم المرتبط بحالة التشغيل.
- `src/services/favoritesManager.ts`: إدارة المفضلة للسور والقراء محلياً.
- `src/services/themeManager.ts`: إدارة المظاهر الثلاثة (الداكن الكلاسيكي، الليلي الكامل، الفاتح الأنيق).
- `src/services/developerAuthManager.ts` و`src/services/developerContentManager.ts`: لوحة تحكم المطوّر، القراء المخصصون، الإشعارات، البانر، والنسخ الاحتياطي JSON.
- `src/data/quranData.ts`: بيانات السور الـ 114 والقراء الافتراضيين.

## التشغيل والبناء
```bash
npm install
npm run dev
npm run build
npm run lint
```
