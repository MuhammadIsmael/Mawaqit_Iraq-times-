package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.data.models.AthanVoice
import com.example.data.models.CalculationMethod
import com.example.data.models.ColorThemePref
import com.example.data.models.IRAQI_CITIES
import com.example.data.models.IraqiCity
import com.example.data.models.JuristicSchool
import com.example.data.models.ScreenOrientationPref
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppSettings(
    val mosqueName: String = "جامع الرحمن",
    val cityName: String = "بغداد - العراق",
    val selectedCityIndex: Int = 0, // 0 = Baghdad
    val calculationMethod: CalculationMethod = CalculationMethod.IRAQI_SUNNI,
    val juristicSchool: JuristicSchool = JuristicSchool.SHAFI_HANBALI_MALIKI,
    val fajrIqamaMin: Int = 25,
    val duhrIqamaMin: Int = 12,
    val asrIqamaMin: Int = 12,
    val maghribIqamaMin: Int = 8,
    val ishaIqamaMin: Int = 12,
    val fajrOffsetMin: Int = 0,
    val shurukOffsetMin: Int = 0,
    val duhrOffsetMin: Int = 0,
    val asrOffsetMin: Int = 0,
    val maghribOffsetMin: Int = 0,
    val ishaOffsetMin: Int = 0,
    val orientationPref: ScreenOrientationPref = ScreenOrientationPref.LANDSCAPE,
    val colorTheme: ColorThemePref = ColorThemePref.GOLD_YELLOW,
    val athanVoice: AthanVoice = AthanVoice.BAGHDAD,
    val athanVolumePercent: Int = 90,
    val hijriAdjustmentDays: Int = 0,
    val is24HourFormat: Boolean = false,
    val showSeconds: Boolean = true,
    val mosqueId: String = "34383",
    val announcementTicker: String = "قال رسول الله ﷺ: «الصلاة عماد الدين» — يرجى إغلاق الهواتف النقالة أثناء إقامة الصلاة",
    val isAthanAlertPopupEnabled: Boolean = true
) {
    val currentCity: IraqiCity
        get() = IRAQI_CITIES.getOrElse(selectedCityIndex) { IRAQI_CITIES[0] }
}

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("mawaqit_iraq_prefs", Context.MODE_PRIVATE)

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<AppSettings> = _settingsFlow.asStateFlow()

    private fun loadSettings(): AppSettings {
        val orientationOrdinal = prefs.getInt("orientation_pref", ScreenOrientationPref.LANDSCAPE.ordinal)
        val orientation = ScreenOrientationPref.entries.getOrElse(orientationOrdinal) { ScreenOrientationPref.LANDSCAPE }

        val methodOrdinal = prefs.getInt("calc_method", CalculationMethod.IRAQI_SUNNI.ordinal)
        val method = CalculationMethod.entries.getOrElse(methodOrdinal) { CalculationMethod.IRAQI_SUNNI }

        val juristicOrdinal = prefs.getInt("juristic_school", JuristicSchool.SHAFI_HANBALI_MALIKI.ordinal)
        val juristic = JuristicSchool.entries.getOrElse(juristicOrdinal) { JuristicSchool.SHAFI_HANBALI_MALIKI }

        val themeOrdinal = prefs.getInt("color_theme", ColorThemePref.GOLD_YELLOW.ordinal)
        val theme = ColorThemePref.entries.getOrElse(themeOrdinal) { ColorThemePref.GOLD_YELLOW }

        val voiceOrdinal = prefs.getInt("athan_voice", AthanVoice.BAGHDAD.ordinal)
        val voice = AthanVoice.entries.getOrElse(voiceOrdinal) { AthanVoice.BAGHDAD }

        return AppSettings(
            mosqueName = prefs.getString("mosque_name", "Mosque Name") ?: "Mosque Name",
            cityName = prefs.getString("city_name", "City Name") ?: "City Name",
            selectedCityIndex = prefs.getInt("selected_city_idx", 0),
            calculationMethod = method,
            juristicSchool = juristic,
            fajrIqamaMin = prefs.getInt("fajr_iqama", 25),
            duhrIqamaMin = prefs.getInt("duhr_iqama", 12),
            asrIqamaMin = prefs.getInt("asr_iqama", 12),
            maghribIqamaMin = prefs.getInt("maghrib_iqama", 8),
            ishaIqamaMin = prefs.getInt("isha_iqama", 12),
            fajrOffsetMin = prefs.getInt("fajr_offset", 0),
            shurukOffsetMin = prefs.getInt("shuruk_offset", 0),
            duhrOffsetMin = prefs.getInt("duhr_offset", 0),
            asrOffsetMin = prefs.getInt("asr_offset", 0),
            maghribOffsetMin = prefs.getInt("maghrib_offset", 0),
            ishaOffsetMin = prefs.getInt("isha_offset", 0),
            orientationPref = orientation,
            colorTheme = theme,
            athanVoice = voice,
            athanVolumePercent = prefs.getInt("athan_volume", 90),
            hijriAdjustmentDays = prefs.getInt("hijri_adj", 0),
            is24HourFormat = prefs.getBoolean("is_24h", false),
            showSeconds = prefs.getBoolean("show_seconds", true),
            mosqueId = prefs.getString("mosque_id", "34383") ?: "34383",
            announcementTicker = prefs.getString("ticker_text", "قال رسول الله ﷺ: «الصلاة عماد الدين» — يرجى إغلاق الهواتف أثناء الصلاة") ?: "",
            isAthanAlertPopupEnabled = prefs.getBoolean("athan_alert_enabled", true)
        )
    }

    fun updateSettings(newSettings: AppSettings) {
        prefs.edit().apply {
            putString("mosque_name", newSettings.mosqueName)
            putString("city_name", newSettings.cityName)
            putInt("selected_city_idx", newSettings.selectedCityIndex)
            putInt("calc_method", newSettings.calculationMethod.ordinal)
            putInt("juristic_school", newSettings.juristicSchool.ordinal)
            putInt("fajr_iqama", newSettings.fajrIqamaMin)
            putInt("duhr_iqama", newSettings.duhrIqamaMin)
            putInt("asr_iqama", newSettings.asrIqamaMin)
            putInt("maghrib_iqama", newSettings.maghribIqamaMin)
            putInt("isha_iqama", newSettings.ishaIqamaMin)
            putInt("fajr_offset", newSettings.fajrOffsetMin)
            putInt("shuruk_offset", newSettings.shurukOffsetMin)
            putInt("duhr_offset", newSettings.duhrOffsetMin)
            putInt("asr_offset", newSettings.asrOffsetMin)
            putInt("maghrib_offset", newSettings.maghribOffsetMin)
            putInt("isha_offset", newSettings.ishaOffsetMin)
            putInt("orientation_pref", newSettings.orientationPref.ordinal)
            putInt("color_theme", newSettings.colorTheme.ordinal)
            putInt("athan_voice", newSettings.athanVoice.ordinal)
            putInt("athan_volume", newSettings.athanVolumePercent)
            putInt("hijri_adj", newSettings.hijriAdjustmentDays)
            putBoolean("is_24h", newSettings.is24HourFormat)
            putBoolean("show_seconds", newSettings.showSeconds)
            putString("mosque_id", newSettings.mosqueId)
            putString("ticker_text", newSettings.announcementTicker)
            putBoolean("athan_alert_enabled", newSettings.isAthanAlertPopupEnabled)
            apply()
        }
        _settingsFlow.value = newSettings
    }
}
