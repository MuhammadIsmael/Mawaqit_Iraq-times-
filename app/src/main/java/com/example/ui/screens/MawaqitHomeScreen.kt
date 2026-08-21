package com.example.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.audio.AthanSoundManager
import com.example.data.models.PrayerTimeItem
import com.example.data.models.ScreenOrientationPref
import com.example.data.preferences.AppSettings
import com.example.data.preferences.SettingsRepository
import com.example.data.repository.PrayerTimesRepository
import com.example.ui.components.AthanAlertOverlay
import com.example.ui.components.BigClockCard
import com.example.ui.components.CountdownBanner
import com.example.ui.components.FooterBar
import com.example.ui.components.LiveStreamPlayerDialog
import com.example.ui.components.MihrabArchPrayerCard
import com.example.ui.components.ShurukJumuaBadge
import com.example.ui.components.TopHeaderBar
import com.example.ui.viewmodel.PrayerTimesViewModel
import com.example.ui.viewmodel.PrayerTimesViewModelFactory

@Composable
fun MawaqitHomeScreen(
    settingsRepository: SettingsRepository,
    athanSoundManager: AthanSoundManager,
    prayerTimesRepository: PrayerTimesRepository = remember { PrayerTimesRepository() },
    viewModel: PrayerTimesViewModel = viewModel(
        factory = PrayerTimesViewModelFactory(prayerTimesRepository, settingsRepository, athanSoundManager)
    ),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val settings = uiState.settings

    // Apply orientation preference to Activity
    DisposableEffect(settings.orientationPref) {
        val activity = context as? Activity
        when (settings.orientationPref) {
            ScreenOrientationPref.LANDSCAPE -> {
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
            ScreenOrientationPref.PORTRAIT -> {
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
            ScreenOrientationPref.AUTO -> {
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
        onDispose { }
    }

    // Dialog state
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showLiveStreamsDialog by remember { mutableStateOf(false) }

    val primaryThemeColor = Color(settings.colorTheme.primaryHex)
    val schedule = uiState.schedule

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .testTag("mawaqit_root_screen")
    ) {
        val isLandscape = maxWidth > maxHeight

        // Subtle Islamic Star Pattern in deep OLED background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val patternColor = Color(0x08FFFFFF)
            val stroke = Stroke(width = 1.dp.toPx())

            // Geometric lattice grid lines
            val step = 120.dp.toPx()
            var x = 0f
            while (x < w) {
                var y = 0f
                while (y < h) {
                    drawCircle(color = patternColor, radius = step * 0.35f, center = Offset(x, y), style = stroke)
                    y += step
                }
                x += step
            }
        }

        if (isLandscape) {
            // Exact Replica of Landscape 1920x1080 Home Screen
            LandscapeHomeScreenLayout(
                mosqueName = settings.mosqueName,
                cityName = settings.cityName,
                currentTimeFormatted = uiState.currentTimeFormatted,
                fullDateFormatted = uiState.fullDateFormatted,
                shurukTime = schedule.shuruk.timeFormatted,
                jumuaTime = schedule.jumua.timeFormatted,
                nextPrayerName = uiState.nextPrayerName,
                secondsRemaining = uiState.secondsRemaining,
                isIqamaCountdown = uiState.isIqamaCountdown,
                fajr = schedule.fajr,
                duhr = schedule.duhr,
                asr = schedule.asr,
                maghrib = schedule.maghrib,
                isha = schedule.isha,
                primaryColor = primaryThemeColor,
                mosqueId = settings.mosqueId,
                announcementTicker = settings.announcementTicker,
                onMenuClick = { showSettingsDialog = true },
                onStreamsClick = { showLiveStreamsDialog = true },
                onSoundTestClick = {
                    viewModel.playTestAthan(settings.athanVoice)
                },
                onPrayerCardClick = { _ ->
                    showSettingsDialog = true
                }
            )
        } else {
            // Adaptive Mosque Pillar / Vertical Portrait Screen
            PortraitHomeScreenLayout(
                mosqueName = settings.mosqueName,
                cityName = settings.cityName,
                currentTimeFormatted = uiState.currentTimeFormatted,
                fullDateFormatted = uiState.fullDateFormatted,
                shurukTime = schedule.shuruk.timeFormatted,
                jumuaTime = schedule.jumua.timeFormatted,
                nextPrayerName = uiState.nextPrayerName,
                secondsRemaining = uiState.secondsRemaining,
                isIqamaCountdown = uiState.isIqamaCountdown,
                fajr = schedule.fajr,
                duhr = schedule.duhr,
                asr = schedule.asr,
                maghrib = schedule.maghrib,
                isha = schedule.isha,
                primaryColor = primaryThemeColor,
                mosqueId = settings.mosqueId,
                announcementTicker = settings.announcementTicker,
                onMenuClick = { showSettingsDialog = true },
                onStreamsClick = { showLiveStreamsDialog = true },
                onSoundTestClick = {
                    viewModel.playTestAthan(settings.athanVoice)
                }
            )
        }

        // Fullscreen Athan Alert Modal
        uiState.triggerAthanAlert?.let { prayer ->
            AthanAlertOverlay(
                prayer = prayer,
                secondsUntilIqama = (prayer.iqamaOffsetMinutes * 60).toLong(),
                onDismiss = {
                    viewModel.dismissAthanAlert()
                },
                onSilenceAudio = {
                    athanSoundManager.stopAudio()
                }
            )
        }

        // Settings Dialog Modal
        if (showSettingsDialog) {
            SettingsDialog(
                currentSettings = settings,
                onSave = { updated ->
                    viewModel.updateSettings(updated)
                },
                onDismiss = { showSettingsDialog = false },
                onTestSound = { voice ->
                    viewModel.playTestAthan(voice)
                }
            )
        }

        // Live Streams Modal
        if (showLiveStreamsDialog) {
            LiveStreamPlayerDialog(
                onDismiss = { showLiveStreamsDialog = false }
            )
        }
    }
}

@Composable
fun LandscapeHomeScreenLayout(
    mosqueName: String,
    cityName: String,
    currentTimeFormatted: String,
    fullDateFormatted: String,
    shurukTime: String,
    jumuaTime: String,
    nextPrayerName: String,
    secondsRemaining: Long,
    isIqamaCountdown: Boolean,
    fajr: PrayerTimeItem,
    duhr: PrayerTimeItem,
    asr: PrayerTimeItem,
    maghrib: PrayerTimeItem,
    isha: PrayerTimeItem,
    primaryColor: Color,
    mosqueId: String,
    announcementTicker: String,
    onMenuClick: () -> Unit,
    onStreamsClick: () -> Unit,
    onSoundTestClick: () -> Unit,
    onPrayerCardClick: (PrayerTimeItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Top Header Bar
        TopHeaderBar(
            mosqueName = mosqueName,
            cityName = cityName,
            onMenuClick = onMenuClick,
            onStreamsClick = onStreamsClick,
            onSoundTestClick = onSoundTestClick
        )

        // 2. Middle Hero Section (Shuruk - Big Yellow Clock Card - Jumua)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Shuruk Time
            ShurukJumuaBadge(
                title = "Shuruk",
                time = shurukTime,
                modifier = Modifier.width(170.dp),
                alignEnd = false
            )

            // Center: Giant Golden Clock Card
            BigClockCard(
                currentTimeFormatted = currentTimeFormatted,
                fullDateFormatted = fullDateFormatted,
                cardColor = primaryColor,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 24.dp)
            )

            // Right: Jumua Time
            ShurukJumuaBadge(
                title = "Jumua",
                time = jumuaTime,
                modifier = Modifier.width(170.dp),
                alignEnd = true
            )
        }

        // 3. Dynamic Countdown Banner (e.g. 🕌 Maghrib Athan in 01:30:15 🕌)
        CountdownBanner(
            prayerName = nextPrayerName,
            secondsRemaining = secondsRemaining,
            isIqamaCountdown = isIqamaCountdown,
            textColor = primaryColor
        )

        // 4. Five Arch/Mihrab Shaped Prayer Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val prayers = listOf(fajr, duhr, asr, maghrib, isha)
            prayers.forEach { prayer ->
                MihrabArchPrayerCard(
                    prayer = prayer,
                    isActive = prayer.isNext,
                    primaryColor = primaryColor,
                    modifier = Modifier.weight(1f),
                    heightDp = 185.dp,
                    onClick = { onPrayerCardClick(prayer) }
                )
            }
        }

        // 5. Footer Bar (ID, QR Code, Mawaqit Badge)
        FooterBar(
            mosqueId = mosqueId,
            announcementTicker = announcementTicker
        )
    }
}

