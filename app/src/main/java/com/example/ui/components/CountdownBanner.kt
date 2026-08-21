package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun CountdownBanner(
    prayerName: String, // e.g. "Maghrib"
    secondsRemaining: Long, // e.g. 5415
    isIqamaCountdown: Boolean = false,
    textColor: Color = Color(0xFFFFB703),
    modifier: Modifier = Modifier
) {
    val totalSecs = secondsRemaining.coerceAtLeast(0)
    val hours = totalSecs / 3600
    val minutes = (totalSecs % 3600) / 60
    val seconds = totalSecs % 60

    val timeFormatted = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    val typeLabel = if (isIqamaCountdown) "Iqama in" else "Athan in"

    Row(
        modifier = modifier
            .padding(vertical = 4.dp)
            .testTag("countdown_banner"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Green Mosque Emoji / Icon
        Text(
            text = "🕌",
            fontSize = 24.sp,
            modifier = Modifier.padding(end = 8.dp)
        )

        AnimatedContent(
            targetState = "$prayerName $typeLabel $timeFormatted",
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "countdownText"
        ) { displayText ->
            Text(
                text = displayText,
                color = textColor,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center
            )
        }

        // Green Mosque Emoji / Icon
        Text(
            text = "🕌",
            fontSize = 24.sp,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
