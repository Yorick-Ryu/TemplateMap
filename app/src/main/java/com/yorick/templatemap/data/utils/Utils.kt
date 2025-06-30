package com.yorick.templatemap.data.utils

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import com.baidu.mapapi.map.BitmapDescriptor
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.yorick.common.data.utils.CommonUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object Utils {
    const val TAG = "Utils"

    fun isEventTimeReached(eventTimeString: String, offsetMinutes: Int): Boolean {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        // 解析事件时间
        val eventTime: Date? = try {
            formatter.parse(eventTimeString)
        } catch (e: Exception) {
            Log.e("TAG", "时间解析失败: $e")
            return false // 如果解析失败，返回 false
        }

        // 当前时间
        val currentTime = Calendar.getInstance().time

        // 应用偏置时间
        val adjustedEventTime = Calendar.getInstance().apply {
            if (eventTime != null) {
                time = eventTime
            }
            add(Calendar.MINUTE, offsetMinutes) // 添加偏置时间
        }.time

        // 判断事件时间是否到达
        return currentTime >= adjustedEventTime
    }

    fun getAsset(context: Context, assetName: String): BitmapDescriptor? {
        val assetsStream = context.assets.open(assetName)
        return BitmapFactory.decodeStream(assetsStream)?.let {
            BitmapDescriptorFactory.fromBitmap(it)
        }
    }

    fun getDrawableWithScaled(
        context: Context,
        vectorResId: Int,
        width: Int = 40,
        height: Int = 40
    ): BitmapDescriptor {
        val originalBitmap =
            BitmapFactory.decodeResource(context.resources, vectorResId)
        val scaledBitmap = CommonUtils.scaleBitmap(originalBitmap, width, height) // 调整图标尺寸
        return BitmapDescriptorFactory.fromBitmap(scaledBitmap)
    }

    fun getDrawable(
        context: Context,
        vectorResId: Int,
    ): BitmapDescriptor {
        val bitmap =
            BitmapFactory.decodeResource(context.resources, vectorResId)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

}