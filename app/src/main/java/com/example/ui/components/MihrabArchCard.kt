package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.PrayerTimeItem

/**
 * Creates the authentic Islamic Mihrab / Mosque Arch Path
 */
fun createMihrabArchPath(width: Float, height: Float, cornerRadius: Float): Path {
    val path = Path()
    val archApexY = 0f
    val shoulderY = height * 0.28f // where arch curves meet the vertical sides
    val midX = width / 2f
    val r = cornerRadius

    // Start at bottom-left corner with rounded bottom
    path.moveTo(0f, height - r)
    path.quadraticTo(0f, height, r, height)
    path.lineTo(width - r, height)
    path.quadraticTo(width, height, width, height - r)

    // Go up right vertical edge
    path.lineTo(width, shoulderY)

    // Right Arch curve to apex: Compound Islamic arch with pointed peak
    path.cubicTo(
        width * 0.95f, shoulderY * 0.45f,
        midX + width * 0.12f, shoulderY * 0.15f,
        midX, archApexY
    )

    // Left Arch curve from apex to left shoulder
    path.cubicTo(
        midX - width * 0.12f, shoulderY * 0.15f,
        width * 0.05f, shoulderY * 0.45f,
        0f, shoulderY
    )

    // Close path down left edge
    path.lineTo(0f, height - r)
    path.close()
    return path
}

@Composable
fun MihrabArchPrayerCard(
    prayer: PrayerTimeItem,
    isActive: Boolean,
    primaryColor: Color,
    modifier: Modifier = Modifier,
    heightDp: Dp = 190.dp,
    onClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val cardScale by animateFloatAsState(
        targetValue = if (isFocused) 1.05f else 1.0f,
        animationSpec = tween(180),
        label = "cardScale"
    )

    val cardBgColor by animateColorAsState(
        targetValue = when {
            isActive -> primaryColor
            isFocused -> Color(0xFF1E293B)
            else -> Color(0x99000000)
        },
        animationSpec = tween(300),
        label = "bgColor"
    )

    val strokeColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color(0xFFFFB703)
            isActive -> Color.White
            else -> Color.White
        },
        animationSpec = tween(300),
        label = "strokeColor"
    )

    val nameColor = when {
        isActive -> Color.White
        else -> Color.White
    }

    val timeColor = when {
        isActive -> Color.White
        else -> Color.White
    }

    val iqamaColor = when {
        isActive -> Color(0xFF1C1917)
        else -> Color.White
    }

    Box(
        modifier = modifier
            .height(heightDp)
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            }
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.key) {
                        Key.DirectionCenter, Key.Enter, Key.NumPadEnter -> {
                            onClick()
                            true
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }
            .focusable(interactionSource = interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("prayer_card_${prayer.type.name.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        // Canvas drawing Mihrab background and outer border
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cornerR = 24.dp.toPx()

            val archPath = createMihrabArchPath(w, h, cornerR)

            // Draw Card Background Fill
            drawPath(
                path = archPath,
                color = cardBgColor,
                style = Fill
            )

            // Draw Card Mihrab Border (thicker and glowing gold when focused on TV)
            val strokeWidthPx = if (isFocused) 4.5.dp.toPx() else 3.dp.toPx()
            drawPath(
                path = archPath,
                color = strokeColor,
                style = Stroke(
                    width = strokeWidthPx,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // If active, draw subtle inner gold glow
            if (isActive) {
                drawPath(
                    path = archPath,
                    color = Color.White.copy(alpha = 0.15f),
                    style = Fill
                )
            }
        }

        // Inner Content matching exact screenshot typography
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 28.dp, bottom = 12.dp, start = 8.dp, end = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Prayer Name (e.g. Fajr, Duhr, Asr, Maghrib, Isha)
            Text(
                text = prayer.nameEn,
                color = nameColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("text_prayer_name")
            )

            // Prayer Time (e.g. 03:51 AM)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val parts = prayer.timeFormatted.split(" ")
                val timeDigits = parts.getOrNull(0) ?: prayer.timeFormatted
                val amPm = parts.getOrNull(1) ?: ""

                Text(
                    text = "$timeDigits $amPm",
                    color = timeColor,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("text_prayer_time")
                )
            }

            // Iqama offset badge (e.g. +25, +12, +8)
            Text(
                text = "+${prayer.iqamaOffsetMinutes}",
                color = iqamaColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("text_prayer_iqama")
            )
        }
    }
}
