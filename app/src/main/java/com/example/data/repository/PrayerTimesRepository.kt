package com.example.data.repository

import com.example.data.api.PrayerApiService
import com.example.data.api.PrayerApiTimings
import com.example.data.api.RetrofitClient
import com.example.data.calculator.DailyPrayerSchedule
import com.example.data.calculator.PrayerCalculator
import com.example.data.models.CalculationMethod
import com.example.data.models.IRAQI_CITIES
import com.example.data.models.IraqiCity
import com.example.data.models.JuristicSchool
import com.example.data.models.PrayerTimeItem
import com.example.data.models.PrayerType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class PrayerOffsets(
    val fajr: Int = 0,
    val shuruk: Int = 0,
    val duhr: Int = 0,
    val asr: Int = 0,
    val maghrib: Int = 0,
    val isha: Int = 0
)

data class CountdownInfo(
    val nextPrayer: PrayerTimeItem,
    val secondsRemaining: Long,
    val isIqamaWindow: Boolean,
    val formattedCountdown: String
)

class PrayerTimesRepository(
    private val apiService: PrayerApiService = RetrofitClient.prayerApiService
) {

    /**
     * Fetches daily prayer times for an Iraqi city.
     * Tries public REST API (Aladhan) first; falls back to local high-precision calculation on network error.
     */
    suspend fun getDailyPrayerTimes(
        city: IraqiCity,
        calendar: Calendar = Calendar.getInstance(),
        method: CalculationMethod = CalculationMethod.IRAQI_SUNNI,
        juristicSchool: JuristicSchool = JuristicSchool.SHAFI_HANBALI_MALIKI,
        fajrIqama: Int = 25,
        duhrIqama: Int = 12,
        asrIqama: Int = 12,
        maghribIqama: Int = 8,
        ishaIqama: Int = 12,
        offsets: PrayerOffsets = PrayerOffsets()
    ): Result<DailyPrayerSchedule> = withContext(Dispatchers.IO) {
        try {
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
            val dateStr = dateFormat.format(calendar.time)

            val apiMethodCode = mapCalculationMethodToApiCode(method)
            val apiSchoolCode = if (juristicSchool == JuristicSchool.HANAFI) 1 else 0

            val response = apiService.getTimingsByCoordinates(
                date = dateStr,
                latitude = city.latitude,
                longitude = city.longitude,
                method = apiMethodCode,
                school = apiSchoolCode
            )

            val timings = response.data?.timings
            if (response.code == 200 && timings != null) {
                val schedule = convertTimingsToSchedule(
                    timings = timings,
                    calendar = calendar,
                    fajrIqama = fajrIqama,
                    duhrIqama = duhrIqama,
                    asrIqama = asrIqama,
                    maghribIqama = maghribIqama,
                    ishaIqama = ishaIqama,
                    offsets = offsets
                )
                Result.success(schedule)
            } else {
                // API returned non-200, fallback to local calculator
                val localSchedule = calculateLocally(
                    city, calendar, method, juristicSchool,
                    fajrIqama, duhrIqama, asrIqama, maghribIqama, ishaIqama, offsets
                )
                Result.success(localSchedule)
            }
        } catch (e: Exception) {
            // Network failure or timeout -> Seamless offline fallback
            val fallbackSchedule = calculateLocally(
                city, calendar, method, juristicSchool,
                fajrIqama, duhrIqama, asrIqama, maghribIqama, ishaIqama, offsets
            )
            Result.success(fallbackSchedule)
        }
    }

    /**
     * Calculates countdown to the next prayer or next Iqama
     */
    fun calculateCountdown(
        schedule: DailyPrayerSchedule,
        calendar: Calendar = Calendar.getInstance()
    ): CountdownInfo {
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val currentSecond = calendar.get(Calendar.SECOND)
        val currentTotalSeconds = currentHour * 3600 + currentMinute * 60 + currentSecond

        val fivePrayers = listOf(schedule.fajr, schedule.duhr, schedule.asr, schedule.maghrib, schedule.isha)

        // 1. Check if inside Iqama window (between athan and iqama)
        for (prayer in fivePrayers) {
            val athanSecs = prayer.hour24 * 3600 + prayer.minute * 60
            val iqamaSecs = athanSecs + (prayer.iqamaOffsetMinutes * 60)
            if (currentTotalSeconds in athanSecs until iqamaSecs) {
                val remSecs = (iqamaSecs - currentTotalSeconds).toLong()
                return CountdownInfo(
                    nextPrayer = prayer,
                    secondsRemaining = remSecs,
                    isIqamaWindow = true,
                    formattedCountdown = formatCountdown(remSecs)
                )
            }
        }

        // 2. Otherwise find next upcoming Athan today
        for (prayer in fivePrayers) {
            val athanSecs = prayer.hour24 * 3600 + prayer.minute * 60
            if (athanSecs > currentTotalSeconds) {
                val remSecs = (athanSecs - currentTotalSeconds).toLong()
                return CountdownInfo(
                    nextPrayer = prayer,
                    secondsRemaining = remSecs,
                    isIqamaWindow = false,
                    formattedCountdown = formatCountdown(remSecs)
                )
            }
        }

        // 3. Past Isha -> Next prayer is tomorrow's Fajr
        val fajrSecsTomorrow = (24 * 3600 - currentTotalSeconds) + (schedule.fajr.hour24 * 3600 + schedule.fajr.minute * 60)
        return CountdownInfo(
            nextPrayer = schedule.fajr,
            secondsRemaining = fajrSecsTomorrow.toLong(),
            isIqamaWindow = false,
            formattedCountdown = formatCountdown(fajrSecsTomorrow.toLong())
        )
    }

    fun formatCountdown(totalSeconds: Long): String {
        val hrs = totalSeconds / 3600
        val mins = (totalSeconds % 3600) / 60
        val secs = totalSeconds % 60
        return String.format(Locale.US, "%02d:%02d:%02d", hrs, mins, secs)
    }

    fun getIraqiCities(): List<IraqiCity> = IRAQI_CITIES

    private fun parseTimeString(raw: String, offsetMinutes: Int): Pair<Int, Int> {
        val clean = raw.trim().takeWhile { it.isDigit() || it == ':' }
        val parts = clean.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0

        val totalM = h * 60 + m + offsetMinutes
        val normalized = ((totalM % 1440) + 1440) % 1440
        return Pair(normalized / 60, normalized % 60)
    }

    private fun convertTimingsToSchedule(
        timings: PrayerApiTimings,
        calendar: Calendar,
        fajrIqama: Int,
        duhrIqama: Int,
        asrIqama: Int,
        maghribIqama: Int,
        ishaIqama: Int,
        offsets: PrayerOffsets
    ): DailyPrayerSchedule {
        val (fajrH, fajrM) = parseTimeString(timings.fajr, offsets.fajr)
        val (shurukH, shurukM) = parseTimeString(timings.sunrise, offsets.shuruk)
        val (duhrH, duhrM) = parseTimeString(timings.dhuhr, offsets.duhr)
        val (asrH, asrM) = parseTimeString(timings.asr, offsets.asr)
        val (maghribH, maghribM) = parseTimeString(timings.maghrib, offsets.maghrib)
        val (ishaH, ishaM) = parseTimeString(timings.isha, offsets.isha)

        fun calcIqamaTime(h: Int, m: Int, offset: Int): String {
            val totalM = h * 60 + m + offset
            val nh = (totalM / 60) % 24
            val nm = totalM % 60
            return PrayerCalculator.formatTime12h(nh, nm)
        }

        val fajrItem = PrayerTimeItem(
            type = PrayerType.FAJR,
            nameEn = "Fajr",
            nameAr = "الفجر",
            timeFormatted = PrayerCalculator.formatTime12h(fajrH, fajrM),
            hour24 = fajrH,
            minute = fajrM,
            iqamaOffsetMinutes = fajrIqama,
            iqamaTimeFormatted = calcIqamaTime(fajrH, fajrM, fajrIqama)
        )

        val shurukItem = PrayerTimeItem(
            type = PrayerType.SHURUK,
            nameEn = "Shuruk",
            nameAr = "الشروق",
            timeFormatted = PrayerCalculator.formatTime12h(shurukH, shurukM),
            hour24 = shurukH,
            minute = shurukM,
            iqamaOffsetMinutes = 0,
            iqamaTimeFormatted = ""
        )

        val duhrItem = PrayerTimeItem(
            type = PrayerType.DUHR,
            nameEn = "Duhr",
            nameAr = "الظهر",
            timeFormatted = PrayerCalculator.formatTime12h(duhrH, duhrM),
            hour24 = duhrH,
            minute = duhrM,
            iqamaOffsetMinutes = duhrIqama,
            iqamaTimeFormatted = calcIqamaTime(duhrH, duhrM, duhrIqama)
        )

        val asrItem = PrayerTimeItem(
            type = PrayerType.ASR,
            nameEn = "Asr",
            nameAr = "العصر",
            timeFormatted = PrayerCalculator.formatTime12h(asrH, asrM),
            hour24 = asrH,
            minute = asrM,
            iqamaOffsetMinutes = asrIqama,
            iqamaTimeFormatted = calcIqamaTime(asrH, asrM, asrIqama)
        )

        val maghribItem = PrayerTimeItem(
            type = PrayerType.MAGHRIB,
            nameEn = "Maghrib",
            nameAr = "المغرب",
            timeFormatted = PrayerCalculator.formatTime12h(maghribH, maghribM),
            hour24 = maghribH,
            minute = maghribM,
            iqamaOffsetMinutes = maghribIqama,
            iqamaTimeFormatted = calcIqamaTime(maghribH, maghribM, maghribIqama)
        )

        val ishaItem = PrayerTimeItem(
            type = PrayerType.ISHA,
            nameEn = "Isha",
            nameAr = "العشاء",
            timeFormatted = PrayerCalculator.formatTime12h(ishaH, ishaM),
            hour24 = ishaH,
            minute = ishaM,
            iqamaOffsetMinutes = ishaIqama,
            iqamaTimeFormatted = calcIqamaTime(ishaH, ishaM, ishaIqama)
        )

        val jumuaItem = PrayerTimeItem(
            type = PrayerType.JUMUA,
            nameEn = "Jumua",
            nameAr = "الجمعة",
            timeFormatted = PrayerCalculator.formatTime12h(duhrH, duhrM),
            hour24 = duhrH,
            minute = duhrM,
            iqamaOffsetMinutes = duhrIqama,
            iqamaTimeFormatted = calcIqamaTime(duhrH, duhrM, duhrIqama)
        )

        val initialSchedule = DailyPrayerSchedule(
            fajr = fajrItem,
            shuruk = shurukItem,
            duhr = duhrItem,
            asr = asrItem,
            maghrib = maghribItem,
            isha = ishaItem,
            jumua = jumuaItem,
            nextPrayer = fajrItem,
            secondsUntilNextAthan = 0,
            secondsUntilNextIqama = 0,
            isIqamaWindow = false
        )

        val countdown = calculateCountdown(initialSchedule, calendar)

        return initialSchedule.copy(
            fajr = fajrItem.copy(isNext = countdown.nextPrayer.type == PrayerType.FAJR),
            duhr = duhrItem.copy(isNext = countdown.nextPrayer.type == PrayerType.DUHR),
            asr = asrItem.copy(isNext = countdown.nextPrayer.type == PrayerType.ASR),
            maghrib = maghribItem.copy(isNext = countdown.nextPrayer.type == PrayerType.MAGHRIB),
            isha = ishaItem.copy(isNext = countdown.nextPrayer.type == PrayerType.ISHA),
            nextPrayer = countdown.nextPrayer,
            secondsUntilNextAthan = if (!countdown.isIqamaWindow) countdown.secondsRemaining else 0,
            secondsUntilNextIqama = if (countdown.isIqamaWindow) countdown.secondsRemaining else 0,
            isIqamaWindow = countdown.isIqamaWindow
        )
    }

    private fun calculateLocally(
        city: IraqiCity,
        calendar: Calendar,
        method: CalculationMethod,
        juristicSchool: JuristicSchool,
        fajrIqama: Int,
        duhrIqama: Int,
        asrIqama: Int,
        maghribIqama: Int,
        ishaIqama: Int,
        offsets: PrayerOffsets
    ): DailyPrayerSchedule {
        return PrayerCalculator.calculateDailyPrayers(
            calendar = calendar,
            city = city,
            method = method,
            juristicSchool = juristicSchool,
            fajrIqama = fajrIqama,
            duhrIqama = duhrIqama,
            asrIqama = asrIqama,
            maghribIqama = maghribIqama,
            ishaIqama = ishaIqama,
            fajrOffsetMin = offsets.fajr,
            shurukOffsetMin = offsets.shuruk,
            duhrOffsetMin = offsets.duhr,
            asrOffsetMin = offsets.asr,
            maghribOffsetMin = offsets.maghrib,
            ishaOffsetMin = offsets.isha
        )
    }

    private fun mapCalculationMethodToApiCode(method: CalculationMethod): Int {
        return when (method) {
            CalculationMethod.IRAQI_SUNNI -> 3 // Muslim World League standard
            CalculationMethod.IRAQI_SHIA -> 0 // Shia Ithna-Ashari, Leva Institute, Qum
            CalculationMethod.EGYPTIAN -> 5 // Egyptian General Authority of Survey
            CalculationMethod.UMM_AL_QURA -> 4 // Umm Al-Qura University, Makkah
            CalculationMethod.MUSLIM_WORLD_LEAGUE -> 3 // Muslim World League
        }
    }
}
