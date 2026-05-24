package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldPrimaryDark,
    background = OledBackground,
    surface = OledSurface,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimaryLight,
    background = LightBackground,
    surface = LightSurface,
    onBackground = Color.Black,
    onSurface = Color.Black
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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

// Global variable or flow could change the accent colors
fun getAccentScheme(paletteName: String, isDark: Boolean): ColorScheme {
    val primaryColor = when(paletteName) {
        "sapphire" -> if(isDark) SapphirePrimaryDark else SapphirePrimaryLight
        "amethyst" -> if(isDark) AmethystPrimaryDark else AmethystPrimaryLight
        "rose" -> if(isDark) RosePrimaryDark else RosePrimaryLight
        "amber" -> if(isDark) AmberPrimaryDark else AmberPrimaryLight
        else -> if(isDark) EmeraldPrimaryDark else EmeraldPrimaryLight // Emerald default
    }
    
    return if (isDark) {
        darkColorScheme(
            primary = primaryColor,
            background = OledBackground,
            surface = OledSurface
        )
    } else {
        lightColorScheme(
            primary = primaryColor,
            background = LightBackground,
            surface = LightSurface
        )
    }
}
