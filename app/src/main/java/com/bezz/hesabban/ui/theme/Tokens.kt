package com.bezz.hesabban.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Design tokens ported 1:1 from design_handoff_hesabban_android/styles.css
 * and Finance App v2.dc.html. These are the ground-truth hex values from
 * the CSS custom properties — do not "round" them to Material defaults.
 */
object Tokens {
    val bg = Color(0xFFF3F2F2)
    val surface = Color(0xFFEAE9E9)
    val ink = Color(0xFF201E1D)      // --color-text
    val white = Color(0xFFFFFFFF)

    val accent = Color(0xFFEC3013)   // --color-accent
    val accent2 = Color(0xFFE15B47)

    // Accent ramp
    val accent100 = Color(0xFFFFF2EF)
    val accent200 = Color(0xFFFFE0D9)
    val accent300 = Color(0xFFFFC4B8)
    val accent400 = Color(0xFFFF9783)
    val accent500 = Color(0xFFFF563C)
    val accent600 = Color(0xFFDD2B0F)
    val accent700 = Color(0xFFAE1800)
    val accent800 = Color(0xFF7C1405)
    val accent900 = Color(0xFF4D170E)

    // Neutral ramp
    val neutral100 = Color(0xFFF8F4F4)
    val neutral200 = Color(0xFFEAE7E7)
    val neutral300 = Color(0xFFD7D3D3)
    val neutral400 = Color(0xFFBAB6B6)
    val neutral500 = Color(0xFF9B9797)
    val neutral600 = Color(0xFF7D7979)
    val neutral700 = Color(0xFF605D5D)
    val neutral800 = Color(0xFF444141)
    val neutral900 = Color(0xFF2D2B2B)

    // Spacing rhythm
    val space1 = 4.dp
    val space2 = 8.dp
    val space3 = 12.dp
    val space4 = 16.dp
    val space6 = 24.dp
    val space8 = 32.dp

    val screenPadding = 22.dp

    // Border widths — zero corner radius everywhere, borders do the organizing.
    val borderThin = 1.dp    // row dividers, neutral-300
    val borderThick = 2.dp   // structural: ink borders/rules
    val borderExtra = 3.dp   // bottom-sheet top border, SMS quote start-border
}
