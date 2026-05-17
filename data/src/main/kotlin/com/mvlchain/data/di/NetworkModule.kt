package com.mvlchain.data.di

import retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.mvlchain.data.BuildConfig
import com.mvlchain.data.remote.api.AqiApi
import com.mvlchain.data.remote.api.BookApi
import com.mvlchain.data.remote.api.GeocodingApi
import com.mvlchain.data.remote.interceptor.MockBooksInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        json: Json,
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(MockBooksInterceptor(json))

        if (BuildConfig.ENABLE_HTTP_LOGGING) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            builder.addInterceptor(logging)
        }
        return builder.build()
    }

    @Provides
    @Singleton
    fun provideAqiApi(client: OkHttpClient, json: Json): AqiApi =
        Retrofit.Builder()
            .baseUrl("https://api.waqi.info/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(AqiApi::class.java)

    @Provides
    @Singleton
    fun provideGeocodingApi(client: OkHttpClient, json: Json): GeocodingApi =
        Retrofit.Builder()
            .baseUrl("https://api.bigdatacloud.net/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(GeocodingApi::class.java)

    @Provides
    @Singleton
    fun provideBookApi(client: OkHttpClient, json: Json): BookApi =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BOOKS_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(BookApi::class.java)
}
