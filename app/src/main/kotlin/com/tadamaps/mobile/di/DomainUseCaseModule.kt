package com.tadamaps.mobile.di

import com.mvlchain.domain.usecase.BuildMapLocationUseCase
import com.mvlchain.domain.usecase.CreateBookingUseCase
import com.mvlchain.domain.usecase.GetAirQualityUseCase
import com.mvlchain.domain.usecase.GetBookingHistoryUseCase
import com.mvlchain.domain.usecase.ObserveNicknameUseCase
import com.mvlchain.domain.usecase.ReverseGeocodeUseCase
import com.mvlchain.domain.usecase.SaveNicknameUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DomainUseCaseModule {

    @Provides
    @Singleton
    fun provideGetAirQualityUseCase(
        uc: com.mvlchain.domain.repository.AirQualityRepository,
    ) = GetAirQualityUseCase(uc)

    @Provides
    @Singleton
    fun provideReverseGeocodeUseCase(
        uc: com.mvlchain.domain.repository.GeocodingRepository,
    ) = ReverseGeocodeUseCase(uc)

    @Provides
    @Singleton
    fun provideBuildMapLocation(
        air: GetAirQualityUseCase,
        geo: ReverseGeocodeUseCase,
        prefs: com.mvlchain.domain.repository.LocationPreferenceRepository,
    ) = BuildMapLocationUseCase(air, geo, prefs)

    @Provides
    @Singleton
    fun provideCreateBooking(repo: com.mvlchain.domain.repository.BookRepository) =
        CreateBookingUseCase(repo)

    @Provides
    @Singleton
    fun provideHistory(repo: com.mvlchain.domain.repository.BookRepository) =
        GetBookingHistoryUseCase(repo)

    @Provides
    @Singleton
    fun provideSaveNickname(prefs: com.mvlchain.domain.repository.LocationPreferenceRepository) =
        SaveNicknameUseCase(prefs)

    @Provides
    @Singleton
    fun provideObserveNickname(prefs: com.mvlchain.domain.repository.LocationPreferenceRepository) =
        ObserveNicknameUseCase(prefs)
}
