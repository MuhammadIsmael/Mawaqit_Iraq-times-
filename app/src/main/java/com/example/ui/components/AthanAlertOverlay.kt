package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhonelinkErase
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.PrayerTimeItem
import com.example.ui.util.dpadFocusable
import java.util.Locale

@Composable
fun AthanAlertOverlay(
    prayer: PrayerTimeItem,
    secondsUntilIqama: Long,
    onDismiss: () -> Unit,
    onSilenceAudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val initialFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        try {
            initialFocusRequester.requestFocus()
        } catch (e: Exception) {
            // ignore
        }
    }

    val minsRemaining = (secondsUntilIqama / 60).coerceAtLeast(0)
    val secsRemaining = (secondsUntilIqama % 60).coerceAtLeast(0)
    val formattedIqamaCountdown = String.format(Locale.US, "%02d:%02d", minsRemaining, secsRemaining)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xF0050811))
            .padding(32.dp)
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown && (keyEvent.key == Key.Back || keyEvent.key == Key.Escape)) {
                    onDismiss()
                    true
                } else {
                    false
                }
            }
            .testTag("athan_alert_overlay"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                    )
                )
                .border(2.5.dp, Color(0xFFFFB703), RoundedCornerShape(32.dp))
                .padding(36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Glowing Mosque Icon
            Text(
                text = "🕌",
                fontSize = 54.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Athan Call Notice
            Text(
                text = "حان الآن موعد أذان ${prayer.nameAr} (${prayer.nameEn})",
                color = Color(0xFFFFB703),
                fontSize = 34.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Iqama Countdown Box
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF020617))
                    .border(1.5.dp, Color(0x66FFB703), RoundedCornerShape(20.dp))
                    .padding(vertical = 14.dp, horizontal = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "الوقت المتبقي للإقامة (Iqama Countdown)",
                        color = Color(0xFF94A3B8),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = formattedIqamaCountdown,
                        color = Color.White,
                        fontSize = 52.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Silence Phone Mosque Etiquette Card
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x33EF4444))
                    .border(1.dp, Color(0x99EF4444), RoundedCornerShape(14.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PhonelinkErase,
                    contentDescription = null,
                    tint = Color(0xFFFCA5A5),
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "يرجى وضع الهواتف في الوضع الصامت أو إغلاقها إكراماً لبيوت الله",
                    color = Color(0xFFFEE2E2),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Action Buttons for TV D-Pad
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Silence Athan Audio
                val silenceInteractionSource = remember { MutableInteractionSource() }
                val isSilenceFocused by silenceInteractionSource.collectIsFocusedAsState()
                val silenceScale by animateFloatAsState(
                    targetValue = if (isSilenceFocused) 1.08f else 1.0f,
                    animationSpec = tween(150),
                    label = "silenceScale"
                )

                Box(
                    modifier = Modifier
                        .focusRequester(initialFocusRequester)
                        .graphicsLayer {
                            scaleX = silenceScale
                            scaleY = silenceScale
                        }
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSilenceFocused) Color(0xFFFFB703) else Color(0xFF334155))
                        .border(
                            width = if (isSilenceFocused) 3.dp else 0.dp,
                            color = Color.White,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .dpadFocusable(
                            onClick = onSilenceAudio,
                            interactionSource = silenceInteractionSource,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                        .testTag("button_silence_athan"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VolumeMute,
                            contentDescription = "Mute Sound",
                            tint = if (isSilenceFocused) Color.Black else Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "كتم الصوت (Mute)",
                            color = if (isSilenceFocused) Color.Black else Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Dismiss / Close
                val dismissInteractionSource = remember { MutableInteractionSource() }
                val isDismissFocused by dismissInteractionSource.collectIsFocusedAsState()
                val dismissScale by animateFloatAsState(
                    targetValue = if (isDismissFocused) 1.08f else 1.0f,
                    animationSpec = tween(150),
                    label = "dismissScale"
                )

                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = dismissScale
                            scaleY = dismissScale
                        }
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDismissFocused) Color.White else Color(0xFF1E293B))
                        .border(
                            width = if (isDismissFocused) 3.dp else 1.dp,
                            color = if (isDismissFocused) Color(0xFFFFB703) else Color(0xFF475569),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .dpadFocusable(
                            onClick = onDismiss,
                            interactionSource = dismissInteractionSource,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 28.dp, vertical = 12.dp)
                        .testTag("button_dismiss_athan"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = if (isDismissFocused) Color.Black else Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "إغلاق الشاشة (Close)",
                            color = if (isDismissFocused) Color.Black else Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
