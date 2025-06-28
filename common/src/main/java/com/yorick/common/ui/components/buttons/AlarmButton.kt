package com.yorick.common.ui.components.buttons

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yorick.common.R
import com.yorick.common.ui.components.ConfirmDialog
import kotlinx.coroutines.launch

@Composable
fun AlarmButton(
    modifier: Modifier = Modifier,
    alarmState: ButtonState = ButtonState.Idle,
    onAlarmTriggered: () -> Unit = {},
    onAlarmStop: () -> Unit = {},
    enabled: Boolean = true
) {
    var showDialog by remember { mutableStateOf(false) }
    var buttonState by remember { mutableStateOf(alarmState) }
    var progress by remember { mutableFloatStateOf(0f) }
    val textMeasurer = rememberTextMeasurer()
    val scope = rememberCoroutineScope()

    LaunchedEffect(alarmState) {
        buttonState = alarmState
    }

    val progressAnimation = remember { Animatable(0f) }

    // 水波纹动画状态
    val rippleAnimatable = remember { Animatable(0f) }
    val secondRippleAnimatable = remember { Animatable(0.5f) }

    val density = LocalDensity.current
    val buttonSize = 80.dp
    val buttonSizePx = with(density) { buttonSize.toPx() }

    // 重置函数
    fun reset() {
        buttonState = ButtonState.Idle
        scope.launch {
            progressAnimation.snapTo(0f)
            rippleAnimatable.snapTo(0f)
            secondRippleAnimatable.snapTo(0.5f)
            progress = 0f
        }
    }

    // 触发状态下的水波纹动画
    LaunchedEffect(buttonState) {

        when (buttonState) {
            ButtonState.Triggered -> {
                // 添加触发时的震动
//                context.vibrate(120L)
                // 两个交替的水波纹动画
                launch {
                    while (buttonState == ButtonState.Triggered) {
                        rippleAnimatable.animateTo(
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(2000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            )
                        )
                    }
                }
                launch {
                    while (buttonState == ButtonState.Triggered) {
                        secondRippleAnimatable.animateTo(
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(2000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            )
                        )
                    }
                }
                onAlarmTriggered()
            }

            ButtonState.Idle -> {
                progressAnimation.snapTo(0f)
                rippleAnimatable.snapTo(0f)
                secondRippleAnimatable.snapTo(0.5f)
                progress = 0f
            }

            else -> {}
        }
    }

    if (showDialog) {
        ConfirmDialog(
            title = stringResource(id = R.string.confirm),
            content = "你确定要停止报警吗？",
            onConfirm = {
                onAlarmStop()
                reset()
                showDialog = false
            },
            onDismiss = { showDialog = false },
        )
    }

    Box(
        modifier = modifier
            .size(buttonSize)
            .pointerInput(enabled) {
                detectTapGestures(
                    onPress = {
                        if (buttonState == ButtonState.Triggered) {
                            showDialog = true
                            return@detectTapGestures
                        }

                        buttonState = ButtonState.Pressed
                        scope.launch {
                            progressAnimation.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(2000)
                            ) {
                                progress = this.value
                                if (progress >= 1f) {
                                    buttonState = ButtonState.Triggered
                                }
                            }
                        }

                        tryAwaitRelease()

                        if (buttonState != ButtonState.Triggered) {
                            reset()
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerOffset = Offset(buttonSizePx / 2, buttonSizePx / 2)

            // 绘制水波纹
            if (buttonState == ButtonState.Triggered) {
                // 第一个水波纹
                drawCircle(
                    color = Color.Red.copy(alpha = (1f - rippleAnimatable.value) * 0.3f),
                    radius = buttonSizePx * (0.5f + rippleAnimatable.value * 0.5f),
                    center = centerOffset
                )

                // 第二个水波纹
                drawCircle(
                    color = Color.Red.copy(alpha = (1f - secondRippleAnimatable.value) * 0.3f),
                    radius = buttonSizePx * (0.5f + secondRippleAnimatable.value * 0.5f),
                    center = centerOffset
                )
            }

            // 绘制背景圆
            drawCircle(
                color = if (enabled) {
                    when (buttonState) {
                        ButtonState.Triggered -> Color.Red.copy(alpha = 0.2f)
                        else -> Color.Red.copy(alpha = 0.1f)
                    }
                } else {
                    Color.Gray.copy(alpha = 0.1f)
                },
                radius = buttonSizePx / 2,
                center = centerOffset
            )

            // 绘制进度环
            if (buttonState != ButtonState.Triggered) {
                drawArc(
                    color = if (enabled) Color.Red else Color.Gray,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    style = Stroke(
                        width = 4.dp.toPx(),
                        cap = StrokeCap.Round
                    ),
                    size = Size(buttonSizePx, buttonSizePx)
                )
            }

            // 绘制中心按钮
            drawCircle(
                color = when {
                    !enabled -> Color.Gray.copy(alpha = 0.6f)
                    buttonState == ButtonState.Triggered -> Color.Red
                    else -> Color.Red.copy(alpha = 0.6f)
                },
                radius = buttonSizePx / 3,
                center = centerOffset
            )

            // Draw SOS text
            val textStyle = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            val textLayoutResult = textMeasurer.measure(
                text = "SOS",
                style = textStyle
            )

            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = center.copy(
                    x = center.x - textLayoutResult.size.width / 2,
                    y = center.y - textLayoutResult.size.height / 2
                ),
                color = Color.White
            )
        }
    }
}

enum class ButtonState {
    Idle,
    Pressed,
    Triggered
}

@Preview(
    name = "Alarm Button Preview",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun AlarmButtonPreview() {
    Box(
        modifier = Modifier
            .size(200.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        AlarmButton(
            onAlarmTriggered = {}
        )
    }
}

@Preview(
    name = "Alarm Button Triggered Preview",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun AlarmButtonTriggeredPreview() {
    Box(
        modifier = Modifier
            .size(200.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        var triggered by remember { mutableStateOf(true) }
        AlarmButton(
            onAlarmTriggered = { triggered = true },
            enabled = !triggered
        )
    }
}
