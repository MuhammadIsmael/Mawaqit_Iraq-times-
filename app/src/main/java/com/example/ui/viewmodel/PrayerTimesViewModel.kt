package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.audio.AthanSoundManager
import com.example.data.calculator.DailyPrayerSchedule
import com.example.data.calculator.HijriCalendarHelper
import com.example.data.calculator.PrayerCalculator
import com.example.data.models.AthanVoice
import com.example.data.models.IRAQI_CITIES
import com.example.data.models.IraqiCity
import com.example.data.models.PrayerTimeItem
import com.example.data.models.PrayerType
import com.example.data.preferences.AppSettings
import com.example.data.preferences.SettingsRepository
import com.example.data.repository.PrayerOffsets
import com.example.data.repository.PrayerTimesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class PrayerTimesUiState(
    val isLoading: Boolean = false,
    val isOnline: Boolean = true,
    val schedule: DailyPrayerSchedule = getInitialDefaultSchedule(),
    val currentTimeFormatted: String = "12:00:00 PM",
    val fullDateFormatted: String = "",
    val countdownFormatted: String = "00:00:00",
    val secondsRemaining: Long = 0L,
    val isIqamaCountdown: Boolean = false,
    val nextPrayerName: String = "Fajr",
    val selectedCity: IraqiCity = IRAQI_CITIES[0],
    val settings: AppSettings = AppSettings(),
    val triggerAthanAlert: PrayerTimeItem? = null,
    val errorMessage: String? = null
)

private fun getInitialDefaultSchedule(): DailyPrayerSchedule {
    val dummy = PrayerTimeItem(
        type = PrayerType.FAJR,
        nameEn = "Fajr",
        nameAr = "الفجر",
        timeFormatted = "04:15 AM",
        hour24 = 4,
        minute = 15,
        iqamaOffsetMinutes = 25,
        iqamaTimeFormatted = "04:40 AM"
    )
    return DailyPrayerSchedule(
        fajr = dummy,
        shuruk = dummy.copy(type = PrayerType.SHURUK, nameEn = "Shuruk", nameAr = "الشروق", timeFormatted = "05:30 AM", hour24 = 5, minute = 30),
        duhr = dummy.copy(type = PrayerType.DUHR, nameEn = "Duhr", nameAr = "الظهر", timeFormatted = "12:15 PM", hour24 = 12, minute = 15),
        asr = dummy.copy(type = PrayerType.ASR, nameEn = "Asr", nameAr = "العصر", timeFormatted = "03:45 PM", hour24 = 15, minute = 45),
        maghrib = dummy.copy(type = PrayerType.MAGHRIB, nameEn = "Maghrib", nameAr = "المغرب", timeFormatted = "06:45 PM", hour24 = 18, minute = 45),
        isha = dummy.copy(type = PrayerType.ISHA, nameEn = "Isha", nameAr = "العشاء", timeFormatted = "08:15 PM", hour24 = 20, minute = 15),
        jumua = dummy.copy(type = PrayerType.JUMUA, nameEn = "Jumua", nameAr = "الجمعة", timeFormatted = "12:15 PM", hour24 = 12, minute = 15),
        nextPrayer = dummy,
        secondsUntilNextAthan = 3600,
        secondsUntilNextIqama = 0,
        isIqamaWindow = false
    )
}

