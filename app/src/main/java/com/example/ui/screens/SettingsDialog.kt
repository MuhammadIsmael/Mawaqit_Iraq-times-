package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.AthanVoice
import com.example.data.models.CalculationMethod
import com.example.data.models.ColorThemePref
import com.example.data.models.IRAQI_CITIES
import com.example.data.models.JuristicSchool
import com.example.data.models.ScreenOrientationPref
import com.example.data.preferences.AppSettings
import com.example.ui.util.dpadAdjustable
import com.example.ui.util.dpadFocusable

enum class SettingsTab(val titleEn: String, val titleAr: String, val icon: ImageVector) {
    GENERAL("General & Mosque", "المسجد والمدينة", Icons.Default.Mosque),
    PRAYER_TIMES("Prayer Methods", "طريقة الحساب", Icons.Default.Timer),
    IQAMA("Iqama Times", "أوقات الإقامة", Icons.Default.Timer),
    DISPLAY("Display & Layout", "العرض والاتجاه", Icons.Default.ScreenRotation),
    AUDIO("Athan Sound", "الصوت والأذان", Icons.Default.VolumeUp)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    currentSettings: AppSettings,
    onSave: (AppSettings) -> Unit,
    onDismiss: () -> Unit,
    onTestSound: (AthanVoice) -> Unit,
    modifier: Modifier = Modifier
) {
    var draftSettings by remember { mutableStateOf(currentSettings) }
    var selectedTab by remember { mutableStateOf(SettingsTab.GENERAL) }

    val initialFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        try {
            initialFocusRequester.requestFocus()
        } catch (e: Exception) {
            // Ignore if layout not ready
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xE6050811))
            .padding(20.dp)
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown && (keyEvent.key == Key.Back || keyEvent.key == Key.Escape)) {
                    onDismiss()
                    true
                } else {
                    false
                }
            }
            .testTag("settings_dialog"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF0F172A))
                .border(2.dp, Color(0xFF334155), RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            // Header Bar with Title and Save/Close
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = Color(0xFFFFB703),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "إعدادات شاشة مواقيت العراق (Settings)",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Save Button
                    val saveInteractionSource = remember { MutableInteractionSource() }
                    val isSaveFocused by saveInteractionSource.collectIsFocusedAsState()

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSaveFocused) Color.White else Color(0xFFFFB703))
                            .border(
                                width = if (isSaveFocused) 3.dp else 0.dp,
                                color = Color.White,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .dpadFocusable(
                                onClick = {
                                    onSave(draftSettings)
                                    onDismiss()
                                },
                                interactionSource = saveInteractionSource,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                            .testTag("button_save_settings"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Save",
                                tint = Color.Black
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "حفظ (Save)",
                                color = Color.Black,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Close Button
                    val closeInteractionSource = remember { MutableInteractionSource() }
                    val isCloseFocused by closeInteractionSource.collectIsFocusedAsState()

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isCloseFocused) Color(0xFFEF4444) else Color(0xFF334155))
                            .border(
                                width = if (isCloseFocused) 3.dp else 0.dp,
                                color = Color.White,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .dpadFocusable(
                                onClick = onDismiss,
                                interactionSource = closeInteractionSource,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(8.dp)
                            .testTag("button_close_settings"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }
            }

            Divider(color = Color(0xFF1E293B), thickness = 1.dp)

            Spacer(modifier = Modifier.height(12.dp))

            // Body: Left Tabs + Right Settings Content
            Row(modifier = Modifier.weight(1f)) {
                // Left Navigation Tabs (D-Pad Friendly)
                Column(
                    modifier = Modifier
                        .width(230.dp)
                        .fillMaxHeight()
                        .padding(end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SettingsTab.entries.forEachIndexed { index, tab ->
                        val isSelected = tab == selectedTab
                        val tabInteractionSource = remember { MutableInteractionSource() }
                        val isTabFocused by tabInteractionSource.collectIsFocusedAsState()
                        val scale by animateFloatAsState(
                            targetValue = if (isTabFocused) 1.04f else 1.0f,
                            animationSpec = tween(150),
                            label = "tabScale"
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(if (index == 0) Modifier.focusRequester(initialFocusRequester) else Modifier)
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                }
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    when {
                                        isSelected -> Color(0xFFFFB703)
                                        isTabFocused -> Color(0xFF475569)
                                        else -> Color(0xFF1E293B)
                                    }
                                )
                                .border(
                                    width = if (isTabFocused) 2.5.dp else 0.dp,
                                    color = Color.White,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .dpadFocusable(
                                    onClick = { selectedTab = tab },
                                    interactionSource = tabInteractionSource,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                                .testTag("tab_${tab.name.lowercase()}"),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.Black else Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = tab.titleAr,
                                        color = if (isSelected) Color.Black else Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = tab.titleEn,
                                        color = if (isSelected) Color(0xFF451A03) else Color(0xFF94A3B8),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Divider(
                    color = Color(0xFF1E293B),
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                )

                // Right: Tab Content Area
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(start = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (selectedTab) {
                        SettingsTab.GENERAL -> {
                            item {
                                SectionHeader("معلومات المسجد والمدينة (Mosque & Location)")

                                OutlinedTextField(
                                    value = draftSettings.mosqueName,
                                    onValueChange = { draftSettings = draftSettings.copy(mosqueName = it) },
                                    label = { Text("اسم المسجد (Mosque Name)", color = Color(0xFF94A3B8)) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFFFFB703),
                                        unfocusedBorderColor = Color(0xFF475569)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_mosque_name")
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = draftSettings.cityName,
                                    onValueChange = { draftSettings = draftSettings.copy(cityName = it) },
                                    label = { Text("المدينة / المحافظة (City & Province)", color = Color(0xFF94A3B8)) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFFFFB703),
                                        unfocusedBorderColor = Color(0xFF475569)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_city_name")
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = "اختر المحافظة العراقية لتحديد الإحداثيات الدقيقة (استخدم الأسهم لاختيار المدينة):",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    IRAQI_CITIES.chunked(3).forEach { rowCities ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            rowCities.forEach { city ->
                                                val isSelected = IRAQI_CITIES.indexOf(city) == draftSettings.selectedCityIndex
                                                CityChip(
                                                    city = city,
                                                    isSelected = isSelected,
                                                    onSelect = {
                                                        draftSettings = draftSettings.copy(
                                                            selectedCityIndex = IRAQI_CITIES.indexOf(city),
                                                            cityName = "${city.nameAr} - العراق"
                                                        )
                                                    },
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                OutlinedTextField(
                                    value = draftSettings.announcementTicker,
                                    onValueChange = { draftSettings = draftSettings.copy(announcementTicker = it) },
                                    label = { Text("شريط الإعلانات والحديث النبوي (Announcement Ticker)", color = Color(0xFF94A3B8)) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFFFFB703),
                                        unfocusedBorderColor = Color(0xFF475569)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_ticker_text")
                                )
                            }
                        }

                        SettingsTab.PRAYER_TIMES -> {
                            item {
                                SectionHeader("طريقة حساب أوقات الصلاة (Calculation Method)")

                                CalculationMethod.entries.forEach { method ->
                                    val isSelected = method == draftSettings.calculationMethod
                                    SelectableOptionCard(
                                        title = method.titleAr,
                                        subtitle = "${method.titleEn} (Fajr ${method.fajrAngle}°, Isha ${method.ishaAngle}°)",
                                        isSelected = isSelected,
                                        onSelect = { draftSettings = draftSettings.copy(calculationMethod = method) }
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                SectionHeader("المذهب الفقهي لحساب وقت العصر (Juristic School for Asr)")
                                JuristicSchool.entries.forEach { school ->
                                    val isSelected = school == draftSettings.juristicSchool
                                    SelectableOptionCard(
                                        title = school.titleAr,
                                        subtitle = school.titleEn,
                                        isSelected = isSelected,
                                        onSelect = { draftSettings = draftSettings.copy(juristicSchool = school) }
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                SectionHeader("تعديل التاريخ الهجري (Hijri Date Adjustment)")
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    listOf(-2, -1, 0, 1, 2).forEach { adj ->
                                        val isSelected = draftSettings.hijriAdjustmentDays == adj
                                        val label = when {
                                            adj > 0 -> "+$adj يوم"
                                            adj < 0 -> "$adj يوم"
                                            else -> "افتراضي (0)"
                                        }
                                        ChipOption(
                                            label = label,
                                            isSelected = isSelected,
                                            onClick = { draftSettings = draftSettings.copy(hijriAdjustmentDays = adj) }
                                        )
                                    }
                                }
                            }
                        }

                        SettingsTab.IQAMA -> {
                            item {
                                SectionHeader("فارق وقت الإقامة بعد الأذان بالدقائق (Iqama Delays in Minutes)")

                                IqamaAdjustmentRow("صلاة الفجر (Fajr)", draftSettings.fajrIqamaMin) {
                                    draftSettings = draftSettings.copy(fajrIqamaMin = it)
                                }
                                IqamaAdjustmentRow("صلاة الظهر (Duhr)", draftSettings.duhrIqamaMin) {
                                    draftSettings = draftSettings.copy(duhrIqamaMin = it)
                                }
                                IqamaAdjustmentRow("صلاة العصر (Asr)", draftSettings.asrIqamaMin) {
                                    draftSettings = draftSettings.copy(asrIqamaMin = it)
                                }
                                IqamaAdjustmentRow("صلاة المغرب (Maghrib)", draftSettings.maghribIqamaMin) {
                                    draftSettings = draftSettings.copy(maghribIqamaMin = it)
                                }
                                IqamaAdjustmentRow("صلاة العشاء (Isha)", draftSettings.ishaIqamaMin) {
                                    draftSettings = draftSettings.copy(ishaIqamaMin = it)
                                }
                            }
                        }

                        SettingsTab.DISPLAY -> {
                            item {
                                SectionHeader("اتجاه الشاشة والعرض (Screen Orientation Mode)")
                                ScreenOrientationPref.entries.forEach { pref ->
                                    val isSelected = pref == draftSettings.orientationPref
                                    SelectableOptionCard(
                                        title = pref.titleAr,
                                        subtitle = pref.titleEn,
                                        isSelected = isSelected,
                                        onSelect = { draftSettings = draftSettings.copy(orientationPref = pref) }
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                SectionHeader("نمط الثيم والألوان (Color Palette)")
                                ColorThemePref.entries.forEach { theme ->
                                    val isSelected = theme == draftSettings.colorTheme
                                    SelectableOptionCard(
                                        title = theme.titleEn,
                                        subtitle = if (theme == ColorThemePref.GOLD_YELLOW) "مطابق للصورة تماماً" else "لون مخصص",
                                        isSelected = isSelected,
                                        onSelect = { draftSettings = draftSettings.copy(colorTheme = theme) }
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                ToggleSettingRow(
                                    title = "عرض الثواني في الساعة الرئيسية (Show Seconds)",
                                    checked = draftSettings.showSeconds,
                                    onCheckedChange = { draftSettings = draftSettings.copy(showSeconds = it) }
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                ToggleSettingRow(
                                    title = "نظام 24 ساعة (24-Hour Clock Format)",
                                    checked = draftSettings.is24HourFormat,
                                    onCheckedChange = { draftSettings = draftSettings.copy(is24HourFormat = it) }
                                )
                            }
                        }

                        SettingsTab.AUDIO -> {
                            item {
                                SectionHeader("صوت الأذان والتنبيهات (Athan Reciter & Notifications)")

                                AthanVoice.entries.forEach { voice ->
                                    val isSelected = voice == draftSettings.athanVoice
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            SelectableOptionCard(
                                                title = voice.titleAr,
                                                subtitle = voice.titleEn,
                                                isSelected = isSelected,
                                                onSelect = { draftSettings = draftSettings.copy(athanVoice = voice) }
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))

                                        // Quick Test Sound Button with D-Pad focus
                                        val testInteractionSource = remember { MutableInteractionSource() }
                                        val isTestFocused by testInteractionSource.collectIsFocusedAsState()

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isTestFocused) Color.White else Color(0xFF334155))
                                                .border(
                                                    width = if (isTestFocused) 2.dp else 0.dp,
                                                    color = Color(0xFFFFB703),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .dpadFocusable(
                                                    onClick = { onTestSound(voice) },
                                                    interactionSource = testInteractionSource,
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .padding(horizontal = 12.dp, vertical = 14.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "تجربة (Test)",
                                                color = if (isTestFocused) Color.Black else Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = "مستوى صوت الأذان (Volume: ${draftSettings.athanVolumePercent}%) — (استخدم الأسهم ◀ ▶ للضبط):",
                                    color = Color.White,
                                    fontSize = 16.sp
                                )

                                val sliderInteractionSource = remember { MutableInteractionSource() }
                                val isSliderFocused by sliderInteractionSource.collectIsFocusedAsState()

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .border(
                                            width = if (isSliderFocused) 2.dp else 0.dp,
                                            color = Color(0xFFFFB703),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .dpadAdjustable(
                                            onDecrease = {
                                                draftSettings = draftSettings.copy(
                                                    athanVolumePercent = (draftSettings.athanVolumePercent - 5).coerceAtLeast(0)
                                                )
                                            },
                                            onIncrease = {
                                                draftSettings = draftSettings.copy(
                                                    athanVolumePercent = (draftSettings.athanVolumePercent + 5).coerceAtMost(100)
                                                )
                                            }
                                        )
                                        .focusable(interactionSource = sliderInteractionSource)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Slider(
                                        value = draftSettings.athanVolumePercent.toFloat(),
                                        onValueChange = { draftSettings = draftSettings.copy(athanVolumePercent = it.toInt()) },
                                        valueRange = 0f..100f,
                                        steps = 20,
                                        colors = SliderDefaults.colors(
                                            thumbColor = Color(0xFFFFB703),
                                            activeTrackColor = Color(0xFFFFB703),
                                            inactiveTrackColor = Color(0xFF334155)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                ToggleSettingRow(
                                    title = "تفعيل شاشة التنبيه المنبثقة للأذان (Full Screen Athan Popup)",
                                    checked = draftSettings.isAthanAlertPopupEnabled,
                                    onCheckedChange = { draftSettings = draftSettings.copy(isAthanAlertPopupEnabled = it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ToggleSettingRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isFocused) Color(0xFF1E293B) else Color.Transparent)
            .border(
                width = if (isFocused) 2.dp else 0.dp,
                color = Color(0xFFFFB703),
                shape = RoundedCornerShape(10.dp)
            )
            .dpadFocusable(
                onClick = { onCheckedChange(!checked) },
                interactionSource = interactionSource,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 16.sp
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = Color(0xFFFFB703)
            )
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        color = Color(0xFFFFB703),
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun SelectableOptionCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.02f else 1.0f,
        animationSpec = tween(150),
        label = "optScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    isSelected -> Color(0xFF1E293B)
                    isFocused -> Color(0xFF334155)
                    else -> Color(0xFF0F172A)
                }
            )
            .border(
                width = if (isFocused) 2.5.dp else if (isSelected) 2.dp else 1.dp,
                color = when {
                    isFocused -> Color(0xFFFFB703)
                    isSelected -> Color(0xFFFFB703)
                    else -> Color(0xFF334155)
                },
                shape = RoundedCornerShape(12.dp)
            )
            .dpadFocusable(
                onClick = onSelect,
                interactionSource = interactionSource,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color(0xFFFFB703),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun CityChip(
    city: com.example.data.models.IraqiCity,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.08f else 1.0f,
        animationSpec = tween(150),
        label = "chipScale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    isSelected -> Color(0xFFFFB703)
                    isFocused -> Color(0xFF475569)
                    else -> Color(0xFF1E293B)
                }
            )
            .border(
                width = if (isFocused) 2.5.dp else 0.dp,
                color = Color.White,
                shape = RoundedCornerShape(8.dp)
            )
            .dpadFocusable(
                onClick = onSelect,
                interactionSource = interactionSource,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(vertical = 8.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = city.nameAr,
            color = if (isSelected) Color.Black else Color.White,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun ChipOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.08f else 1.0f,
        animationSpec = tween(150),
        label = "chipOptScale"
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    isSelected -> Color(0xFFFFB703)
                    isFocused -> Color(0xFF475569)
                    else -> Color(0xFF1E293B)
                }
            )
            .border(
                width = if (isFocused) 2.5.dp else 1.dp,
                color = if (isFocused) Color.White else Color(0xFF334155),
                shape = RoundedCornerShape(8.dp)
            )
            .dpadFocusable(
                onClick = onClick,
                interactionSource = interactionSource,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.Black else Color.White,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun IqamaAdjustmentRow(
    prayerTitle: String,
    currentMinutes: Int,
    onMinutesChanged: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = prayerTitle,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(5, 10, 12, 15, 20, 25, 30).forEach { mins ->
                val isSelected = currentMinutes == mins
                val interactionSource = remember { MutableInteractionSource() }
                val isFocused by interactionSource.collectIsFocusedAsState()
                val scale by animateFloatAsState(
                    targetValue = if (isFocused) 1.1f else 1.0f,
                    animationSpec = tween(150),
                    label = "iqamaScale"
                )

                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when {
                                isSelected -> Color(0xFFFFB703)
                                isFocused -> Color(0xFF475569)
                                else -> Color(0xFF1E293B)
                            }
                        )
                        .border(
                            width = if (isFocused) 2.dp else 0.dp,
                            color = Color.White,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .dpadFocusable(
                            onClick = { onMinutesChanged(mins) },
                            interactionSource = interactionSource,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+$mins",
                        color = if (isSelected) Color.Black else Color.White,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
