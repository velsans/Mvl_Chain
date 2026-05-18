package com.tadamaps.mobile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.ViewModelProvider
import com.tadamaps.mobile.presentation.navigation.AppNavHost
import com.tadamaps.mobile.presentation.theme.MvlTheme

@Composable
fun MVLApp(viewModelFactory: ViewModelProvider.Factory) {
    CompositionLocalProvider(LocalViewModelFactory provides viewModelFactory) {
        MvlTheme {
            AppNavHost()
        }
    }
}
