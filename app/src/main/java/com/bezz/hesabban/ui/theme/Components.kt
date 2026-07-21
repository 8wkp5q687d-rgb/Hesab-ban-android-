package com.bezz.hesabban.ui.theme

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RectangleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** The 48x28 two-state toggle from onboarding step 2 / Settings sender rows. */
@Composable
fun HbToggle(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val knobOffset by animateDpAsState(targetValue = if (checked) 23.dp else 3.dp, animationSpec = tween(180), label = "toggleKnob")
    Box(
        modifier = modifier
            .width(48.dp)
            .height(28.dp)
            .background(if (checked) Tokens.accent else Tokens.white)
            .border(Tokens.borderThick, Tokens.ink)
            .clickable { onCheckedChange(!checked) }
    ) {
        Box(
            modifier = Modifier
                .padding(start = knobOffset, top = 3.dp)
                .size(18.dp)
                .background(if (checked) Tokens.white else Tokens.ink)
        )
    }
}

/** Two-option segmented control (زبان, پرداخت/دریافت). Generic over any label list. */
@Composable
fun HbSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .border(Tokens.borderThick, Tokens.ink)
            .background(Tokens.white)
    ) {
        options.forEachIndexed { i, label ->
            val selected = i == selectedIndex
            Box(
                modifier = Modifier
                    .then(if (i > 0) Modifier.startBorderLine(Tokens.borderThick, Tokens.ink) else Modifier)
                    .background(if (selected) Tokens.ink else Color.Transparent)
                    .clickable { onSelect(i) }
                    .padding(horizontal = 20.dp, vertical = 9.dp)
            ) {
                Text(
                    label,
                    color = if (selected) Tokens.white else Tokens.ink,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

/** Small helper: a single vertical line on the start edge (segmented-control divider). */
fun Modifier.startBorderLine(width: androidx.compose.ui.unit.Dp, color: Color): Modifier =
    this.then(androidx.compose.ui.draw.drawBehind {
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(0f, 0f),
            end = androidx.compose.ui.geometry.Offset(0f, size.height),
            strokeWidth = width.toPx()
        )
    })

/** Category chip — selected = ink fill/white text, unselected = outline. */
@Composable
fun HbChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .border(Tokens.borderThick, Tokens.ink)
            .background(if (selected) Tokens.ink else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 11.dp, vertical = 5.dp)
    ) {
        Text(label, color = if (selected) Tokens.white else Tokens.ink, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
    }
}