class PrayerTimesViewModel(
    private val prayerTimesRepository: PrayerTimesRepository = PrayerTimesRepository(),
    private val settingsRepository: SettingsRepository,
    private val athanSoundManager: AthanSoundManager? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrayerTimesUiState())
    val uiState: StateFlow<PrayerTimesUiState> = _uiState.asStateFlow()

    private var clockTickerJob: Job? = null
    private var lastTriggeredMinute: Int = -1

    init {
        // Collect app settings changes
        viewModelScope.launch {
            settingsRepository.settingsFlow.collect { settings ->
                _uiState.update { it.copy(settings = settings, selectedCity = settings.currentCity) }
                fetchPrayerTimes(settings.currentCity)
            }
        }

        // Start live 1-second clock and prayer countdown engine
        startLiveClockTicker()
    }

    /**
     * Fetches daily prayer times for an Iraqi city via REST API
     */
    fun fetchPrayerTimes(city: IraqiCity = _uiState.value.selectedCity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val settings = _uiState.value.settings
            val calendar = Calendar.getInstance()

            val offsets = PrayerOffsets(
                fajr = settings.fajrOffsetMin,
                shuruk = settings.shurukOffsetMin,
                duhr = settings.duhrOffsetMin,
                asr = settings.asrOffsetMin,
                maghrib = settings.maghribOffsetMin,
                isha = settings.ishaOffsetMin
            )

            val result = prayerTimesRepository.getDailyPrayerTimes(
                city = city,
                calendar = calendar,
                method = settings.calculationMethod,
                juristicSchool = settings.juristicSchool,
                fajrIqama = settings.fajrIqamaMin,
                duhrIqama = settings.duhrIqamaMin,
                asrIqama = settings.asrIqamaMin,
                maghribIqama = settings.maghribIqamaMin,
                ishaIqama = settings.ishaIqamaMin,
                offsets = offsets
            )

            result.onSuccess { schedule ->
                val countdown = prayerTimesRepository.calculateCountdown(schedule, calendar)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isOnline = true,
                        schedule = schedule,
                        selectedCity = city,
                        nextPrayerName = countdown.nextPrayer.nameEn,
                        secondsRemaining = countdown.secondsRemaining,
                        isIqamaCountdown = countdown.isIqamaWindow,
                        countdownFormatted = countdown.formattedCountdown
                    )
                }
            }.onFailure { error ->
                // Use local calculation fallback
                val fallback = PrayerCalculator.calculateDailyPrayers(
                    calendar = calendar,
                    city = city,
                    method = settings.calculationMethod,
                    juristicSchool = settings.juristicSchool,
                    fajrIqama = settings.fajrIqamaMin,
                    duhrIqama = settings.duhrIqamaMin,
                    asrIqama = settings.asrIqamaMin,
                    maghribIqama = settings.maghribIqamaMin,
                    ishaIqama = settings.ishaIqamaMin
                )
                val countdown = prayerTimesRepository.calculateCountdown(fallback, calendar)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isOnline = false,
                        schedule = fallback,
                        selectedCity = city,
                        nextPrayerName = countdown.nextPrayer.nameEn,
                        secondsRemaining = countdown.secondsRemaining,
                        isIqamaCountdown = countdown.isIqamaWindow,
                        countdownFormatted = countdown.formattedCountdown,
                        errorMessage = error.localizedMessage
                    )
                }
            }
        }
    }

    /**
     * Change active Iraqi city
     */
    fun selectCity(city: IraqiCity) {
        val index = IRAQI_CITIES.indexOf(city).coerceAtLeast(0)
        val currentSettings = _uiState.value.settings
        val updated = currentSettings.copy(
            selectedCityIndex = index,
            cityName = "${city.nameAr} - العراق"
        )
        settingsRepository.updateSettings(updated)
    }

    /**
     * Updates and saves settings
     */
    fun updateSettings(newSettings: AppSettings) {
        settingsRepository.updateSettings(newSettings)
    }

    /**
     * Dismiss full screen Athan alert overlay
     */
    fun dismissAthanAlert() {
        _uiState.update { it.copy(triggerAthanAlert = null) }
        athanSoundManager?.stopAudio()
    }

    /**
     * Test Athan Audio
     */
    fun playTestAthan(voice: AthanVoice) {
        athanSoundManager?.playAthan(voice, _uiState.value.settings.athanVolumePercent)
    }

    /**
     * Starts continuous 1-second background ticker to update clock, date, and prayer countdown
     */
    private fun startLiveClockTicker() {
        clockTickerJob?.cancel()
        clockTickerJob = viewModelScope.launch {
            while (isActive) {
                val now = Calendar.getInstance()
                updateClockAndCountdown(now)
                delay(1000)
            }
        }
    }

    private fun updateClockAndCountdown(now: Calendar) {
        val state = _uiState.value
        val settings = state.settings

        // Format Clock String
        val clockPattern = if (settings.is24HourFormat) {
            if (settings.showSeconds) "HH:mm:ss" else "HH:mm"
        } else {
            if (settings.showSeconds) "hh:mm:ss a" else "hh:mm a"
        }
        val clockFormat = SimpleDateFormat(clockPattern, Locale.US)
        val timeFormatted = clockFormat.format(now.time)

        // Format Full Date String (Gregorian + Day + Hijri)
        val fullDate = HijriCalendarHelper.getFullFormattedDate(now, settings.hijriAdjustmentDays)

        // Calculate real-time countdown to next prayer or next Iqama
        val countdown = prayerTimesRepository.calculateCountdown(state.schedule, now)

        // Check for Athan Arrival right on minute 00 seconds
        val currentTotalMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val currentSeconds = now.get(Calendar.SECOND)

        var triggeredPrayer: PrayerTimeItem? = null
        if (currentSeconds == 0 && currentTotalMinutes != lastTriggeredMinute) {
            val dailyPrayers = listOf(
                state.schedule.fajr,
                state.schedule.duhr,
                state.schedule.asr,
                state.schedule.maghrib,
                state.schedule.isha
            )
            for (prayer in dailyPrayers) {
                val pMin = prayer.hour24 * 60 + prayer.minute
                if (pMin == currentTotalMinutes) {
                    lastTriggeredMinute = currentTotalMinutes
                    triggeredPrayer = prayer
                    if (settings.isAthanAlertPopupEnabled) {
                        _uiState.update { it.copy(triggerAthanAlert = prayer) }
                    }
                    athanSoundManager?.playAthan(settings.athanVoice, settings.athanVolumePercent)
                    break
                }
            }
        }

        // Update UI state with updated countdown and active flags
        val updatedSchedule = state.schedule.copy(
            fajr = state.schedule.fajr.copy(isNext = countdown.nextPrayer.type == PrayerType.FAJR),
            duhr = state.schedule.duhr.copy(isNext = countdown.nextPrayer.type == PrayerType.DUHR),
            asr = state.schedule.asr.copy(isNext = countdown.nextPrayer.type == PrayerType.ASR),
            maghrib = state.schedule.maghrib.copy(isNext = countdown.nextPrayer.type == PrayerType.MAGHRIB),
            isha = state.schedule.isha.copy(isNext = countdown.nextPrayer.type == PrayerType.ISHA),
            nextPrayer = countdown.nextPrayer,
            secondsUntilNextAthan = if (!countdown.isIqamaWindow) countdown.secondsRemaining else 0,
            secondsUntilNextIqama = if (countdown.isIqamaWindow) countdown.secondsRemaining else 0,
            isIqamaWindow = countdown.isIqamaWindow
        )

        _uiState.update {
            it.copy(
                currentTimeFormatted = timeFormatted,
                fullDateFormatted = fullDate,
                countdownFormatted = countdown.formattedCountdown,
                secondsRemaining = countdown.secondsRemaining,
                isIqamaCountdown = countdown.isIqamaWindow,
                nextPrayerName = countdown.nextPrayer.nameEn,
                schedule = updatedSchedule
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        clockTickerJob?.cancel()
    }
}

class PrayerTimesViewModelFactory(
    private val repository: PrayerTimesRepository,
    private val settingsRepository: SettingsRepository,
    private val athanSoundManager: AthanSoundManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PrayerTimesViewModel::class.java)) {
            return PrayerTimesViewModel(repository, settingsRepository, athanSoundManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
