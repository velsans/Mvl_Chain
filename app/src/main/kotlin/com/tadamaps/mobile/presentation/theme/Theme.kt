package com.tadamaps.mobile.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import com.tadamaps.mobile.R

@Composable
private fun mvlLightColorScheme() = lightColorScheme(
    primary = colorResource(R.color.mvl_primary),
    onPrimary = colorResource(R.color.mvl_on_primary),
    primaryContainer = colorResource(R.color.mvl_primary_container),
    onPrimaryContainer = colorResource(R.color.mvl_on_primary_container),
    secondary = colorResource(R.color.mvl_secondary),
    onSecondary = colorResource(R.color.mvl_on_secondary),
    background = colorResource(R.color.mvl_background),
    surface = colorResource(R.color.mvl_surface),
    onSurface = colorResource(R.color.mvl_on_surface),
    onSurfaceVariant = colorResource(R.color.mvl_on_surface_variant),
)

@Composable
private fun mvlDarkColorScheme() = darkColorScheme(
    primary = colorResource(R.color.mvl_primary),
    onPrimary = colorResource(R.color.mvl_on_primary),
    background = colorResource(R.color.mvl_dark_background),
    surface = colorResource(R.color.mvl_dark_surface),
)

@Composable
fun MvlTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Reference wireframes use a fixed yellow accent; disable dynamic color by default.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colors = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> mvlDarkColorScheme()
        else -> mvlLightColorScheme()
    }
    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        content = content,
    )
}
