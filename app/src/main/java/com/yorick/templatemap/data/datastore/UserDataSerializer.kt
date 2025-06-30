package com.yorick.templatemap.data.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.yorick.common.data.model.DarkThemeConfig
import com.yorick.templatemap.data.model.UserData
import java.io.InputStream
import java.io.OutputStream

object UserDataSerializer : Serializer<UserPref> {
    private val userData = UserData()
    override val defaultValue: UserPref =
        UserPref.getDefaultInstance().toBuilder()
            .setDarkThemeConfig(
                when (userData.darkThemeConfig) {
                    DarkThemeConfig.FOLLOW_SYSTEM -> DarkThemeConfigProto.DARK_THEME_CONFIG_FOLLOW_SYSTEM
                    DarkThemeConfig.LIGHT -> DarkThemeConfigProto.DARK_THEME_CONFIG_LIGHT
                    DarkThemeConfig.DARK -> DarkThemeConfigProto.DARK_THEME_CONFIG_DARK
                }
            )
            .setUseDynamicColor(userData.useDynamicColor)
            .build()

    override suspend fun readFrom(input: InputStream): UserPref {
        try {
            return UserPref.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot rezad proto.", exception)
        }
    }

    override suspend fun writeTo(t: UserPref, output: OutputStream) = t.writeTo(output)

}