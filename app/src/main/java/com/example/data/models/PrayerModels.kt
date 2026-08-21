package com.example.data.models

enum class PrayerType(val displayNameEn: String, val displayNameAr: String) {
    FAJR("Fajr", "الفجر"),
    SHURUK("Shuruk", "الشروق"),
    DUHR("Duhr", "الظهر"),
    ASR("Asr", "العصر"),
    MAGHRIB("Maghrib", "المغرب"),
    ISHA("Isha", "العشاء"),
    JUMUA("Jumua", "الجمعة")
}

data class PrayerTimeItem(
    val type: PrayerType,
    val nameEn: String,
    val nameAr: String,
    val timeFormatted: String, // e.g. "03:51 AM"
    val hour24: Int,
    val minute: Int,
    val iqamaOffsetMinutes: Int, // e.g. 25
    val iqamaTimeFormatted: String, // e.g. "04:16 AM"
    val isNext: Boolean = false,
    val isCurrent: Boolean = false
)

enum class CalculationMethod(val titleEn: String, val titleAr: String, val fajrAngle: Double, val ishaAngle: Double, val maghribMinutes: Int) {
    IRAQI_SUNNI("Iraq Sunni Endowment", "ديوان الوقف السني العراقي", 18.0, 17.5, 0),
    IRAQI_SHIA("Iraq Shia Endowment / Leva", "مركز الدراسات والبحوث الإسلامية / الشيعي", 18.0, 14.0, 4),
    EGYPTIAN("Egyptian General Authority", "الهيئة المصرية العامة للمساحة", 19.5, 17.5, 0),
    UMM_AL_QURA("Umm Al-Qura (Makkah)", "جامعة أم القرى - مكة المكرمة", 18.5, 18.5, 0),
    MUSLIM_WORLD_LEAGUE("Muslim World League", "رابطة العالم الإسلامي", 18.0, 17.0, 0)
}

enum class JuristicSchool(val titleEn: String, val titleAr: String) {
    SHAFI_HANBALI_MALIKI("Standard (Shafi'i, Maliki, Hanbali, Ja'fari)", "الجمهور (شافعي، مالكي، حنبلي، جعفري)"),
    HANAFI("Hanafi (Later Asr)", "الحنفي (العصر المتأخر)")
}

data class IraqiCity(
    val nameEn: String,
    val nameAr: String,
    val latitude: Double,
    val longitude: Double,
    val timeZoneOffsetHours: Double = 3.0
)

val IRAQI_CITIES = listOf(
    IraqiCity("Baghdad", "بغداد", 33.3152, 44.3661),
    IraqiCity("Najaf", "النجف الأشرف", 32.0000, 44.3333),
    IraqiCity("Karbala", "كربلاء المقدسة", 32.6160, 44.0249),
    IraqiCity("Basra", "البصرة الفيحاء", 30.5081, 47.7835),
    IraqiCity("Erbil", "أربيل (هەولێر)", 36.1911, 44.0092),
    IraqiCity("Mosul", "الموصل (نينوى)", 36.3400, 43.1300),
    IraqiCity("Sulaymaniyah", "السليمانية (سلێمانی)", 35.5650, 45.4330),
    IraqiCity("Kirkuk", "كركوك", 35.4681, 44.3922),
    IraqiCity("Nasiriyah", "الناصرية (ذي قار)", 31.0439, 46.2573),
    IraqiCity("Hillah", "الحلة (بابل)", 32.4820, 44.4340),
    IraqiCity("Amarah", "العمارة (ميسان)", 31.8440, 47.1440),
    IraqiCity("Diwaniyah", "الديوانية (القادسية)", 31.9922, 44.9250),
    IraqiCity("Ramadi", "الرمادي (الأنبار)", 33.4241, 43.2980),
    IraqiCity("Samarra", "سامراء (صلاح الدين)", 34.1983, 43.8742),
    IraqiCity("Kut", "الكوت (واسط)", 32.5060, 45.8200),
    IraqiCity("Dohuk", "دهوك (دهۆك)", 36.8679, 42.9885),
    IraqiCity("Baqubah", "بعقوبة (ديالى)", 33.7439, 44.6461),
    IraqiCity("Samawah", "السماوة (المثنى)", 31.3140, 45.2810)
)

enum class ScreenOrientationPref(val titleEn: String, val titleAr: String) {
    LANDSCAPE("Landscape (16:9 Horizontal TV)", "أفقي (شاشات التلفزيون 16:9)"),
    PORTRAIT("Portrait (9:16 Vertical Pillar)", "عمودي (شاشات الأعمدة 9:16)"),
    AUTO("Auto / Sensor", "تلقائي حسب الجهاز")
}

