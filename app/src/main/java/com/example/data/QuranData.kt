package com.example.data

import com.example.model.Reciter
import com.example.model.Surah

object QuranData {

    private fun defaultAudioUrl(surahNumber: Int): String {
        return String.format("https://server8.mp3quran.net/afs/%03d.mp3", surahNumber)
    }

    val reciters = listOf(
        Reciter(
            id = "afs",
            nameArabic = "مشاري بن راشد العفاسي",
            riwayah = "حفص عن عاصم",
            serverBaseUrl = "https://server8.mp3quran.net/afs/"
        ),
        Reciter(
            id = "basit",
            nameArabic = "عبد الباسط عبد الصمد",
            riwayah = "مرتل - حفص عن عاصم",
            serverBaseUrl = "https://server7.mp3quran.net/basit/"
        ),
        Reciter(
            id = "maher",
            nameArabic = "ماهر المعيقلي",
            riwayah = "حفص عن عاصم",
            serverBaseUrl = "https://server12.mp3quran.net/maher/"
        ),
        Reciter(
            id = "s_gmd",
            nameArabic = "سعد الغامدي",
            riwayah = "حفص عن عاصم",
            serverBaseUrl = "https://server7.mp3quran.net/s_gmd/"
        ),
        Reciter(
            id = "husr",
            nameArabic = "محمود خليل الحصري",
            riwayah = "حفص عن عاصم",
            serverBaseUrl = "https://server13.mp3quran.net/husr/"
        ),
        Reciter(
            id = "sds",
            nameArabic = "عبد الرحمن السديس",
            riwayah = "حفص عن عاصم",
            serverBaseUrl = "https://server11.mp3quran.net/sds/"
        )
    )

