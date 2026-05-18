package com.mvlchain.data.di

import android.app.Application
import androidx.room.Room
import com.mvlchain.data.local.db.MvlLocationDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMvlLocationDatabase(application: Application): MvlLocationDatabase =
        Room.databaseBuilder(
            application,
            MvlLocationDatabase::class.java,
            "mvl_location.db",
        ).fallbackToDestructiveMigration().build()

    @Provides
    fun provideCachedMapLocationDao(db: MvlLocationDatabase) = db.cachedMapLocationDao()

    @Provides
    fun provideSlotNicknameDao(db: MvlLocationDatabase) = db.slotNicknameDao()
}