enum class ColorThemePref(val titleEn: String, val primaryHex: Long) {
    GOLD_YELLOW("Golden Yellow (Default Mawaqit)", 0xFFFFB703),
    EMERALD_GREEN("Islamic Emerald Green", 0xFF10B981),
    ROYAL_BLUE("Mosque Royal Blue", 0xFF0284C7),
    AMBER_DARK("Deep Amber OLED", 0xFFF59E0B)
}

enum class AthanVoice(val titleEn: String, val titleAr: String) {
    BAGHDAD("Iraqi Maqam (Baghdad)", "المقام العراقي (بغداد)"),
    MAKKAH("Makkah Al-Mukarramah", "الحرم المكي الشريف"),
    MADINAH("Madinah Al-Munawwarah", "المسجد النبوي الشريف"),
    AL_AQSA("Al-Aqsa Mosque (Jerusalem)", "المسجد الأقصى المبارك"),
    BEEP_ONLY("Gentle Tone / Beep Only", "نغمة هادئة فقط"),
    SILENT("Silent (Visual Notice Only)", "صامت (تنبيه مرئي فقط)")
}

data class LiveStreamChannel(
    val id: String,
    val titleEn: String,
    val titleAr: String,
    val streamUrl: String,
    val backupUrl: String,
    val location: String
)

val LIVE_STREAM_CHANNELS = listOf(
    LiveStreamChannel(
        id = "makkah",
        titleEn = "Holy Kaaba Live",
        titleAr = "بث مباشر - الكعبة المشرفة ومكة المكرمة",
        streamUrl = "https://win.holylive.net:1936/live/makkahtv/playlist.m3u8",
        backupUrl = "https://stream.holyquran.net/makkah.mp4",
        location = "Makkah Al-Mukarramah"
    ),
    LiveStreamChannel(
        id = "madinah",
        titleEn = "Prophet's Mosque Live",
        titleAr = "بث مباشر - المسجد النبوي الشريف",
        streamUrl = "https://win.holylive.net:1936/live/madinahtv/playlist.m3u8",
        backupUrl = "https://stream.holyquran.net/madinah.mp4",
        location = "Al-Madinah Al-Munawwarah"
    ),
    LiveStreamChannel(
        id = "najaf",
        titleEn = "Imam Ali Holy Shrine Live",
        titleAr = "بث مباشر - العتبة العلوية المقدسة (النجف الأشرف)",
        streamUrl = "https://live.imamali.net/hls/live.m3u8",
        backupUrl = "https://stream.holyquran.net/najaf.mp4",
        location = "Najaf Al-Ashraf, Iraq"
    ),
    LiveStreamChannel(
        id = "karbala_hussain",
        titleEn = "Imam Hussain Holy Shrine Live",
        titleAr = "بث مباشر - العتبة الحسينية المقدسة (كربلاء)",
        streamUrl = "https://live.imamhussain.org/hls/stream.m3u8",
        backupUrl = "https://stream.holyquran.net/karbala.mp4",
        location = "Karbala Holy City, Iraq"
    ),
    LiveStreamChannel(
        id = "karbala_abbas",
        titleEn = "Al-Abbas Holy Shrine Live",
        titleAr = "بث مباشر - العتبة العباسية المقدسة (كربلاء)",
        streamUrl = "https://alkafeel.net/live/hls/alkafeel.m3u8",
        backupUrl = "https://stream.holyquran.net/abbas.mp4",
        location = "Karbala Holy City, Iraq"
    ),
    LiveStreamChannel(
        id = "kadhimiya",
        titleEn = "Al-Kadhimiya Holy Shrine Live",
        titleAr = "بث مباشر - العتبة الكاظمية المقدسة (بغداد)",
        streamUrl = "https://live.al-kadhimiya.org/hls/stream.m3u8",
        backupUrl = "https://stream.holyquran.net/kadhimiya.mp4",
        location = "Baghdad, Iraq"
    ),
    LiveStreamChannel(
        id = "iraq_quran",
        titleEn = "Holy Quran Channel Iraq",
        titleAr = "قناة القرآن الكريم - العراق",
        streamUrl = "https://stream.holyquran.net/iraq_quran.m3u8",
        backupUrl = "https://stream.holyquran.net/recitation.mp4",
        location = "Baghdad, Iraq"
    )
)