@Composable
fun PortraitHomeScreenLayout(
    mosqueName: String,
    cityName: String,
    currentTimeFormatted: String,
    fullDateFormatted: String,
    shurukTime: String,
    jumuaTime: String,
    nextPrayerName: String,
    secondsRemaining: Long,
    isIqamaCountdown: Boolean,
    fajr: PrayerTimeItem,
    duhr: PrayerTimeItem,
    asr: PrayerTimeItem,
    maghrib: PrayerTimeItem,
    isha: PrayerTimeItem,
    primaryColor: Color,
    mosqueId: String,
    announcementTicker: String,
    onMenuClick: () -> Unit,
    onStreamsClick: () -> Unit,
    onSoundTestClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopHeaderBar(
            mosqueName = mosqueName,
            cityName = cityName,
            onMenuClick = onMenuClick,
            onStreamsClick = onStreamsClick,
            onSoundTestClick = onSoundTestClick
        )

        BigClockCard(
            currentTimeFormatted = currentTimeFormatted,
            fullDateFormatted = fullDateFormatted,
            cardColor = primaryColor,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShurukJumuaBadge(title = "Shuruk", time = shurukTime)
            ShurukJumuaBadge(title = "Jumua", time = jumuaTime, alignEnd = true)
        }

        CountdownBanner(
            prayerName = nextPrayerName,
            secondsRemaining = secondsRemaining,
            isIqamaCountdown = isIqamaCountdown,
            textColor = primaryColor
        )

        val prayers = listOf(fajr, duhr, asr, maghrib, isha)
        prayers.forEach { prayer ->
            MihrabArchPrayerCard(
                prayer = prayer,
                isActive = prayer.isNext,
                primaryColor = primaryColor,
                modifier = Modifier.fillMaxWidth(),
                heightDp = 110.dp
            )
        }

        FooterBar(
            mosqueId = mosqueId,
            announcementTicker = announcementTicker
        )
    }
}
