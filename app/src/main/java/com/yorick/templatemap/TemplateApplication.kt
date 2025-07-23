package com.yorick.templatemap

import android.app.Application
import com.yorick.common.data.utils.LogUtils
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TemplateApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        LogUtils.initializeLogging(this)
    }
}