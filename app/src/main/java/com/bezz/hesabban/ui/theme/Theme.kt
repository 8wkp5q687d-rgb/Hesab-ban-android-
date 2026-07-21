package com.bezz.hesabban.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bezz.hesabban.R

/** Vazirmatn, bundled locally (offline app — no CDN font loading). */
val Vazirmatn = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_bold, FontWeight.Bold),
    Font(R.font.vazirmatn_extrabold, FontWeight.ExtraBold),
    Font(R.font.vazirmatn_black, FontWeight.Black)
)

/** Archivo-ish fallback for LTR numerals (masked card numbers, phone numbers). Falls back to system monospace. */
val NumeralFontFamily = FontFamily.Monospace

private val HesabBanColorScheme = lightColorScheme(
    primary = Tokens.accent,
    onPrimary = Tokens.white,
    background = Tokens.bg,
    onBackground = Tokens.ink,
    surface = Tokens.white,
    onSurface = Tokens.ink,
    secondary = Tokens.ink,
    onSecondary = Tokens.white,
    error = Tokens.accent700
)

// Zero corner radius everywhere — "Modernist" language, no rounding ever.
// Material3's Shapes requires CornerBasedShape specifically (RectangleShape
// doesn't qualify, even though it's visually identical) — RoundedCornerShape(0.dp)
// is the correct zero-radius CornerBasedShape.
private val Zero = RoundedCornerShape(0.dp)
private val ZeroShapes = androidx.compose.material3.Shapes(
    extraSmall = Zero,
    small = Zero,
    medium = Zero,
    large = Zero,
    extraLarge = Zero
)

private val HesabBanTypography = Typography(
    displayLarge = TextStyle(fontFamily = Vazirmatn, fontWeight = FontWeight.Black, fontSize = 54.sp, lineHeight = 60.sp, letterSpacing = (-1).sp),
    headlineLarge = TextStyle(fontFamily = Vazirmatn, fontWeight = FontWeight.Black, fontSize = 40.sp, lineHeight = 50.sp, letterSpacing = (-0.5).sp),
    headlineMedium = TextStyle(fontFamily = Vazirmatn, fontWeight = FontWeight.Black, fontSize = 30.sp, lineHeight = 40.sp),
    titleLarge = TextStyle(fontFamily = Vazirmatn, fontWeight = FontWeight.Black, fontSize = 21.sp),
    titleMedium = TextStyle(fontFamily = Vazirmatn, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp),
    titleSmall = TextStyle(fontFamily = Vazirmatn, fontWeight = FontWeight.ExtraBold, fontSize = 13.5.sp),
    bodyLarge = TextStyle(fontFamily = Vazirmatn, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 23.sp),
    bodyMedium = TextStyle(fontFamily = Vazirmatn, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp),
    bodySmall = TextStyle(fontFamily = Vazirmatn, fontWeight = FontWeight.Normal, fontSize = 11.5.sp, color = androidx.compose.ui.graphics.Color(0xFF7D7979)),
    labelLarge = TextStyle(fontFamily = Vazirmatn, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp),
    labelMedium = TextStyle(fontFamily = Vazirmatn, fontWeight = FontWeight.ExtraBold, fontSize = 11.5.sp),
    labelSmall = TextStyle(fontFamily = Vazirmatn, fontWeight = FontWeight.ExtraBold, fontSize = 10.5.sp)
)

@Composable
fun HesabBanTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = HesabBanColorScheme,
        typography = HesabBanTypography,
        shapes = ZeroShapes,
        content = content
    )
}
