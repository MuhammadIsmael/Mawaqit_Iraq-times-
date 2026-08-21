package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FooterBar(
    mosqueId: String = "34383",
    appVersion: String = "v1.33.0-615",
    announcementTicker: String = "",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("footer_bar"),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Bottom Left: ID and QR Code
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.testTag("footer_id_qr")
        ) {
            Text(
                text = "ID $mosqueId",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 2.dp)
            )

            // Stylized Crisp Vector QR Code
            Canvas(
                modifier = Modifier
                    .size(46.dp)
                    .background(Color.White, shape = RoundedCornerShape(4.dp))
                    .padding(3.dp)
            ) {
                val s = size.width
                val col = Color.Black
                val cell = s / 7f

                // Top-Left Finder Pattern
                drawRect(color = col, topLeft = Offset(0f, 0f), size = Size(cell * 3, cell * 3))
                drawRect(color = Color.White, topLeft = Offset(cell * 0.5f, cell * 0.5f), size = Size(cell * 2, cell * 2))
                drawRect(color = col, topLeft = Offset(cell, cell), size = Size(cell, cell))

                // Top-Right Finder Pattern
                drawRect(color = col, topLeft = Offset(cell * 4, 0f), size = Size(cell * 3, cell * 3))
                drawRect(color = Color.White, topLeft = Offset(cell * 4.5f, cell * 0.5f), size = Size(cell * 2, cell * 2))
                drawRect(color = col, topLeft = Offset(cell * 5, cell), size = Size(cell, cell))

                // Bottom-Left Finder Pattern
                drawRect(color = col, topLeft = Offset(0f, cell * 4), size = Size(cell * 3, cell * 3))
                drawRect(color = Color.White, topLeft = Offset(cell * 0.5f, cell * 4.5f), size = Size(cell * 2, cell * 2))
                drawRect(color = col, topLeft = Offset(cell, cell * 5), size = Size(cell, cell))

                // Data dots
                drawRect(color = col, topLeft = Offset(cell * 3.5f, cell * 2f), size = Size(cell * 0.8f, cell * 0.8f))
                drawRect(color = col, topLeft = Offset(cell * 2f, cell * 3.5f), size = Size(cell * 0.8f, cell * 0.8f))
                drawRect(color = col, topLeft = Offset(cell * 4f, cell * 4f), size = Size(cell * 0.8f, cell * 0.8f))
                drawRect(color = col, topLeft = Offset(cell * 5.5f, cell * 5.5f), size = Size(cell * 0.8f, cell * 0.8f))
                drawRect(color = col, topLeft = Offset(cell * 3.5f, cell * 5.5f), size = Size(cell * 0.8f, cell * 0.8f))
            }
        }

        // Middle Announcement Ticker (if present)
        if (announcementTicker.isNotBlank()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x33000000))
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = announcementTicker,
                    color = Color(0xFFE2E8F0),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    modifier = Modifier.basicMarquee()
                )
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        // Bottom Right: MAWAQIT Green Brand Badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF15803D)) // Deep Emerald Green
                .padding(horizontal = 10.dp, vertical = 4.dp)
                .testTag("footer_mawaqit_badge")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Islamic Clock emblem
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(Color.White, shape = CircleShape)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(26.dp)) {
                        val center = Offset(size.width / 2, size.height / 2)
                        val r = size.width / 2

                        // Outer ring
                        drawCircle(
                            color = Color(0xFF15803D),
                            radius = r,
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                        // Clock hands / Dome silhouette in green
                        drawLine(
                            color = Color(0xFF15803D),
                            start = center,
                            end = Offset(center.x, center.y - r * 0.6f),
                            strokeWidth = 2.dp.toPx()
                        )
                        drawLine(
                            color = Color(0xFF15803D),
                            start = center,
                            end = Offset(center.x + r * 0.5f, center.y + r * 0.2f),
                            strokeWidth = 1.5.dp.toPx()
                        )
                        // Hour dots
                        for (i in 0 until 12) {
                            val angle = i * (Math.PI / 6.0)
                            val dotX = (center.x + (r - 3.dp.toPx()) * Math.cos(angle)).toFloat()
                            val dotY = (center.y + (r - 3.dp.toPx()) * Math.sin(angle)).toFloat()
                            drawCircle(
                                color = Color(0xFF15803D),
                                radius = 1.dp.toPx(),
                                center = Offset(dotX, dotY)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Brand text in Arabic & English
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "مواقيت",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 15.sp
                    )
                    Text(
                        text = "MAWAQIT",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp,
                        lineHeight = 12.sp
                    )
                    Text(
                        text = appVersion,
                        color = Color(0xFFBBF7D0),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 10.sp
                    )
                }
            }
        }
    }
}
