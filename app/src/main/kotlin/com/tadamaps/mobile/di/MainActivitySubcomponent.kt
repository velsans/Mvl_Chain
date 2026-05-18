package com.tadamaps.mobile.di

import com.tadamaps.mobile.MainActivity
import dagger.BindsInstance
import dagger.Subcomponent

/**
 * Activity-scoped subcomponent (demonstrates Dagger **Subcomponents** for interview / lead discussions).
 * Expand with `@ActivityScope` bindings when you need activity-scoped deps.
 */
@ActivityScope
@Subcomponent
interface MainActivitySubcomponent {
    fun inject(activity: MainActivity)

    @Subcomponent.Factory
    interface Factory {
        fun create(@BindsInstance activity: MainActivity): MainActivitySubcomponent
    }
}
