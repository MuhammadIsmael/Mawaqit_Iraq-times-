package com.example.data.calculator

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.floor

data class HijriDateResult(
    val hijriDay: Int,
    val hijriMonthNumber: Int,
    val hijriMonthNameEn: String,
    val hijriMonthNameAr: String,
    val hijriYear: Int,
    val formattedEn: String,
    val formattedAr: String
)

object HijriCalendarHelper {

    private val HIJRI_MONTHS_EN = listOf(
        "Muharram", "Safar", "Rabi Al-Awwal", "Rabi Al-Thani",
        "Jumada Al-Awwal", "Jumada Al-Thani", "Rajab", "Sha'ban",
        "Ramadan", "Shawwal", "Dhu Al-Qi'dah", "Dhu Al-Hijjah"
    )

    private val HIJRI_MONTHS_AR = listOf(
        "محرم", "صفر", "ربيع الأول", "ربيع الثاني",
        "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
        "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    )

    private val DAYS_OF_WEEK_EN = listOf(
        "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    )

    private val DAYS_OF_WEEK_AR = listOf(
        "الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت"
    )

    /**
     * Kuwati / Umm Al-Qura algorithm for accurate Hijri conversion
     */
    fun getHijriDate(calendar: Calendar = Calendar.getInstance(), adjustmentDays: Int = 0): HijriDateResult {
        val cal = calendar.clone() as Calendar
        if (adjustmentDays != 0) {
            cal.add(Calendar.DAY_OF_MONTH, adjustmentDays)
        }

        val day = cal.get(Calendar.DAY_OF_MONTH)
        var month = cal.get(Calendar.MONTH) // 0-indexed
        var year = cal.get(Calendar.YEAR)

        var m = month + 1
        var y = year
        if (m < 3) {
            y -= 1
            m += 12
        }

        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        val jd = floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5

        val z = jd - 1948440 + 10632
        val n = floor((z - 1) / 10631.0)
        val z1 = z - 10631 * n + 354
        val j = (floor((10985 - z1) / 5316.0)) * (floor((50 * z1) / 17719.0)) + (floor(z1 / 5670.0)) * (floor((43 * z1) / 15238.0))
        val z2 = z1 - (floor((30 - j) / 15.0)) * (floor((17719 * j) / 50.0)) - (floor(j / 16.0)) * (floor((15238 * j) / 43.0)) + 29
        val hMonth = floor((24 * z2) / 709.0).toInt()
        val hDay = (z2 - floor((709 * hMonth) / 24.0)).toInt()
        val hYear = (30 * n + j - 30).toInt()

        val normalizedMonthIndex = ((hMonth - 1).coerceIn(0, 11))
        val monthEn = HIJRI_MONTHS_EN[normalizedMonthIndex]
        val monthAr = HIJRI_MONTHS_AR[normalizedMonthIndex]

        val formattedEn = "$hDay $monthEn, $hYear"
        val formattedAr = "$hDay $monthAr $hYear هـ"

        return HijriDateResult(
            hijriDay = hDay,
            hijriMonthNumber = normalizedMonthIndex + 1,
            hijriMonthNameEn = monthEn,
            hijriMonthNameAr = monthAr,
            hijriYear = hYear,
            formattedEn = formattedEn,
            formattedAr = formattedAr
        )
    }

    /**
     * Format full banner date like in the screenshot:
     * "15 August, 2026 — Saturday — 2 Rabi Al-Awwal, 1448"
     */
    fun getFullFormattedDate(calendar: Calendar = Calendar.getInstance(), adjustmentDays: Int = 0): String {
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val monthFormat = SimpleDateFormat("MMMM", Locale.ENGLISH)
        val monthName = monthFormat.format(calendar.time)
        val year = calendar.get(Calendar.YEAR)

        val dayOfWeekIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1
        val dayOfWeekName = DAYS_OF_WEEK_EN[dayOfWeekIndex]

        val hijri = getHijriDate(calendar, adjustmentDays)

        return "$day $monthName, $year — $dayOfWeekName — ${hijri.formattedEn}"
    }

    fun getFullFormattedDateArabic(calendar: Calendar = Calendar.getInstance(), adjustmentDays: Int = 0): String {
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val monthFormat = SimpleDateFormat("MMMM", Locale("ar"))
        val monthName = monthFormat.format(calendar.time)
        val year = calendar.get(Calendar.YEAR)

        val dayOfWeekIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1
        val dayOfWeekName = DAYS_OF_WEEK_AR[dayOfWeekIndex]

        val hijri = getHijriDate(calendar, adjustmentDays)

        return "$day $monthName $year م — $dayOfWeekName — ${hijri.formattedAr}"
    }
}
