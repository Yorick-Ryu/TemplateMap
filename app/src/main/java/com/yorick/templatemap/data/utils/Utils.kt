package com.yorick.templatemap.data.utils

import android.content.Context
import android.graphics.BitmapFactory
import com.baidu.mapapi.map.BitmapDescriptor
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.yorick.common.data.utils.CommonUtils

object Utils {
    const val TAG = "Utils"

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