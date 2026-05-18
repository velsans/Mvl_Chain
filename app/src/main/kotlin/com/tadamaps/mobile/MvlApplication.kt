package com.tadamaps.mobile

import android.app.Application
import com.tadamaps.mobile.BuildConfig
import com.tadamaps.mobile.di.ApplicationComponent
import com.tadamaps.mobile.di.DaggerApplicationComponent
import com.tadamaps.mobile.security.ReleaseIntegrity
import timber.log.Timber

class MvlApplication : Application() {

    lateinit var applicationComponent: ApplicationComponent
        private set

    override fun onCreate() {
        super.onCreate()
        applicationComponent = DaggerApplicationComponent.builder()
            .application(this)
            .build()
        ReleaseIntegrity.verifyAndEnforce(this)
        if (BuildConfig.LOG_TO_TIMBER) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
