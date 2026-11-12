package com.talha.depremtakip.di

import android.content.Context
import androidx.room.Room
import com.talha.depremtakip.data.local.EarthquakeDao
import com.talha.depremtakip.data.local.EarthquakeDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): EarthquakeDatabase =
        Room.databaseBuilder(context, EarthquakeDatabase::class.java, "deprem-takip.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideEarthquakeDao(database: EarthquakeDatabase): EarthquakeDao =
        database.earthquakeDao()
}
