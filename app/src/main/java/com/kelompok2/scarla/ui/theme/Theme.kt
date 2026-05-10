package com.kelompok2.scarla.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Primary1000, // Kuning lebih pekat untuk dark mode
    secondary = Secondary500,
    tertiary = Tertiary400,
    background = Neutral900,
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Neutral50,
    onSurface = Neutral50
)

private val LightColorScheme = lightColorScheme(
    primary = Primary500,
    secondary = Secondary500,
    tertiary = Tertiary400,
    background = Color.White,
    surface = Neutral50,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = Neutral900,
    onSurface = Neutral900
)

@Composable
fun ScarlaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Set ke false jika tidak ingin warna aplikasi berubah-ubah mengikuti wallpaper HP (Material You)
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}