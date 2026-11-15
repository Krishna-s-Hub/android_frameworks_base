package com.android.systemui.brightness.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// RCOS_VERTICAL_SLIDERS_V10
@Composable
fun RadiantVerticalSlider(
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    symbol: String,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var heightPx by remember { mutableFloatStateOf(1f) }
    val low = valueRange.start
    val high = valueRange.endInclusive
    val span = (high - low).coerceAtLeast(0.001f)
    val fraction = ((value - low) / span).coerceIn(0f, 1f)
    val shape = RoundedCornerShape(27.dp)

    val latestValue by
        androidx.compose.runtime.rememberUpdatedState(value)
    val latestChange by
        androidx.compose.runtime.rememberUpdatedState(onValueChange)
    val latestFinished by
        androidx.compose.runtime.rememberUpdatedState(onValueChangeFinished)

    var gestureValue by remember {
        mutableFloatStateOf(value)
    }


    Box(
        modifier = modifier
            .width(86.dp)
            .height(190.dp)
            .clip(shape)
            .background(Color(0x66343A44))
            .border(1.dp, Color.White.copy(alpha = 0.14f), shape)
            .onSizeChanged { heightPx = it.height.toFloat() }
            .semantics {
                progressBarRangeInfo =
                    ProgressBarRangeInfo(value.coerceIn(low, high), valueRange)
                setProgress {
                    if (enabled) {
                        onValueChange(it.coerceIn(low, high))
                        onValueChangeFinished()
                    }
                    enabled
                }
            }
            // RCOS_RELATIVE_GESTURE_V12
            // Ignore taps; only finger movement changes the value.
            .pointerInput(enabled, heightPx, low, high) {
                detectDragGestures(
                    onDragStart = {
                        gestureValue = latestValue
                    },
                    onDragEnd = {
                        if (enabled) latestFinished()
                    },
                    onDragCancel = {
                        if (enabled) latestFinished()
                    },
                ) { change, dragAmount ->
                    if (enabled) {
                        gestureValue = (
                            gestureValue -
                                dragAmount.y /
                                    heightPx.coerceAtLeast(1f) * span
                        ).coerceIn(low, high)

                        latestChange(gestureValue)
                        change.consume()
                    }
                }
            },
        contentAlignment = Alignment.BottomCenter,
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(4.dp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(fraction)
                    .clip(RoundedCornerShape(23.dp))
                    .background(Color(0xF0F4F5F9))
            )
        }

        Text(
            text = symbol,
            fontSize = 23.sp,
            color = if (fraction > 0.18f) Color(0xFF272A30) else Color.White,
            modifier = Modifier.padding(bottom = 15.dp),
        )
    }
}
