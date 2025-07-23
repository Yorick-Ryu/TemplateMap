package com.yorick.templatemap.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.yorick.templatemap.data.datastore.UserDataSerializer
import com.yorick.templatemap.data.datastore.UserPref
import com.yorick.templatemap.data.repository.UserDataRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val DATA_STORE_FILE_NAME = "user_prefs.pb"

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideDataStore(@ApplicationContext context: Context): DataStore<UserPref> {
        return DataStoreFactory.create(
            serializer = UserDataSerializer,
            produceFile = { context.dataStoreFile(DATA_STORE_FILE_NAME) }
        )
    }

    @Singleton
    @Provides
    fun provideUserDataRepository(dataStore: DataStore<UserPref>): UserDataRepository {
        return UserDataRepository(dataStore)
    }

}