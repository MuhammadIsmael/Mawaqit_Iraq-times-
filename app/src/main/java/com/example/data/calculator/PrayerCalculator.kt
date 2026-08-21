package com.example.data.calculator

import com.example.data.models.CalculationMethod
import com.example.data.models.IraqiCity
import com.example.data.models.JuristicSchool
import com.example.data.models.PrayerTimeItem
import com.example.data.models.PrayerType
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

data class DailyPrayerSchedule(
    val fajr: PrayerTimeItem,
    val shuruk: PrayerTimeItem,
    val duhr: PrayerTimeItem,
    val asr: PrayerTimeItem,
    val maghrib: PrayerTimeItem,
    val isha: PrayerTimeItem,
    val jumua: PrayerTimeItem,
    val nextPrayer: PrayerTimeItem,
    val secondsUntilNextAthan: Long,
    val secondsUntilNextIqama: Long,
    val isIqamaWindow: Boolean // true if current time is between Athan and Iqama
)

object PrayerCalculator {

    private fun degToRad(deg: Double): Double = deg * Math.PI / 180.0
    private fun radToDeg(rad: Double): Double = rad * 180.0 / Math.PI

    private fun fixHour(hour: Double): Double {
        var h = hour - 24.0 * floor(hour / 24.0)
        if (h < 0) h += 24.0
        return h
    }

