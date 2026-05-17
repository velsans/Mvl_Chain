package com.tadamaps.mobile

import android.app.Application
import com.tadamaps.mobile.BuildConfig
import com.tadamaps.mobile.security.ReleaseIntegrity
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class MvlApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ReleaseIntegrity.verifyAndEnforce(this)
        if (BuildConfig.LOG_TO_TIMBER) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
