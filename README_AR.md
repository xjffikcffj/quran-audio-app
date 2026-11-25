# قرآن صوتي — README

## عن المشروع
تطبيق Android مبني باستخدام Kotlin وJetpack Compose لتصفح سور القرآن الكريم وتشغيل التلاوات، مع مشغل خلفي ومؤقت نوم واختيار القراء والمفضلة وتخصيص المظهر.

## بنية مهمة
- `app/src/main/java/com/example/viewmodel/AudioPlayerViewModel.kt`: حالة واجهة المشغل والتحكم بالتلاوة.
- `app/src/main/java/com/example/service/QuranPlaybackService.kt`: خدمة جلسة الوسائط والتشغيل الخلفي.
- `app/src/main/java/com/example/timer/SleepTimerManager.kt`: مؤقت النوم.
- `app/src/main/java/com/example/data/QuranData.kt`: بيانات السور والقراء الافتراضيين.
- `app/src/main/java/com/example/data/DeveloperContentManager.kt`: إعدادات لوحة المطور والقراء المخصصون والنسخ الاحتياطي JSON.
- `app/src/main/java/com/example/model/BookmarkManager.kt`: تخزين العلامات المرجعية محليًا؛ يلزم ربطه بواجهة المستخدم في عمل لاحق.
- `app/src/test/`: اختبارات الوحدة وRobolectric.
- `.github/workflows/build-apk.yml`: سير اختبارات الوحدة وبناء APK للفرع `timer-countdown`.

## البناء والاختبار
في بيئة تحتوي على JDK 17 وGradle 9.3.1 وAndroid SDK المتوافق مع `compileSdk` في المشروع:

```bash
gradle :app:testDebugUnitTest
gradle :app:assembleDebug
```

سير GitHub Actions يثبت Gradle ثم يشغل الاختبارات قبل بناء APK. الأرشيف المرفق بهذه المراجعة لا يحتوي على `gradlew` أو `gradle-wrapper.jar`، لذا استخدم بيئة توفر أمر `gradle` أو أضف ملفات Gradle Wrapper الرسمية إلى المستودع لاحقًا.

## ملاحظات مهمة قبل الإصدار العام
- معرفات Google Mobile Ads الموجودة حاليًا هي معرفات اختبار رسمية؛ يلزم استبدالها بمعرفات التطبيق والوحدات الإعلانية الحقيقية قبل إصدار إنتاجي.
- تسجيل دخول المطور محلي داخل التطبيق، وليس تفويضًا آمنًا من خادم؛ لا تعتمد عليه لحماية عمليات سحابية حساسة.
- الإشعارات من لوحة المطور محلية على الجهاز؛ إرسال إشعارات إلى جميع المستخدمين يحتاج خدمة دفع مثل Firebase Cloud Messaging وإعداداتها.
- خيار تذكير الجمعة محفوظ كإعداد، لكنه يحتاج إلى جدولة فعلية.
- اختبارات تشغيل الصوت على جهاز فعلي، واستمراره في الخلفية، والإشعارات، ومؤقت النوم ما زالت مطلوبة بعد اجتياز الاختبارات الآلية.

## حالة هذه النسخة
هذه نسخة عمل مستخرجة من فرع `timer-countdown` ومعدلة محليًا لأغراض المراجعة. لم يتم دفع التغييرات إلى GitHub من خلال هذه العملية.
