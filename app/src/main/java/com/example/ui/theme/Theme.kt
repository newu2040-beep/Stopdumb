package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun StopDumbTheme(
    selectedTheme: String = "Bento Grid",
    isDark: Boolean = isSystemInDarkTheme(),
    isAmoled: Boolean = false,
    content: @Composable () -> Unit
) {
    // Choose dynamic color mappings based on theme name
    val (primaryColor, primaryDarkColor) = when (selectedTheme) {
        "Lavender" -> Pair(LavenderPrimary, LavenderPrimaryDark)
        "Peach" -> Pair(PeachPrimary, PeachPrimaryDark)
        "Ocean Blue" -> Pair(OceanBluePrimary, OceanBluePrimaryDark)
        "Rose Pink" -> Pair(RosePinkPrimary, RosePinkPrimaryDark)
        "Sunset Orange" -> Pair(SunsetOrangePrimary, SunsetOrangePrimaryDark)
        "Arctic White" -> Pair(ArcticPrimary, ArcticPrimaryDark)
        "Bento Grid" -> Pair(BentoPrimary, BentoPrimary) // High contrast blue
        "Cyberpunk" -> Pair(CyberPrimary, CyberPrimary)
        "Nordic Ice" -> Pair(NordicPrimary, NordicPrimary)
        "Solarized Light" -> Pair(SolarPrimary, SolarPrimary)
        else -> Pair(BentoPrimary, BentoPrimary) // default to Bento
    }

    val finalPrimary = if (isDark && selectedTheme !in listOf("Bento Grid", "Cyberpunk", "Nordic Ice", "Solarized Light")) primaryDarkColor else primaryColor
    
    // Choose backgrounds depending on dark mode + Amoled black settings
    val background = when {
        selectedTheme == "Bento Grid" -> BentoBg
        selectedTheme == "Cyberpunk" -> CyberBg
        selectedTheme == "Nordic Ice" -> NordicBg
        selectedTheme == "Solarized Light" -> SolarBg
        isDark && isAmoled -> AmoledBgSystem
        isDark -> DarkBgSystem
        else -> LightBgSystem
    }

    val surface = when {
        selectedTheme == "Bento Grid" -> Color.White
        selectedTheme == "Cyberpunk" -> Color(0xFF1B0330)
        selectedTheme == "Nordic Ice" -> Color(0xFF3B4252)
        selectedTheme == "Solarized Light" -> Color(0xFFEEE8D5)
        isDark && isAmoled -> AmoledSurfaceCard
        isDark -> DarkSurfaceCard
        else -> LightSurfaceCard
    }

    val onBackground = when (selectedTheme) {
        "Bento Grid" -> BentoText
        "Cyberpunk" -> Color.White
        "Nordic Ice" -> Color(0xFFECEFF4)
        "Solarized Light" -> Color(0xFF586E75)
        else -> if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A)
    }
    
    val onSurface = when (selectedTheme) {
        "Bento Grid" -> BentoText
        "Cyberpunk" -> Color.White
        "Nordic Ice" -> Color(0xFFECEFF4)
        "Solarized Light" -> Color(0xFF586E75)
        else -> if (isDark) Color(0xFFF8FAFC) else Color(0xFF1E293B)
    }

    val colorScheme = if (isDark && selectedTheme !in listOf("Bento Grid", "Cyberpunk", "Nordic Ice", "Solarized Light")) {
        darkColorScheme(
            primary = finalPrimary,
            secondary = if (isAmoled) Color(0xFF1E293B) else Color(0xFF334155),
            tertiary = finalPrimary.copy(alpha = 0.7f),
            background = background,
            surface = surface,
            onPrimary = Color(0xFF0F172A),
            onSecondary = Color(0xFFF8FAFC),
            onTertiary = Color(0xFFFFFFFF),
            onBackground = onBackground,
            onSurface = onSurface,
            surfaceVariant = if (isAmoled) Color(0xFF0F172A) else Color(0xFF1E293B),
            onSurfaceVariant = Color(0xFFCBD5E1)
        )
    } else {
        lightColorScheme(
            primary = finalPrimary,
            secondary = Color(0xFFE2E8F0),
            tertiary = finalPrimary.copy(alpha = 0.7f),
            background = background,
            surface = surface,
            onPrimary = Color.White,
            onSecondary = Color(0xFF334155),
            onTertiary = Color(0xFF0F172A),
            onBackground = onBackground,
            onSurface = onSurface,
            surfaceVariant = Color(0xFFEDF2F7),
            onSurfaceVariant = Color(0xFF475569)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
