package com.bezz.hesabban.ui

import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

private val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

/** ASCII digits -> Persian digits (fa-IR numeral rendering, per prototype's fmt()). */
fun toPersianDigits(s: String): String =
    s.map { c -> if (c in '0'..'9') persianDigits[c - '0'] else c }.joinToString("")

/**
 * Formats an absolute Toman amount with Persian grouping (٬) and Persian
 * digits, matching the prototype's `fmt(n) = Math.abs(n).toLocaleString('fa-IR')`.
 */
fun formatAmountFa(amount: Long): String {
    val nf = NumberFormat.getNumberInstance(Locale.US)
    val grouped = nf.format(kotlin.math.abs(amount)).replace(",", "٬")
    return toPersianDigits(grouped)
}

/** Full amount string with trailing sign, e.g. "۲۸۵٬۰۰۰+" or "۲۸۵٬۰۰۰−" (U+2212 minus, after the number). */
fun formatSignedAmountFa(amount: Long, isIncome: Boolean): String {
    val sign = if (isIncome) "+" else "−"
    return "${formatAmountFa(amount)}$sign"
}

fun formatPercentFa(value: Int): String = "٪${toPersianDigits(value.toString())}"

/**
 * NOTE [محتمل]: this groups by Gregorian calendar day, not Jalali. The design
 * spec calls for Jalali day labels ("امروز", "دیروز", "۱۸ تیر") — a proper
 * Jalali calendar library (e.g. com.github.samanzamani:PersianDate, per
 * ANDROID_GUIDE.md) should replace this for exact parity. Wiring that in is
 * a small, isolated follow-up once the library dependency is added.
 */
fun dayLabel(millis: Long): String {
    val target = Calendar.getInstance().apply { timeInMillis = millis }
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

    fun sameDay(a: Calendar, b: Calendar) =
        a.get(Calendar.YEAR) == b.get(Calendar.YEAR) && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)

    return when {
        sameDay(target, today) -> "امروز"
        sameDay(target, yesterday) -> "دیروز"
        else -> {
            val day = toPersianDigits(target.get(Calendar.DAY_OF_MONTH).toString())
            val month = target.get(Calendar.MONTH)
            val months = listOf(
                "ژانویه", "فوریه", "مارس", "آوریل", "مه", "ژوئن",
                "ژوئیه", "اوت", "سپتامبر", "اکتبر", "نوامبر", "دسامبر"
            )
            "$day ${months[month]}"
        }
    }
}

fun dayKey(millis: Long): Int {
    val c = Calendar.getInstance().apply { timeInMillis = millis }
    return c.get(Calendar.YEAR) * 1000 + c.get(Calendar.DAY_OF_YEAR)
}
