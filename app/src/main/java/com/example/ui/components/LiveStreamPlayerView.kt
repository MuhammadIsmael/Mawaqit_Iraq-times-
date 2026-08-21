package com.example.ui.components

import android.media.MediaPlayer
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.VideoView
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.models.LIVE_STREAM_CHANNELS
import com.example.data.models.LiveStreamChannel
import com.example.ui.util.dpadFocusable

@Composable
fun LiveStreamPlayerDialog(
    initialChannel: LiveStreamChannel = LIVE_STREAM_CHANNELS[0],
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedChannel by remember { mutableStateOf(initialChannel) }
    var isBuffering by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }
    var mediaPlayerRef by remember { mutableStateOf<MediaPlayer?>(null) }

    val initialFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        try {
            initialFocusRequester.requestFocus()
        } catch (e: Exception) {
            // ignore
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xF0050811))
            .padding(24.dp)
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown && (keyEvent.key == Key.Back || keyEvent.key == Key.Escape)) {
                    videoViewRef?.stopPlayback()
                    onDismiss()
                    true
                } else {
                    false
                }
            }
            .testTag("live_stream_player_dialog"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF0F172A))
                .border(2.dp, Color(0xFF334155), RoundedCornerShape(24.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Channel Info & Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color(0xFFEF4444), shape = CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LIVE",
                        color = Color(0xFFEF4444),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = selectedChannel.titleAr,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ${selectedChannel.location}",
                        color = Color(0xFF94A3B8),
                        fontSize = 14.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Mute / Unmute Button
                    val muteInteractionSource = remember { MutableInteractionSource() }
                    val isMuteFocused by muteInteractionSource.collectIsFocusedAsState()

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isMuteFocused) Color(0xFFFFB703) else Color(0xFF1E293B))
                            .border(
                                width = if (isMuteFocused) 2.5.dp else 0.dp,
                                color = Color.White,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .dpadFocusable(
                                onClick = {
                                    isMuted = !isMuted
                                    try {
                                        val vol = if (isMuted) 0f else 1f
                                        mediaPlayerRef?.setVolume(vol, vol)
                                    } catch (e: Exception) {
                                        // ignore
                                    }
                                },
                                interactionSource = muteInteractionSource,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp)
                            .testTag("button_stream_mute"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                            contentDescription = "Mute / Unmute",
                            tint = if (isMuteFocused) Color.Black else Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Close Button (Initial Focus)
                    val closeInteractionSource = remember { MutableInteractionSource() }
                    val isCloseFocused by closeInteractionSource.collectIsFocusedAsState()

                    Box(
                        modifier = Modifier
                            .focusRequester(initialFocusRequester)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isCloseFocused) Color(0xFFEF4444) else Color(0xFF1E293B))
                            .border(
                                width = if (isCloseFocused) 2.5.dp else 0.dp,
                                color = Color.White,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .dpadFocusable(
                                onClick = {
                                    videoViewRef?.stopPlayback()
                                    onDismiss()
                                },
                                interactionSource = closeInteractionSource,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp)
                            .testTag("button_stream_close"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Stream",
                            tint = Color.White
                        )
                    }
                }
            }

            // Center: Smooth Video Player Screen
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black)
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    factory = { ctx ->
                        FrameLayout(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            val vv = VideoView(ctx).apply {
                                layoutParams = FrameLayout.LayoutParams(
                                    FrameLayout.LayoutParams.MATCH_PARENT,
                                    FrameLayout.LayoutParams.MATCH_PARENT
                                )
                                setOnPreparedListener { mp ->
                                    mediaPlayerRef = mp
                                    isBuffering = false
                                    hasError = false
                                    val vol = if (isMuted) 0f else 1f
                                    mp.setVolume(vol, vol)
                                    mp.isLooping = true
                                    start()
                                }
                                setOnErrorListener { _, _, _ ->
                                    isBuffering = false
                                    hasError = true
                                    true
                                }
                                setVideoURI(Uri.parse(selectedChannel.streamUrl))
                            }
                            videoViewRef = vv
                            addView(vv)
                        }
                    },
                    update = {
                        isBuffering = true
                        hasError = false
                        videoViewRef?.stopPlayback()
                        videoViewRef?.setVideoURI(Uri.parse(selectedChannel.streamUrl))
                        videoViewRef?.start()
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Buffering Spinner
                if (isBuffering && !hasError) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFFFFB703),
                            strokeWidth = 4.dp,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "جاري الاتصال بالبث المباشر (Connecting Live Feed...)",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Error / Fallback Card
                if (hasError) {
                    val retryInteractionSource = remember { MutableInteractionSource() }
                    val isRetryFocused by retryInteractionSource.collectIsFocusedAsState()

                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xDD1E293B))
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = null,
                            tint = Color(0xFFFFB703),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "البث المباشر غير متاح حالياً أو يتطلب اتصال إنترنت مستقر",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Retry Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isRetryFocused) Color.White else Color(0xFFFFB703))
                                .border(
                                    width = if (isRetryFocused) 2.5.dp else 0.dp,
                                    color = Color.White,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .dpadFocusable(
                                    onClick = {
                                        hasError = false
                                        isBuffering = true
                                        videoViewRef?.setVideoURI(Uri.parse(selectedChannel.backupUrl))
                                        videoViewRef?.start()
                                    },
                                    interactionSource = retryInteractionSource,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = Color.Black
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "إعادة المحاولة عبر الخادم البديل",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom: Channel Selection Row (D-Pad Navigable)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(LIVE_STREAM_CHANNELS) { channel ->
                    val isSelected = channel.id == selectedChannel.id
                    val channelInteractionSource = remember { MutableInteractionSource() }
                    val isChannelFocused by channelInteractionSource.collectIsFocusedAsState()
                    val scale by animateFloatAsState(
                        targetValue = if (isChannelFocused) 1.06f else 1.0f,
                        animationSpec = tween(150),
                        label = "channelScale"
                    )

                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                when {
                                    isSelected -> Color(0xFFFFB703)
                                    isChannelFocused -> Color(0xFF475569)
                                    else -> Color(0xFF1E293B)
                                }
                            )
                            .border(
                                width = if (isChannelFocused) 2.5.dp else 1.dp,
                                color = if (isChannelFocused) Color.White else Color(0xFF334155),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .dpadFocusable(
                                onClick = { selectedChannel = channel },
                                interactionSource = channelInteractionSource,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .testTag("channel_item_${channel.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tv,
                                contentDescription = null,
                                tint = if (isSelected) Color.Black else Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = channel.titleEn,
                                color = if (isSelected) Color.Black else Color.White,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            videoViewRef?.stopPlayback()
            mediaPlayerRef?.release()
        }
    }
}
