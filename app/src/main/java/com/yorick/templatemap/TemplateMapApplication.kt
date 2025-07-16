package com.yorick.templatemap

import android.app.Application
import com.yorick.common.data.utils.LogUtils
import com.yorick.templatemap.data.utils.BDMapUtils
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TemplateMapApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        LogUtils.initializeLogging(this)
        BDMapUtils.updateMapViewPrivacy(this)
        BDMapUtils.initConfig(this)
    }
}