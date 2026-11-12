package com.talha.depremtakip.di

import com.talha.depremtakip.data.local.SettingsRepositoryImpl
import com.talha.depremtakip.data.repository.EarthquakeRepositoryImpl
import com.talha.depremtakip.domain.repository.EarthquakeRepository
import com.talha.depremtakip.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindEarthquakeRepository(impl: EarthquakeRepositoryImpl): EarthquakeRepository

    @Binds
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
