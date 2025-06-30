package com.yorick.templatemap.data.model

import androidx.annotation.Keep
import com.yorick.common.data.model.BaseUserData
import com.yorick.common.data.model.DarkThemeConfig

@Keep
data class UserData(
    // UI
    override val darkThemeConfig: DarkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
    override val useDynamicColor: Boolean = false,

    ) : BaseUserData
