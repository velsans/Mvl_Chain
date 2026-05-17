package com.tadamaps.mobile

import androidx.compose.runtime.Composable
import com.tadamaps.mobile.presentation.navigation.AppNavHost
import com.tadamaps.mobile.presentation.theme.MvlTheme

@Composable
fun MVLApp() {
    MvlTheme {
        AppNavHost()
    }
}
