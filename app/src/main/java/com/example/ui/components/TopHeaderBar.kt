package com.example.ui.components

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.util.dpadFocusable

@Composable
fun TopHeaderBar(
    mosqueName: String,
    cityName: String,
    onMenuClick: () -> Unit,
    onStreamsClick: () -> Unit,
    onSoundTestClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("top_header_bar"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Menu Icon + Online Badge
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hamburger Menu Button (Android TV D-Pad Focusable with Gold Glow)
            val menuInteractionSource = remember { MutableInteractionSource() }
            val isMenuFocused by menuInteractionSource.collectIsFocusedAsState()
            val menuScale by animateFloatAsState(
                targetValue = if (isMenuFocused) 1.08f else 1.0f,
                animationSpec = tween(150),
                label = "menuScale"
            )

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = menuScale
                        scaleY = menuScale
                    }
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isMenuFocused) Color(0x66FFB703) else Color(0x22FFFFFF))
                    .border(
                        width = if (isMenuFocused) 2.5.dp else 0.dp,
                        color = if (isMenuFocused) Color(0xFFFFB703) else Color.Transparent,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .dpadFocusable(
                        onClick = onMenuClick,
                        interactionSource = menuInteractionSource,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(8.dp)
                    .testTag("button_menu"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu and Settings",
                    tint = if (isMenuFocused) Color(0xFFFFB703) else Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Green Dot + Online text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.testTag("indicator_online")
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color(0xFF22C55E), shape = CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Online",
                    color = Color(0xFF4ADE80),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Center: Mosque Name - City Name (Clean Bold White typography)
        Text(
            text = "$mosqueName - $cityName",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
                .testTag("text_mosque_title")
        )

        // Right Action Shortcuts (Live Streams, Sound Test, Settings)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HeaderQuickButton(
                icon = Icons.Default.Videocam,
                label = "بث مباشر",
                contentDescription = "Holy Shrine Live Streams",
                onClick = onStreamsClick,
                testTag = "button_live_streams"
            )

            HeaderQuickButton(
                icon = Icons.Default.VolumeUp,
                label = "فحص الأذان",
                contentDescription = "Test Athan Sound",
                onClick = onSoundTestClick,
                testTag = "button_sound_test"
            )

            HeaderQuickButton(
                icon = Icons.Default.Settings,
                label = "الإعدادات",
                contentDescription = "Open Settings",
                onClick = onMenuClick,
                testTag = "button_header_settings"
            )
        }
    }
}

@Composable
fun HeaderQuickButton(
    icon: ImageVector,
    label: String,
    contentDescription: String,
    onClick: () -> Unit,
    testTag: String
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.08f else 1.0f,
        animationSpec = tween(150),
        label = "btnScale"
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(10.dp))
            .background(if (isFocused) Color(0x66FFB703) else Color(0x22FFFFFF))
            .border(
                width = if (isFocused) 2.5.dp else 1.dp,
                color = if (isFocused) Color(0xFFFFB703) else Color(0x33FFFFFF),
                shape = RoundedCornerShape(10.dp)
            )
            .dpadFocusable(
                onClick = onClick,
                interactionSource = interactionSource,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (isFocused) Color(0xFFFFB703) else Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