    /**
     * قائمة ثابتة بجميع سور القرآن الكريم الـ 114 مرتبة من الفاتحة إلى الناس
     * تحتوي كل سورة على: رقم السورة، اسمها، عدد آياتها، ورابط ملف الصوت المباشر
     */
    val surahs: List<Surah> = listOf(
        Surah(1, "الفاتحة", 7, defaultAudioUrl(1), "Al-Fatihah", true),
        Surah(2, "البقرة", 286, defaultAudioUrl(2), "Al-Baqarah", false),
        Surah(3, "آل عمران", 200, defaultAudioUrl(3), "Ali 'Imran", false),
        Surah(4, "النساء", 176, defaultAudioUrl(4), "An-Nisa", false),
        Surah(5, "المائدة", 120, defaultAudioUrl(5), "Al-Ma'idah", false),
        Surah(6, "الأنعام", 165, defaultAudioUrl(6), "Al-An'am", true),
        Surah(7, "الأعراف", 206, defaultAudioUrl(7), "Al-A'raf", true),
        Surah(8, "الأنفال", 75, defaultAudioUrl(8), "Al-Anfal", false),
        Surah(9, "التوبة", 129, defaultAudioUrl(9), "At-Tawbah", false),
        Surah(10, "يونس", 109, defaultAudioUrl(10), "Yunus", true),
        Surah(11, "هود", 123, defaultAudioUrl(11), "Hud", true),
        Surah(12, "يوسف", 111, defaultAudioUrl(12), "Yusuf", true),
        Surah(13, "الرعد", 43, defaultAudioUrl(13), "Ar-Ra'd", false),
        Surah(14, "إبراهيم", 52, defaultAudioUrl(14), "Ibrahim", true),
        Surah(15, "الحجر", 99, defaultAudioUrl(15), "Al-Hijr", true),
        Surah(16, "النحل", 128, defaultAudioUrl(16), "An-Nahl", true),
        Surah(17, "الإسراء", 111, defaultAudioUrl(17), "Al-Isra", true),
        Surah(18, "الكهف", 110, defaultAudioUrl(18), "Al-Kahf", true),
        Surah(19, "مريم", 98, defaultAudioUrl(19), "Maryam", true),
        Surah(20, "طه", 135, defaultAudioUrl(20), "Ta-Ha", true),
        Surah(21, "الأنبياء", 112, defaultAudioUrl(21), "Al-Anbiya", true),
        Surah(22, "الحج", 78, defaultAudioUrl(22), "Al-Hajj", false),
        Surah(23, "المؤمنون", 118, defaultAudioUrl(23), "Al-Mu'minun", true),
        Surah(24, "النور", 64, defaultAudioUrl(24), "An-Nur", false),
        Surah(25, "الفرقان", 77, defaultAudioUrl(25), "Al-Furqan", true),
        Surah(26, "الشعراء", 227, defaultAudioUrl(26), "Ash-Shu'ara", true),
        Surah(27, "النمل", 93, defaultAudioUrl(27), "An-Naml", true),
        Surah(28, "القصص", 88, defaultAudioUrl(28), "Al-Qasas", true),
        Surah(29, "العنكبوت", 69, defaultAudioUrl(29), "Al-'Ankabut", true),
        Surah(30, "الروم", 60, defaultAudioUrl(30), "Ar-Rum", true),
        Surah(31, "لقمان", 34, defaultAudioUrl(31), "Luqman", true),
        Surah(32, "السجدة", 30, defaultAudioUrl(32), "As-Sajdah", true),
        Surah(33, "الأحزاب", 73, defaultAudioUrl(33), "Al-Ahzab", false),
        Surah(34, "سبأ", 54, defaultAudioUrl(34), "Saba", true),
        Surah(35, "فاطر", 45, defaultAudioUrl(35), "Fatir", true),
        Surah(36, "يس", 83, defaultAudioUrl(36), "Ya-Sin", true),
        Surah(37, "الصافات", 182, defaultAudioUrl(37), "As-Saffat", true),
        Surah(38, "ص", 88, defaultAudioUrl(38), "Sad", true),
        Surah(39, "الزمر", 75, defaultAudioUrl(39), "Az-Zumar", true),
        Surah(40, "غافر", 85, defaultAudioUrl(40), "Ghafir", true),
        Surah(41, "فصلت", 54, defaultAudioUrl(41), "Fussilat", true),
        Surah(42, "الشورى", 53, defaultAudioUrl(42), "Ash-Shura", true),
        Surah(43, "الزخرف", 89, defaultAudioUrl(43), "Az-Zukhruf", true),
        Surah(44, "الدخان", 59, defaultAudioUrl(44), "Ad-Dukhan", true),
        Surah(45, "الجاثية", 37, defaultAudioUrl(45), "Al-Jathiyah", true),
        Surah(46, "الأحقاف", 35, defaultAudioUrl(46), "Al-Ahqaf", true),
        Surah(47, "محمد", 38, defaultAudioUrl(47), "Muhammad", false),
        Surah(48, "الفتح", 29, defaultAudioUrl(48), "Al-Fath", false),
        Surah(49, "الحجرات", 18, defaultAudioUrl(49), "Al-Hujurat", false),
        Surah(50, "ق", 45, defaultAudioUrl(50), "Qaf", true),
        Surah(51, "الذاريات", 60, defaultAudioUrl(51), "Adh-Dhariyat", true),
        Surah(52, "الطور", 49, defaultAudioUrl(52), "At-Tur", true),
        Surah(53, "النجم", 62, defaultAudioUrl(53), "An-Najm", true),
        Surah(54, "القمر", 55, defaultAudioUrl(54), "Al-Qamar", true),
        Surah(55, "الرحمن", 78, defaultAudioUrl(55), "Ar-Rahman", false),
        Surah(56, "الواقعة", 96, defaultAudioUrl(56), "Al-Waqi'ah", true),
        Surah(57, "الحديد", 29, defaultAudioUrl(57), "Al-Hadid", false),
        Surah(58, "المجادلة", 22, defaultAudioUrl(58), "Al-Mujadila", false),
        Surah(59, "الحشر", 24, defaultAudioUrl(59), "Al-Hashr", false),
        Surah(60, "الممتحنة", 13, defaultAudioUrl(60), "Al-Mumtahanah", false),
        Surah(61, "الصف", 14, defaultAudioUrl(61), "As-Saff", false),
        Surah(62, "الجمعة", 11, defaultAudioUrl(62), "Al-Jumu'ah", false),
        Surah(63, "المنافقون", 11, defaultAudioUrl(63), "Al-Munafiqun", false),
        Surah(64, "التغابن", 18, defaultAudioUrl(64), "At-Taghabun", false),
        Surah(65, "الطلاق", 12, defaultAudioUrl(65), "At-Talaq", false),
        Surah(66, "التحريم", 12, defaultAudioUrl(66), "At-Tahrim", false),
        Surah(67, "الملك", 30, defaultAudioUrl(67), "Al-Mulk", true),
        Surah(68, "القلم", 52, defaultAudioUrl(68), "Al-Qalam", true),
        Surah(69, "الحاقة", 52, defaultAudioUrl(69), "Al-Haqqah", true),
        Surah(70, "المعارج", 44, defaultAudioUrl(70), "Al-Ma'arij", true),
        Surah(71, "نوح", 28, defaultAudioUrl(71), "Nuh", true),
        Surah(72, "الجن", 28, defaultAudioUrl(72), "Al-Jinn", true),
        Surah(73, "المزمل", 20, defaultAudioUrl(73), "Al-Muzzammil", true),
        Surah(74, "المدثر", 56, defaultAudioUrl(74), "Al-Muddaththir", true),
        Surah(75, "القيامة", 40, defaultAudioUrl(75), "Al-Qiyamah", true),
        Surah(76, "الإنسان", 31, defaultAudioUrl(76), "Al-Insan", false),
        Surah(77, "المرسلات", 50, defaultAudioUrl(77), "Al-Mursalat", true),
        Surah(78, "النبأ", 40, defaultAudioUrl(78), "An-Naba", true),
        Surah(79, "النازعات", 46, defaultAudioUrl(79), "An-Nazi'at", true),
        Surah(80, "عبس", 42, defaultAudioUrl(80), "'Abasa", true),
        Surah(81, "التكوير", 29, defaultAudioUrl(81), "At-Takwir", true),
        Surah(82, "الانفطار", 19, defaultAudioUrl(82), "Al-Infitar", true),
        Surah(83, "المطففين", 36, defaultAudioUrl(83), "Al-Mutaffifin", true),
        Surah(84, "الانشقاق", 25, defaultAudioUrl(84), "Al-Inshiqaq", true),
        Surah(85, "البروج", 22, defaultAudioUrl(85), "Al-Buruj", true),
        Surah(86, "الطارق", 17, defaultAudioUrl(86), "At-Tariq", true),
        Surah(87, "الأعلى", 19, defaultAudioUrl(87), "Al-A'la", true),
        Surah(88, "الغاشية", 26, defaultAudioUrl(88), "Al-Ghashiyah", true),
        Surah(89, "الفجر", 30, defaultAudioUrl(89), "Al-Fajr", true),
        Surah(90, "البلد", 20, defaultAudioUrl(90), "Al-Balad", true),
        Surah(91, "الشمس", 15, defaultAudioUrl(91), "Ash-Shams", true),
        Surah(92, "الليل", 21, defaultAudioUrl(92), "Al-Layl", true),
        Surah(93, "الضحى", 11, defaultAudioUrl(93), "Ad-Duha", true),
        Surah(94, "الشرح", 8, defaultAudioUrl(94), "Ash-Sharh", true),
        Surah(95, "التين", 8, defaultAudioUrl(95), "At-Tin", true),
        Surah(96, "العلق", 19, defaultAudioUrl(96), "Al-'Alaq", true),
        Surah(97, "القدر", 5, defaultAudioUrl(97), "Al-Qadr", true),
        Surah(98, "البينة", 8, defaultAudioUrl(98), "Al-Bayyinah", false),
        Surah(99, "الزلزلة", 8, defaultAudioUrl(99), "Az-Zalzalah", false),
        Surah(100, "العاديات", 11, defaultAudioUrl(100), "Al-'Adiyat", true),
        Surah(101, "القارعة", 11, defaultAudioUrl(101), "Al-Qari'ah", true),
        Surah(102, "التكاثر", 8, defaultAudioUrl(102), "At-Takathur", true),
        Surah(103, "العصر", 3, defaultAudioUrl(103), "Al-'Asr", true),
        Surah(104, "الهمزة", 9, defaultAudioUrl(104), "Al-Humazah", true),
        Surah(105, "الفيل", 5, defaultAudioUrl(105), "Al-Fil", true),
        Surah(106, "قريش", 4, defaultAudioUrl(106), "Quraysh", true),
        Surah(107, "الماعون", 7, defaultAudioUrl(107), "Al-Ma'un", true),
        Surah(108, "الكوثر", 3, defaultAudioUrl(108), "Al-Kawthar", true),
        Surah(109, "الكافرون", 6, defaultAudioUrl(109), "Al-Kafirun", true),
        Surah(110, "النصر", 3, defaultAudioUrl(110), "An-Nasr", false),
        Surah(111, "المسد", 5, defaultAudioUrl(111), "Al-Masad", true),
        Surah(112, "الإخلاص", 4, defaultAudioUrl(112), "Al-Ikhlas", true),
        Surah(113, "الفلق", 5, defaultAudioUrl(113), "Al-Falaq", true),
        Surah(114, "الناس", 6, defaultAudioUrl(114), "An-Nas", true)
    )
}
