package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = VpnCyanPrimary,
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = Color(0xFF97F0FF),
    secondary = VpnBlueSecondary,
    onSecondary = Color(0xFF003258),
    tertiary = VpnConnectedGreen,
    onTertiary = Color(0xFF00391E),
    background = VpnNavyBackground,
    onBackground = VpnTextPrimary,
    surface = VpnCardBackground,
    onSurface = VpnTextPrimary,
    surfaceVariant = VpnCardElevated,
    onSurfaceVariant = VpnTextSecondary,
    outline = VpnCardBorder
)

private val LightColorScheme = DarkColorScheme // VPN is optimized as a dedicated dark tech interface

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
