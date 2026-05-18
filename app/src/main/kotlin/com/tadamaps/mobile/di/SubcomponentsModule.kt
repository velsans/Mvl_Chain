package com.tadamaps.mobile.di

import dagger.Module

/**
 * Registers activity subcomponents on the application graph (Dagger requires this on a @Module).
 */
@Module(subcomponents = [MainActivitySubcomponent::class])
object SubcomponentsModule
