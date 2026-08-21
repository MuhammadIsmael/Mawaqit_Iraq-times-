package com.example.ui.util

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Enhanced focusable modifier designed specifically for 10-foot Android TV remote navigation.
 * Provides:
 * 1. High contrast animated border highlight
 * 2. Subtle elevation / scale animation on focus
 * 3. Handles DPAD_CENTER / ENTER / NUMPAD_ENTER key events cleanly
 */
fun Modifier.dpadFocusable(
    onClick: () -> Unit,
    interactionSource: MutableInteractionSource? = null,
    focusedBorderColor: Color = Color(0xFFFFB703),
    focusedBorderWidth: Dp = 3.dp,
    shape: Shape = RoundedCornerShape(12.dp),
    scaleOnFocus: Float = 1.04f,
    enabled: Boolean = true,
    focusRequester: FocusRequester? = null,
    onFocusedChanged: ((Boolean) -> Unit)? = null
): Modifier = composed {
    val currentInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val isFocused by currentInteractionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isFocused) scaleOnFocus else 1.0f,
        animationSpec = tween(durationMillis = 180),
        label = "dpad_scale"
    )

    var baseModifier = this
        .then(
            if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier
        )
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .border(
            width = if (isFocused) focusedBorderWidth else 0.dp,
            color = if (isFocused) focusedBorderColor else Color.Transparent,
            shape = shape
        )
        .onFocusChanged { focusState ->
            onFocusedChanged?.invoke(focusState.isFocused)
        }
        .onPreviewKeyEvent { keyEvent ->
            if (enabled && keyEvent.type == KeyEventType.KeyDown) {
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
        .focusable(enabled = enabled, interactionSource = currentInteractionSource)
        .clickable(
            enabled = enabled,
            interactionSource = currentInteractionSource,
            indication = null,
            onClick = onClick
        )

    baseModifier
}

/**
 * Handles directional DPAD slider adjustment (LEFT decreases, RIGHT increases)
 */
fun Modifier.dpadAdjustable(
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    enabled: Boolean = true
): Modifier = this.onKeyEvent { keyEvent ->
    if (enabled && keyEvent.type == KeyEventType.KeyDown) {
        when (keyEvent.key) {
            Key.DirectionLeft -> {
                onDecrease()
                true
            }
            Key.DirectionRight -> {
                onIncrease()
                true
            }
            else -> false
        }
    } else {
        false
    }
}
