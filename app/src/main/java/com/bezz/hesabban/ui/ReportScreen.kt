package com.bezz.hesabban.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bezz.hesabban.data.CategorySummary
import com.bezz.hesabban.data.Transaction
import com.bezz.hesabban.ui.icons.CategoryIconPaths
import com.bezz.hesabban.ui.icons.SvgStrokeIcon
import com.bezz.hesabban.ui.theme.Tokens
import com.bezz.hesabban.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportScreen(viewModel: TransactionViewModel) {
    val monthly by viewModel.monthlySummary.collectAsState()
    val previous by viewModel.previousMonthSummary.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    val income = monthly.filter { it.type == "INCOME" }.sumOf { it.total }
    val expense = monthly.filter { it.type == "EXPENSE" }.sumOf { it.total }
    val net = income - expense
    val prevExpense = previous.filter { it.type == "EXPENSE" }.sumOf { it.total }

    val diffPct = if (prevExpense > 0) (((expense - prevExpense).toDouble() / prevExpense) * 100).toInt() else 0
    val expenseByCategory = monthly.filter { it.type == "EXPENSE" }.sortedByDescending { it.total }
    val maxCategoryTotal = expenseByCategory.maxOfOrNull { it.total } ?: 1L

    val monthLabel = toPersianDigits(SimpleDateFormat("MMMM yyyy", Locale("fa")).format(Date()))
        .ifBlank { SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date()) }

    val topExpenses = transactions.filter { it.type == "EXPENSE" }.sortedByDescending { it.amount }.take(3)

    LazyColumn(Modifier.fillMaxSize()) {
        item {
            Column(Modifier.fillMaxWidth().background(Tokens.accent).padding(22.dp)) {
                Text("گزارش ماهانه", color = Tokens.white.copy(alpha = 0.85f), fontWeight = FontWeight.ExtraBold, fontSize = 11.5.sp)
                Text(monthLabel, color = Tokens.white, fontWeight = FontWeight.Black, fontSize = 34.sp)
            }

            Row(
                Modifier.fillMaxWidth().background(Tokens.white).edgeBorder(bottom = 2.dp, color = Tokens.ink)
            ) {
                StatCell("دریافتی", formatSignedAmountFa(income, true), Tokens.ink, Modifier.weight(1f).edgeBorder(end = 2.dp, color = Tokens.ink))
                StatCell("پرداختی", formatSignedAmountFa(expense, false), Tokens.accent700, Modifier.weight(1f).edgeBorder(end = 2.dp, color = Tokens.ink))
                StatCell("خالص", "${if (net >= 0) "+" else "−"}${formatAmountFa(net)}", Tokens.ink, Modifier.weight(1f))
            }

            Row(
                Modifier.fillMaxWidth().edgeBorder(bottom = 2.dp, color = Tokens.ink).padding(vertical = 13.dp, horizontal = 22.dp)
            ) {
                Text(
                    "هزینه‌ها نسبت به ماه قبل ",
                    fontSize = 13.sp, color = Tokens.ink, lineHeight = 25.sp
                )
                Text(
                    "${if (diffPct >= 0) "" else ""}${formatPercentFa(kotlin.math.abs(diffPct))}",
                    fontWeight = FontWeight.Black, fontSize = 13.sp, color = Tokens.accent700
                )
                Text(
                    if (diffPct >= 0) " بیشتر شده است." else " کمتر شده است.",
                    fontSize = 13.sp, color = Tokens.ink, lineHeight = 25.sp
                )
            }

            Column(Modifier.padding(22.dp)) {
                Text("به تفکیک دسته", fontWeight = FontWeight.Black, fontSize = 15.sp, color = Tokens.ink)
                Spacer(Modifier.height(14.dp))
                if (expenseByCategory.isEmpty()) {
                    Text("داده‌ای برای این ماه نیست.", color = Tokens.neutral600, fontSize = 12.5.sp)
                } else {
                    expenseByCategory.forEach { s -> CategoryBarRow(s, maxCategoryTotal) }
                }
            }

            Column(
                Modifier
                    .padding(start = 22.dp, end = 22.dp)
                    .edgeBorder(top = 2.dp, color = Tokens.ink)
                    .padding(top = 15.dp)
            ) {
                Text("بزرگ‌ترین هزینه‌ها", fontWeight = FontWeight.Black, fontSize = 15.sp, color = Tokens.ink)
                if (topExpenses.isEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text("هنوز هزینه‌ای ثبت نشده.", color = Tokens.neutral600, fontSize = 12.5.sp)
                } else {
                    topExpenses.forEach { tx -> TopExpenseRow(tx) }
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun StatCell(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color, modifier: Modifier) {
    Column(modifier.padding(vertical = 14.dp, horizontal = 18.dp)) {
        Text(label, fontWeight = FontWeight.ExtraBold, fontSize = 10.5.sp, color = Tokens.neutral600)
        Text(value, fontWeight = FontWeight.Black, fontSize = 16.5.sp, color = valueColor)
    }
}

@Composable
private fun CategoryBarRow(summary: CategorySummary, maxTotal: Long) {
    val pct = if (maxTotal > 0) ((summary.total.toDouble() / maxTotal) * 100).toInt() else 0
    Column(Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(summary.category, fontWeight = FontWeight.Black, fontSize = 12.5.sp, color = Tokens.ink)
            Text(
                "${formatAmountFa(summary.total)} · ${formatPercentFa(pct)}",
                fontSize = 12.5.sp, color = Tokens.neutral700
            )
        }
        Spacer(Modifier.height(5.dp))
        Box(Modifier.fillMaxWidth().height(13.dp).background(Tokens.neutral200)) {
            Box(
                Modifier
                    .fillMaxWidth(fraction = (pct.coerceIn(0, 100)) / 100f)
                    .height(13.dp)
                    .background(Tokens.accent)
            )
        }
    }
}

@Composable
private fun TopExpenseRow(tx: Transaction) {
    Row(
        Modifier.fillMaxWidth().edgeBorder(bottom = 1.dp, color = Tokens.neutral300).padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(32.dp).border(2.dp, Tokens.ink), contentAlignment = Alignment.Center) {
            SvgStrokeIcon(CategoryIconPaths.forCategory(tx.category), tint = Tokens.ink, size = 16.dp)
        }
        Spacer(Modifier.width(12.dp))
        Text(tx.title, modifier = Modifier.weight(1f), fontWeight = FontWeight.ExtraBold, fontSize = 13.5.sp, color = Tokens.ink)
        Text(formatAmountFa(tx.amount), fontWeight = FontWeight.Black, fontSize = 13.5.sp, color = Tokens.accent700)
    }
}

/** One-sided border helper (bottom/top/end) shared across report rows. */
private fun Modifier.edgeBorder(bottom: androidx.compose.ui.unit.Dp = 0.dp, top: androidx.compose.ui.unit.Dp = 0.dp, end: androidx.compose.ui.unit.Dp = 0.dp, color: androidx.compose.ui.graphics.Color): Modifier =
    this.then(androidx.compose.ui.draw.drawBehind {
        if (bottom > 0.dp) drawLine(color, androidx.compose.ui.geometry.Offset(0f, size.height), androidx.compose.ui.geometry.Offset(size.width, size.height), bottom.toPx())
        if (top > 0.dp) drawLine(color, androidx.compose.ui.geometry.Offset(0f, 0f), androidx.compose.ui.geometry.Offset(size.width, 0f), top.toPx())
        if (end > 0.dp) drawLine(color, androidx.compose.ui.geometry.Offset(size.width, 0f), androidx.compose.ui.geometry.Offset(size.width, size.height), end.toPx())
    })
