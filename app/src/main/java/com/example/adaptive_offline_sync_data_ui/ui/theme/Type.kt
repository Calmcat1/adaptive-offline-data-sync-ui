package com.example.adaptive_offline_sync_data_ui.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.example.adaptive_offline_sync_data_ui.R

val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val InterFont = GoogleFont("Inter")

val InterFontFamily = FontFamily(
    Font(googleFont = InterFont, fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = InterFont, fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = InterFont, fontProvider = fontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = InterFont, fontProvider = fontProvider, weight = FontWeight.Bold)
)

private val defaultTypography = Typography()

val Typography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = InterFontFamily, letterSpacing = (-0.5).sp),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = InterFontFamily, letterSpacing = (-0.25).sp),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = InterFontFamily, letterSpacing = 0.sp),
    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = InterFontFamily, letterSpacing = (-0.25).sp),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = InterFontFamily, letterSpacing = (-0.2).sp),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = InterFontFamily, letterSpacing = 0.sp),
    titleLarge = defaultTypography.titleLarge.copy(fontFamily = InterFontFamily, letterSpacing = 0.sp),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = InterFontFamily, letterSpacing = 0.sp),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = InterFontFamily, letterSpacing = 0.sp),
    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = InterFontFamily, letterSpacing = 0.sp),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = InterFontFamily, letterSpacing = 0.sp),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = InterFontFamily, letterSpacing = 0.sp),
    labelLarge = defaultTypography.labelLarge.copy(fontFamily = InterFontFamily, letterSpacing = 0.sp),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = InterFontFamily, letterSpacing = 0.sp),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = InterFontFamily, letterSpacing = 0.sp)
)