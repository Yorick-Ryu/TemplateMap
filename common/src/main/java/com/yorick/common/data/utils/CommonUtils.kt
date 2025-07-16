package com.yorick.common.data.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.net.toUri
import timber.log.Timber
import java.security.MessageDigest

fun <T : Any> ActivityResultLauncher<T>.safeLaunch(input: T?) {
    if (null == input) {
        Timber.tag("AppUtils").e("safeLaunch(T): input = null")
        return
    }
    val launchResult = kotlin.runCatching {
        launch(input)
    }
    if (launchResult.isFailure) {
        Timber.tag("AppUtils")
            .e("safeLaunch(T),Exception:${launchResult.exceptionOrNull()?.message}")
    }
}

fun getVersionName(context: Context): String {
    return context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "Unknow"
}

/**
 * 打开App权限设置页面
 */
fun openAppPermissionSettingPage(context: Context) {
    val packageName = context.packageName
    try {
        val intent = Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = "package:$packageName".toUri()
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        try {
            // 往设置页面跳
            context.startActivity(Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (ignore: ActivityNotFoundException) {
            // 有些手机跳系统设置也会崩溃
        }
    }
}

object CommonUtils {

    // 添加位图缓存
    private val kilometerMarkerCache = mutableMapOf<String, Bitmap>()

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun getVersionName(context: Context): String {
        return context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "Unknow"
    }
    fun openIgnoreBatteryOptimizations(context: Context) {
        showToast(context, "请找到本应用，并忽略电池优化，如果没跳转代表已经打开电池优化。")
        val packageName = context.packageName
        try {
            val intent = Intent().apply {
                action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                data = "package:$packageName".toUri()
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            try {
                // 往设置页面跳
                context.startActivity(Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            } catch (ignore: ActivityNotFoundException) {
                // 有些手机跳系统设置也会崩溃
            }
        }
    }

    fun String.sha256(): String {
        return MessageDigest
            .getInstance("SHA-256")
            .digest(padStart(128, '0').toByteArray())
            .fold("") { str, it -> str + "%02x".format(it) }
    }

    fun scaleBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        val matrix = Matrix().apply {
            postScale(
                width.toFloat() / bitmap.width,
                height.toFloat() / bitmap.height
            )
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun getAndroidId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    fun getSDCardPtah(): String {
        return Environment.getExternalStorageDirectory().toString()
    }

    fun dialPhoneNumber(context: Context, phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = "tel:$phoneNumber".toUri()
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            showToast(context, "无法拨打电话")
        }
    }

}