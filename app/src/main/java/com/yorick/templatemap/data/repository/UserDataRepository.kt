package com.yorick.templatemap.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import com.yorick.common.data.model.DarkThemeConfig
import com.yorick.templatemap.data.datastore.DarkThemeConfigProto
import com.yorick.templatemap.data.datastore.UserPref
import com.yorick.templatemap.data.model.UserData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class UserDataRepository(
    private val userPrefStore: DataStore<UserPref>
) {

    companion object {
        const val TAG: String = "UserDataRepo"
    }

    val userDataFlow: Flow<UserData> = userPrefStore.data.map { prefs ->
        UserData(
            darkThemeConfig = when (prefs.darkThemeConfig) {
                null,
                DarkThemeConfigProto.DARK_THEME_CONFIG_UNSPECIFIED,
                DarkThemeConfigProto.UNRECOGNIZED,
                DarkThemeConfigProto.DARK_THEME_CONFIG_FOLLOW_SYSTEM -> DarkThemeConfig.FOLLOW_SYSTEM

                DarkThemeConfigProto.DARK_THEME_CONFIG_LIGHT -> DarkThemeConfig.LIGHT

                DarkThemeConfigProto.DARK_THEME_CONFIG_DARK -> DarkThemeConfig.DARK
            },
            useDynamicColor = prefs.useDynamicColor
        )
    }.catch { exception ->
        if (exception is IOException) {
            Log.e(TAG, "Error reading user preferences.", exception)
        } else {
            throw exception
        }
    }

    suspend fun setUserDate(userData: UserData) {
        userPrefStore.updateData { prefs ->
            prefs.toBuilder()
                .setDarkThemeConfig(
                    when (userData.darkThemeConfig) {
                        DarkThemeConfig.FOLLOW_SYSTEM -> DarkThemeConfigProto.DARK_THEME_CONFIG_FOLLOW_SYSTEM
                        DarkThemeConfig.LIGHT -> DarkThemeConfigProto.DARK_THEME_CONFIG_LIGHT
                        DarkThemeConfig.DARK -> DarkThemeConfigProto.DARK_THEME_CONFIG_DARK
                    }
                )
                .setUseDynamicColor(userData.useDynamicColor)
                .build()
        }
    }
}