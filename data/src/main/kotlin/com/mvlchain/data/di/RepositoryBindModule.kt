package com.mvlchain.data.di

import com.mvlchain.data.repository.AirQualityRepositoryImpl
import com.mvlchain.data.repository.BookRepositoryImpl
import com.mvlchain.data.repository.GeocodingRepositoryImpl
import com.mvlchain.data.repository.LocationPreferenceRepositoryImpl
import com.mvlchain.domain.repository.AirQualityRepository
import com.mvlchain.domain.repository.BookRepository
import com.mvlchain.domain.repository.GeocodingRepository
import com.mvlchain.domain.repository.LocationPreferenceRepository
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class RepositoryBindModule {

    @Binds
    @Singleton
    abstract fun bindAirQualityRepository(impl: AirQualityRepositoryImpl): AirQualityRepository

    @Binds
    @Singleton
    abstract fun bindGeocodingRepository(impl: GeocodingRepositoryImpl): GeocodingRepository

    @Binds
    @Singleton
    abstract fun bindBookRepository(impl: BookRepositoryImpl): BookRepository

    @Binds
    @Singleton
    abstract fun bindLocationPrefs(impl: LocationPreferenceRepositoryImpl): LocationPreferenceRepository
}
