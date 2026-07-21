package com.bezz.hesabban.ui.icons

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Renders one of the app's stroke-based line icons (ported verbatim from the
 * inline SVG paths in the "Finance App v2.dc.html" prototype — 24x24 viewBox,
 * stroke:currentColor, round caps/joins, no fill).
 */
@Composable
fun SvgStrokeIcon(
    pathData: String,
    tint: Color,
    size: Dp = 20.dp,
    strokeWidthPx: Float = 1.8f,
    modifier: Modifier = Modifier
) {
    val path = remember(pathData) { PathParser().parsePathString(pathData).toPath() }
    Canvas(modifier = modifier.then(Modifier.size(size))) {
        val s = this.size.width / 24f
        scale(s, s, pivot = Offset.Zero) {
            drawPath(
                path = path,
                color = tint,
                style = Stroke(
                    width = strokeWidthPx / s,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}

/** Category icon path data, keyed by the Persian category name (canonical key per spec). */
object CategoryIconPaths {
    const val KHORAK = "M5 8h11v7a4 4 0 0 1-4 4H9a4 4 0 0 1-4-4V8ZM16 9h1.5a2.5 2.5 0 0 1 0 5H16"
    const val HAML_O_NAGHL = "M5 17l1.2-6.4A2 2 0 0 1 8.2 9h7.6a2 2 0 0 1 2 1.6L19 17v3h-2.5v-1.5h-9V20H5v-3ZM8 14h.01M16 14h.01"
    const val GHABZ_HA = "M7 3h10v18l-1.7-1.3L13.6 21l-1.6-1.3L10.4 21l-1.7-1.3L7 21V3ZM10 8h5M10 12h4"
    const val KHARID = "M6.5 8h11L16.4 21H7.6L6.5 8ZM9.5 8a2.5 2.5 0 0 1 5 0"
    const val DARAMAD = "M3.5 7.5h17v9h-17v-9ZM14 12a2 2 0 1 1-4 0 2 2 0 0 1 4 0ZM6.5 10v.01M17.5 14v.01"
    const val SAYER = "M6 12h.01M12 12h.01M18 12h.01"

    fun forCategory(category: String): String = when (category) {
        "خوراک" -> KHORAK
        "حمل‌ونقل" -> HAML_O_NAGHL
        "قبض‌ها" -> GHABZ_HA
        "خرید" -> KHARID
        "درآمد" -> DARAMAD
        else -> SAYER
    }
}

object NavIconPaths {
    const val HOME = "M4.5 11 12 4.5 19.5 11v8.5h-5.5v-5.5h-4v5.5H4.5V11Z"
    const val TX = "M8.5 6.5h11M8.5 12h11M8.5 17.5h11M4.5 6.5h.01M4.5 12h.01M4.5 17.5h.01"
    const val REPORT = "M5.5 19.5v-8M12 19.5v-15M18.5 19.5v-5"
    const val SETTINGS = "M4.5 7.5h9M17.5 7.5h2M13.5 4.8v5.4M4.5 16.5h3M11.5 16.5h8M7.5 13.8v5.4"
}

object MiscIconPaths {
    const val SMS_BUBBLE = "M4 5h16v12H8l-4 3.5V5Z"
    const val ARROW_UP = "M12 19V5M6 11l6-6 6 6"
    const val ARROW_DOWN = "M12 5v14M6 13l6 6 6-6"
    const val PLUS = "M12 4v16M4 12h16"
}
