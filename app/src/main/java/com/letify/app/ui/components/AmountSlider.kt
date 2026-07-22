package com.letify.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.letify.app.ui.theme.Letify
import kotlin.math.roundToInt

/**
 * Pill-shaped draggable slider for picking a quantity (e.g. ml of water).
 * Replaces a static grid of quick-add buttons: drag (or tap anywhere on)
 * the track to move the thumb; the current value is shown in a small
 * speech-bubble that tracks the thumb's x position, mirroring the
 * "star badge over a pill slider" reference the design was based on.
 */
@Composable
fun AmountSlider(
    valueMl: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    maxMl: Int = 1000,
    stepMl: Int = 50,
    accentColor: Color = Letify.colors.accent,
    unitLabel: String = "мл",
) {
    val density = LocalDensity.current
    var trackWidthPx by remember { mutableFloatStateOf(0f) }
    var bubbleWidthPx by remember { mutableStateOf(0) }
    val thumbSizeDp = 40.dp
    val thumbSizePx = with(density) { thumbSizeDp.toPx() }

    val fraction = (valueMl.toFloat() / maxMl).coerceIn(0f, 1f)
    val usableWidth = (trackWidthPx - thumbSizePx).coerceAtLeast(0f)
    val thumbCenterX = thumbSizePx / 2f + usableWidth * fraction

    fun updateFromX(x: Float) {
        if (trackWidthPx <= 0f || usableWidth <= 0f) return
        val f = ((x - thumbSizePx / 2f) / usableWidth).coerceIn(0f, 1f)
        val raw = (f * maxMl).roundToInt()
        val snapped = ((raw + stepMl / 2) / stepMl) * stepMl
        onValueChange(snapped.coerceIn(0, maxMl))
    }

    Column(modifier) {
        // Value bubble — floats above the track, its x tracks the thumb.
        Box(Modifier.fillMaxWidth().height(40.dp)) {
            if (trackWidthPx > 0f) {
                val bubbleX = with(density) {
                    (thumbCenterX - bubbleWidthPx / 2f).coerceIn(0f, trackWidthPx - bubbleWidthPx).toDp()
                }
                Box(
                    Modifier
                        .offset(x = bubbleX)
                        .onSizeChanged { bubbleWidthPx = it.width },
                ) {
                    ValueBubble(text = "$valueMl $unitLabel", color = accentColor)
                }
            }
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(52.dp)
                .onSizeChanged { trackWidthPx = it.width.toFloat() }
                .background(Letify.colors.container, RoundedCornerShape(999.dp))
                .pointerInput(maxMl, stepMl) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset -> updateFromX(offset.x) },
                        onHorizontalDrag = { change, _ ->
                            change.consume()
                            updateFromX(change.position.x)
                        },
                    )
                },
        ) {
            if (trackWidthPx > 0f) {
                val fillWidthDp = with(density) { (thumbCenterX + thumbSizePx / 2f).toDp() }
                Box(
                    Modifier
                        .fillMaxHeight()
                        .width(fillWidthDp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(accentColor),
                )
                val thumbOffsetDp = with(density) { (thumbCenterX - thumbSizePx / 2f).toDp() }
                Box(
                    Modifier
                        .align(Alignment.CenterStart)
                        .offset(x = thumbOffsetDp)
                        .size(thumbSizeDp)
                        .shadow(3.dp, CircleShape)
                        .background(Color.White, CircleShape),
                )
            }
        }
    }
}

/** Small rounded speech-bubble with a triangular tail, used to show the
 *  live value above the [AmountSlider] thumb. */
@Composable
private fun ValueBubble(text: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .background(color, RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Text(text, color = Color.White, style = Letify.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
        Box(
            Modifier
                .offset(y = (-5).dp)
                .size(10.dp)
                .graphicsLayer { rotationZ = 45f }
                .background(color, RoundedCornerShape(2.dp)),
        )
    }
}