    /**
     * Compute Julian Day from Gregorian date
     */
    private fun getJulianDay(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    /**
     * Sun declination and equation of time
     */
    private fun sunPosition(jd: Double): Pair<Double, Double> {
        val d = jd - 2451545.0
        val g = fixHour(357.529 + 0.98560028 * d)
        val q = fixHour(280.459 + 0.98564736 * d)
        val l = fixHour(q + 1.915 * sin(degToRad(g)) + 0.020 * sin(degToRad(2 * g)))

        val e = 23.439 - 0.00000036 * d
        val ra = radToDeg(atan2(cos(degToRad(e)) * sin(degToRad(l)), cos(degToRad(l)))) / 15.0
        val rightAscension = fixHour(ra)
        val declination = radToDeg(asin(sin(degToRad(e)) * sin(degToRad(l))))
        val equationOfTime = q / 15.0 - rightAscension
        return Pair(declination, equationOfTime)
    }

    /**
     * Calculate Midday (Zawal / Solar Noon)
     */
    private fun computeMidDay(timeZone: Double, longitude: Double, equationOfTime: Double): Double {
        return fixHour(12.0 + timeZone - longitude / 15.0 - equationOfTime)
    }

    /**
     * Calculate sun angle time difference (Hour Angle)
     */
    private fun computeHourAngle(angle: Double, latitude: Double, declination: Double): Double {
        val latRad = degToRad(latitude)
        val decRad = degToRad(declination)
        val top = -sin(degToRad(angle)) - sin(latRad) * sin(decRad)
        val bottom = cos(latRad) * cos(decRad)
        val cosH = top / bottom
        if (cosH > 1.0 || cosH < -1.0) {
            return 0.0
        }
        return radToDeg(acos(cosH)) / 15.0
    }

    /**
     * Calculate Asr hour angle based on shadow length factor
     */
    private fun computeAsrHourAngle(shadowFactor: Double, latitude: Double, declination: Double): Double {
        val latRad = degToRad(latitude)
        val decRad = degToRad(declination)
        val d = abs(latitude - declination)
        val angle = -radToDeg(atan(1.0 / (shadowFactor + tan(degToRad(d)))))
        val top = sin(degToRad(angle)) - sin(latRad) * sin(decRad)
        val bottom = cos(latRad) * cos(decRad)
        val cosH = top / bottom
        if (cosH > 1.0 || cosH < -1.0) {
            return 0.0
        }
        return radToDeg(acos(cosH)) / 15.0
    }

    /**
     * Format double hour to 12-hour AM/PM string (e.g. 05:30 AM)
     */
    fun formatTime12h(hour: Int, minute: Int): String {
        val isPm = hour >= 12
        val h12 = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        val amPm = if (isPm) "PM" else "AM"
        return String.format(Locale.US, "%02d:%02d %s", h12, minute, amPm)
    }

    fun calculateDailyPrayers(
        calendar: Calendar = Calendar.getInstance(),
        city: IraqiCity,
        method: CalculationMethod = CalculationMethod.IRAQI_SUNNI,
        juristicSchool: JuristicSchool = JuristicSchool.SHAFI_HANBALI_MALIKI,
        fajrIqama: Int = 25,
        duhrIqama: Int = 12,
        asrIqama: Int = 12,
        maghribIqama: Int = 8,
        ishaIqama: Int = 12,
        fajrOffsetMin: Int = 0,
        shurukOffsetMin: Int = 0,
        duhrOffsetMin: Int = 0,
        asrOffsetMin: Int = 0,
        maghribOffsetMin: Int = 0,
        ishaOffsetMin: Int = 0
    ): DailyPrayerSchedule {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val jd = getJulianDay(year, month, day)
        val (declination, eqOfTime) = sunPosition(jd)
        val midDay = computeMidDay(city.timeZoneOffsetHours, city.longitude, eqOfTime)

        // Sunrise angle = 0.833°
        val sunAngleSunrise = 0.833
        val sunriseHourAngle = computeHourAngle(sunAngleSunrise, city.latitude, declination)

        // Fajr angle
        val fajrHourAngle = computeHourAngle(method.fajrAngle, city.latitude, declination)

        // Asr
        val shadowFactor = if (juristicSchool == JuristicSchool.HANAFI) 2.0 else 1.0
        val asrHourAngle = computeAsrHourAngle(shadowFactor, city.latitude, declination)

        // Maghrib
        val maghribHourAngle = sunriseHourAngle

        // Isha angle
        val ishaHourAngle = computeHourAngle(method.ishaAngle, city.latitude, declination)

        // Raw hours
        val rawFajr = fixHour(midDay - fajrHourAngle)
        val rawSunrise = fixHour(midDay - sunriseHourAngle)
        val rawDuhr = fixHour(midDay)
        val rawAsr = fixHour(midDay + asrHourAngle)
        val rawMaghrib = fixHour(midDay + maghribHourAngle) + (method.maghribMinutes / 60.0)
        val rawIsha = fixHour(midDay + ishaHourAngle)

        // Convert to minutes with user manual offset adjustments
        fun toMinutes(rawHour: Double, offsetMinutes: Int): Pair<Int, Int> {
            val totalMins = (rawHour * 60.0 + offsetMinutes).toInt()
            val normalizedMins = ((totalMins % 1440) + 1440) % 1440
            return Pair(normalizedMins / 60, normalizedMins % 60)
        }

        val (fajrH, fajrM) = toMinutes(rawFajr, fajrOffsetMin)
        val (shurukH, shurukM) = toMinutes(rawSunrise, shurukOffsetMin)
        val (duhrH, duhrM) = toMinutes(rawDuhr, duhrOffsetMin)
        val (asrH, asrM) = toMinutes(rawAsr, asrOffsetMin)
        val (maghribH, maghribM) = toMinutes(rawMaghrib, maghribOffsetMin)
        val (ishaH, ishaM) = toMinutes(rawIsha, ishaOffsetMin)

        fun calcIqamaTime(h: Int, m: Int, offset: Int): String {
            val totalM = h * 60 + m + offset
            val nh = (totalM / 60) % 24
            val nm = totalM % 60
            return formatTime12h(nh, nm)
        }

        val fajrItem = PrayerTimeItem(
            type = PrayerType.FAJR,
            nameEn = "Fajr",
            nameAr = "الفجر",
            timeFormatted = formatTime12h(fajrH, fajrM),
            hour24 = fajrH,
            minute = fajrM,
            iqamaOffsetMinutes = fajrIqama,
            iqamaTimeFormatted = calcIqamaTime(fajrH, fajrM, fajrIqama)
        )

        val shurukItem = PrayerTimeItem(
            type = PrayerType.SHURUK,
            nameEn = "Shuruk",
            nameAr = "الشروق",
            timeFormatted = formatTime12h(shurukH, shurukM),
            hour24 = shurukH,
            minute = shurukM,
            iqamaOffsetMinutes = 0,
            iqamaTimeFormatted = ""
        )

        val duhrItem = PrayerTimeItem(
            type = PrayerType.DUHR,
            nameEn = "Duhr",
            nameAr = "الظهر",
            timeFormatted = formatTime12h(duhrH, duhrM),
            hour24 = duhrH,
            minute = duhrM,
            iqamaOffsetMinutes = duhrIqama,
            iqamaTimeFormatted = calcIqamaTime(duhrH, duhrM, duhrIqama)
        )

        val asrItem = PrayerTimeItem(
            type = PrayerType.ASR,
            nameEn = "Asr",
            nameAr = "العصر",
            timeFormatted = formatTime12h(asrH, asrM),
            hour24 = asrH,
            minute = asrM,
            iqamaOffsetMinutes = asrIqama,
            iqamaTimeFormatted = calcIqamaTime(asrH, asrM, asrIqama)
        )

        val maghribItem = PrayerTimeItem(
            type = PrayerType.MAGHRIB,
            nameEn = "Maghrib",
            nameAr = "المغرب",
            timeFormatted = formatTime12h(maghribH, maghribM),
            hour24 = maghribH,
            minute = maghribM,
            iqamaOffsetMinutes = maghribIqama,
            iqamaTimeFormatted = calcIqamaTime(maghribH, maghribM, maghribIqama)
        )

        val ishaItem = PrayerTimeItem(
            type = PrayerType.ISHA,
            nameEn = "Isha",
            nameAr = "العشاء",
            timeFormatted = formatTime12h(ishaH, ishaM),
            hour24 = ishaH,
            minute = ishaM,
            iqamaOffsetMinutes = ishaIqama,
            iqamaTimeFormatted = calcIqamaTime(ishaH, ishaM, ishaIqama)
        )

        val isFriday = calendar.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY
        val jumuaItem = PrayerTimeItem(
            type = PrayerType.JUMUA,
            nameEn = "Jumua",
            nameAr = "الجمعة",
            timeFormatted = formatTime12h(duhrH, duhrM),
            hour24 = duhrH,
            minute = duhrM,
            iqamaOffsetMinutes = duhrIqama,
            iqamaTimeFormatted = calcIqamaTime(duhrH, duhrM, duhrIqama)
        )

        // Determine current and next prayer relative to right now
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val currentSecond = calendar.get(Calendar.SECOND)
        val currentTotalSeconds = currentHour * 3600 + currentMinute * 60 + currentSecond

        val prayerList = listOf(fajrItem, shurukItem, duhrItem, asrItem, maghribItem, ishaItem)
        val fiveDailyPrayers = listOf(fajrItem, duhrItem, asrItem, maghribItem, ishaItem)

        // Find next athan
        var nextPrayerFound: PrayerTimeItem = fajrItem
        var secondsUntilNextAthan: Long = 0
        var isFound = false

        for (p in fiveDailyPrayers) {
            val prayerSecs = p.hour24 * 3600 + p.minute * 60
            if (prayerSecs > currentTotalSeconds) {
                nextPrayerFound = p
                secondsUntilNextAthan = (prayerSecs - currentTotalSeconds).toLong()
                isFound = true
                break
            }
        }

        if (!isFound) {
            // Next is tomorrow's Fajr
            nextPrayerFound = fajrItem
            val fajrSecsTomorrow = (24 * 3600 - currentTotalSeconds) + (fajrItem.hour24 * 3600 + fajrItem.minute * 60)
            secondsUntilNextAthan = fajrSecsTomorrow.toLong()
        }

        // Check if we are currently inside an Iqama window (Athan just happened, waiting for Iqama)
        var isIqamaWindow = false
        var secondsUntilNextIqama: Long = 0

        for (p in fiveDailyPrayers) {
            val athanSecs = p.hour24 * 3600 + p.minute * 60
            val iqamaSecs = athanSecs + (p.iqamaOffsetMinutes * 60)
            if (currentTotalSeconds in athanSecs until iqamaSecs) {
                isIqamaWindow = true
                secondsUntilNextIqama = (iqamaSecs - currentTotalSeconds).toLong()
                nextPrayerFound = p
                break
            }
        }

        // Return copy with updated isNext tags
        val taggedFajr = fajrItem.copy(isNext = nextPrayerFound.type == PrayerType.FAJR)
        val taggedDuhr = duhrItem.copy(isNext = nextPrayerFound.type == PrayerType.DUHR)
        val taggedAsr = asrItem.copy(isNext = nextPrayerFound.type == PrayerType.ASR)
        val taggedMaghrib = maghribItem.copy(isNext = nextPrayerFound.type == PrayerType.MAGHRIB)
        val taggedIsha = ishaItem.copy(isNext = nextPrayerFound.type == PrayerType.ISHA)

        return DailyPrayerSchedule(
            fajr = taggedFajr,
            shuruk = shurukItem,
            duhr = taggedDuhr,
            asr = taggedAsr,
            maghrib = taggedMaghrib,
            isha = taggedIsha,
            jumua = jumuaItem,
            nextPrayer = nextPrayerFound,
            secondsUntilNextAthan = secondsUntilNextAthan,
            secondsUntilNextIqama = secondsUntilNextIqama,
            isIqamaWindow = isIqamaWindow
        )
    }
}
