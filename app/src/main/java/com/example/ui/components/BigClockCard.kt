package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.PrayerTimeItem

@Composable
fun BigClockCard(
    currentTimeFormatted: String, // e.g. "05:30:35 PM"
    fullDateFormatted: String, // e.g. "15 August, 2026 — Saturday — 2 Rabi Al-Awwal, 1448"
    cardColor: Color = Color(0xFFFFB703),
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .testTag("big_clock_card"),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Giant Bold Digital Time (05:30:35 PM)
            Text(
                text = currentTimeFormatted,
                color = Color(0xFF0F172A),
                fontSize = 76.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("text_digital_clock")
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Gregorian Date — Day of Week — Hijri Date
            Text(
                text = fullDateFormatted,
                color = Color(0xFF0F172A),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.3.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("text_full_date")
            )
        }
    }
}

@Composable
fun ShurukJumuaBadge(
    title: String, // "Shuruk" or "Jumua"
    time: String, // "05:30 AM" or "12:17 PM"
    modifier: Modifier = Modifier,
    alignEnd: Boolean = false
) {
    Column(
        modifier = modifier.testTag("badge_${title.lowercase()}"),
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        val parts = time.split(" ")
        val digits = parts.getOrNull(0) ?: time
        val amPm = parts.getOrNull(1) ?: ""

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = digits,
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
            if (amPm.isNotEmpty()) {
                Text(
                    text = amPm,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
                )
            }
        }
    }
}
