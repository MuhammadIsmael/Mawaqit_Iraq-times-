package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.api.PrayerApiResponse
import com.example.data.api.PrayerApiService
import com.example.data.api.PrayerApiTimings
import com.example.data.api.PrayerData
import com.example.data.models.IRAQI_CITIES
import com.example.data.models.PrayerType
import com.example.data.preferences.SettingsRepository
import com.example.data.repository.PrayerTimesRepository
import com.example.ui.viewmodel.PrayerTimesViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

class FakePrayerApiService(
    var shouldFail: Boolean = false
) : PrayerApiService {
    override suspend fun getTimingsByCity(
        city: String,
        country: String,
        method: Int?,
        school: Int?
    ): PrayerApiResponse {
        if (shouldFail) throw RuntimeException("Network timeout")
        return createMockResponse()
    }

    override suspend fun getTimingsByCoordinates(
        date: String,
        latitude: Double,
        longitude: Double,
        method: Int?,
        school: Int?,
        tune: String?
    ): PrayerApiResponse {
        if (shouldFail) throw RuntimeException("Network connection failed")
        return createMockResponse()
    }

    private fun createMockResponse(): PrayerApiResponse {
        return PrayerApiResponse(
            code = 200,
            status = "OK",
            data = PrayerData(
                timings = PrayerApiTimings(
                    fajr = "04:12",
                    sunrise = "05:32",
                    dhuhr = "12:15",
                    asr = "15:50",
                    sunset = "18:58",
                    maghrib = "18:58",
                    isha = "20:18",
                    imsak = "04:02",
                    midnight = "00:15"
                ),
                date = null,
                meta = null
            )
        )
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PrayerTimesRepositoryTest {

    @Test
    fun `test repository successfully fetches and parses prayer times from API`() = runTest {
        val fakeApi = FakePrayerApiService(shouldFail = false)
        val repo = PrayerTimesRepository(apiService = fakeApi)
        val baghdad = IRAQI_CITIES[0]

        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.AUGUST, 21, 10, 0, 0)
        }

        val result = repo.getDailyPrayerTimes(city = baghdad, calendar = cal)
        assertTrue(result.isSuccess)

        val schedule = result.getOrNull()
        assertNotNull(schedule)
        assertEquals(4, schedule!!.fajr.hour24)
        assertEquals(12, schedule.fajr.minute)
        assertEquals(12, schedule.duhr.hour24)
        assertEquals(15, schedule.duhr.minute)
        assertEquals(18, schedule.maghrib.hour24)
        assertEquals(58, schedule.maghrib.minute)
    }

    @Test
    fun `test repository falls back to local astronomical calculator on network failure`() = runTest {
        val failingApi = FakePrayerApiService(shouldFail = true)
        val repo = PrayerTimesRepository(apiService = failingApi)
        val najaf = IRAQI_CITIES[1]

        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.AUGUST, 21, 10, 0, 0)
        }

        val result = repo.getDailyPrayerTimes(city = najaf, calendar = cal)
        assertTrue(result.isSuccess)

        val schedule = result.getOrNull()
        assertNotNull(schedule)
        assertTrue(schedule!!.fajr.hour24 in 3..5)
        assertTrue(schedule.maghrib.hour24 in 18..20)
    }

    @Test
    fun `test countdown calculation before Duhr`() = runTest {
        val fakeApi = FakePrayerApiService(shouldFail = false)
        val repo = PrayerTimesRepository(apiService = fakeApi)
        val baghdad = IRAQI_CITIES[0]

        // Schedule has Duhr at 12:15
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.AUGUST, 21, 10, 15, 0) // 2 hours before 12:15
        }

        val result = repo.getDailyPrayerTimes(city = baghdad, calendar = cal)
        val schedule = result.getOrNull()!!

        val countdown = repo.calculateCountdown(schedule, cal)
        assertEquals(PrayerType.DUHR, countdown.nextPrayer.type)
        assertFalse(countdown.isIqamaWindow)
        // 2 hours = 7200 seconds
        assertEquals(7200L, countdown.secondsRemaining)
        assertEquals("02:00:00", countdown.formattedCountdown)
    }

    @Test
    fun `test countdown calculation inside Iqama window`() = runTest {
        val fakeApi = FakePrayerApiService(shouldFail = false)
        val repo = PrayerTimesRepository(apiService = fakeApi)
        val baghdad = IRAQI_CITIES[0]

        // Duhr at 12:15 with 12 min iqama (12:27). Current time 12:20:00 (5 minutes after athan, 7 min until iqama)
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.AUGUST, 21, 12, 20, 0)
        }

        val result = repo.getDailyPrayerTimes(city = baghdad, calendar = cal, duhrIqama = 12)
        val schedule = result.getOrNull()!!

        val countdown = repo.calculateCountdown(schedule, cal)
        assertEquals(PrayerType.DUHR, countdown.nextPrayer.type)
        assertTrue(countdown.isIqamaWindow)
        assertEquals(7 * 60L, countdown.secondsRemaining)
        assertEquals("00:07:00", countdown.formattedCountdown)
    }

    @Test
    fun `test ViewModel state updates and city changes`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val settingsRepo = SettingsRepository(context)
        val fakeApi = FakePrayerApiService(shouldFail = false)
        val prayerRepo = PrayerTimesRepository(apiService = fakeApi)

        val viewModel = PrayerTimesViewModel(
            prayerTimesRepository = prayerRepo,
            settingsRepository = settingsRepo,
            athanSoundManager = null
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.schedule)
        assertNotNull(state.countdownFormatted)

        // Select Basra
        val basra = IRAQI_CITIES[3]
        viewModel.selectCity(basra)
        advanceUntilIdle()

        assertEquals(basra.nameEn, viewModel.uiState.value.selectedCity.nameEn)
    }
}
