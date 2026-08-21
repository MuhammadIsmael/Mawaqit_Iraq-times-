package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.calculator.HijriCalendarHelper
import com.example.data.calculator.PrayerCalculator
import com.example.data.models.CalculationMethod
import com.example.data.models.IRAQI_CITIES
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read app name from string resource`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Mawaqit Iraq", appName)
    }

    @Test
    fun `test prayer time calculation for Baghdad`() {
        val baghdad = IRAQI_CITIES[0]
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.AUGUST, 15, 12, 0, 0)
        }

        val schedule = PrayerCalculator.calculateDailyPrayers(
            calendar = cal,
            city = baghdad,
            method = CalculationMethod.IRAQI_SUNNI
        )

        assertNotNull(schedule.fajr)
        assertNotNull(schedule.duhr)
        assertNotNull(schedule.asr)
        assertNotNull(schedule.maghrib)
        assertNotNull(schedule.isha)

        assertTrue(schedule.fajr.hour24 in 3..5)
        assertTrue(schedule.duhr.hour24 in 11..13)
        assertTrue(schedule.maghrib.hour24 in 18..20)
    }

    @Test
    fun `test Hijri date formatting`() {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.AUGUST, 15)
        }
        val fullDate = HijriCalendarHelper.getFullFormattedDate(cal, 0)
        assertTrue(fullDate.contains("August"))
        assertTrue(fullDate.contains("2026"))
    }
}
