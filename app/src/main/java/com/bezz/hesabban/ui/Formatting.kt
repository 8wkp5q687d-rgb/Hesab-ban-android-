package com.bezz.hesabban.ui

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatToman(amount: Long): String {
    val nf = NumberFormat.getNumberInstance(Locale.US)
    return "${nf.format(amount)} تومان"
}

fun formatDate(millis: Long): String {
    val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US)
    return sdf.format(Date(millis))
}
