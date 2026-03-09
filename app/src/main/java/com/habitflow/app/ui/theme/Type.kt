package com.habitflow.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.habitflow.app.R

// --- Google Fonts provider ---
val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

// --- Inter (primary) ---
val InterFamily = FontFamily(
    Font(GoogleFont("Inter"), provider, weight = FontWeight.Normal),
    Font(GoogleFont("Inter"), provider, weight = FontWeight.Medium),
    Font(GoogleFont("Inter"), provider, weight = FontWeight.SemiBold),
    Font(GoogleFont("Inter"), provider, weight = FontWeight.Bold),
    Font(GoogleFont("Inter"), provider, weight = FontWeight.ExtraBold),
)

// --- Playfair Display (quote card only) ---
val PlayfairFamily = FontFamily(
    Font(GoogleFont("Playfair Display"), provider, weight = FontWeight.Normal),
    Font(GoogleFont("Playfair Display"), provider, weight = FontWeight.Normal, style = FontStyle.Italic),
    Font(GoogleFont("Playfair Display"), provider, weight = FontWeight.Bold),
)

val AppTypography = Typography(
    // Large screen heading — e.g. "Good Morning, Zenith"
    displayLarge = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        color = TextPrimary
    ),
    // Section title — e.g. "Today", "Your Progress"
    titleLarge = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        color = TextPrimary
    ),
    // Card headers / habit names
    titleMedium = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        color = TextPrimary
    ),
    // Body text
    bodyLarge = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        color = TextSecondary
    ),
    bodyMedium = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = TextSecondary
    ),
    // Labels, captions, chips
    labelLarge = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        color = TextTertiary
    ),
    labelSmall = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        color = TextTertiary
    ),
)
