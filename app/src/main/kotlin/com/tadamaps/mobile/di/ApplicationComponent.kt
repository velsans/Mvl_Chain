package com.tadamaps.mobile.di

import android.app.Application
import com.mvlchain.data.di.DatabaseModule
import com.mvlchain.data.di.NetworkModule
import com.mvlchain.data.di.RepositoryBindModule
import com.tadamaps.mobile.core.dispatcher.DispatcherModule
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        DispatcherModule::class,
        DomainUseCaseModule::class,
        RepositoryBindModule::class,
        NetworkModule::class,
        DatabaseModule::class,
        SubcomponentsModule::class,
    ],
)
interface ApplicationComponent {

    fun mviViewModelFactory(): MviViewModelFactory

    fun mainActivitySubcomponentFactory(): MainActivitySubcomponent.Factory

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun build(): ApplicationComponent
    }
}
