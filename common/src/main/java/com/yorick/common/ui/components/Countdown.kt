package com.yorick.common.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * eventTimeString example: 2024-10-30 07:00:00 (yyyy-MM-dd HH:mm:ss)
 */
@Composable
fun Countdown(
    eventName: String,
    eventTimeString: String,
    offsetMinutes: Int = 0, // 偏置时间，以分钟为单位
    offsetExplanation: String = "", // 偏置时间的解释
    endText: String = "倒计时结束！"
) {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    // 检查时间格式
    val eventTime: Date? = try {
        formatter.parse(eventTimeString)
    } catch (e: ParseException) {
        null // 如果解析失败，time 设为 null
    }

    // 显示剩余时间
    var remainingTime by remember { mutableStateOf("") }

    var showOffsetExplanation by remember {
        mutableStateOf(offsetMinutes != 0 && offsetExplanation.isNotEmpty())
    }

    // 每秒更新一次
    LaunchedEffect(Unit) {
        while (true) {
            val currentTime = Calendar.getInstance().time

            if (eventTime != null) {
                // 应用偏置时间
                val adjustedTime = Calendar.getInstance().apply {
                    time = eventTime
                    add(Calendar.MINUTE, offsetMinutes) // 添加偏置时间
                }.time

                val remainingMillis = adjustedTime.time - currentTime.time

                if (remainingMillis > 0) {
                    val days = (remainingMillis / (1000 * 60 * 60 * 24)).toInt()
                    val hours = (remainingMillis / (1000 * 60 * 60) % 24).toInt()
                    val minutes = ((remainingMillis / (1000 * 60)) % 60).toInt()
                    val seconds = ((remainingMillis / 1000) % 60).toInt()

                    remainingTime =
                        "距离${eventName}还有: ${days}天 ${hours}小时 ${minutes}分钟 ${seconds}秒"
                } else {
                    showOffsetExplanation = false
                    remainingTime = endText
                }
            } else {
                remainingTime = "时间格式无效"
            }

            delay(1000) // 每秒更新
        }
    }
    if (showOffsetExplanation) {
        Text(text = offsetExplanation)
    }
    Text(text = remainingTime)
}




