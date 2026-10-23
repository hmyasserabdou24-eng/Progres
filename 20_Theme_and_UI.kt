package dz.mesrs.webetu.automeals.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val BrandPrimaryDark = Color(0xFF123E73)
val BrandPrimaryLight = Color(0xFFEAF2FB)
val BrandBackground = Color(0xFFF7F9FC)
val BrandSurface = Color(0xFFFFFFFF)
val BrandTextDark = Color(0xFF172B4D)
val BrandTextSecondary = Color(0xFF5E6C84)
val StatusSuccess = Color(0xFF1B8738)
val StatusFailure = Color(0xFFD32F2F)

@Composable
fun WebetuAutoMealsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = BrandPrimaryDark,
            background = BrandBackground,
            surface = BrandSurface,
            onPrimary = Color.White,
            onBackground = BrandTextDark,
            onSurface = BrandTextDark
        ),
        content = content
    )
}
