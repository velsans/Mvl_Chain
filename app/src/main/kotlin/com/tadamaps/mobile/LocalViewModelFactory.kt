package com.tadamaps.mobile

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModelProvider

val LocalViewModelFactory = staticCompositionLocalOf<ViewModelProvider.Factory> {
    error("ViewModelProvider.Factory not provided — wrap UI with MVLApp(factory)")
}
