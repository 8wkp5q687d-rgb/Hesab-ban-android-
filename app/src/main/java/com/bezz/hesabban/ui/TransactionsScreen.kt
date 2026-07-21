package com.bezz.hesabban.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bezz.hesabban.data.Transaction
import com.bezz.hesabban.ui.icons.CategoryIconPaths
import com.bezz.hesabban.ui.icons.MiscIconPaths
import com.bezz.hesabban.ui.icons.SvgStrokeIcon
import com.bezz.hesabban.viewmodel.CATEGORIES
import com.bezz.hesabban.ui.theme.HbChip
import com.bezz.hesabban.ui.theme.HbSegmentedControl
import com.bezz.hesabban.ui.theme.Tokens
import com.bezz.hesabban.viewmodel.TransactionViewModel

@Composable
fun TransactionsScreen(
    viewModel: TransactionViewModel,
    expandedId: Long?,
    onToggleExpand: (Long) -> Unit,
    onAddManual: () -> Unit
) {
    val transactions by viewModel.transactions.collectAsState()
    val grouped = transactions.groupBy { dayKey(it.date) }.toSortedMap(compareByDescending { it })

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, top = 16.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("تراکنش‌ها", fontWeight = FontWeight.Black, fontSize = 21.sp, color = Tokens.ink)
            Row(
                Modifier
                    .background(Tokens.ink)
                    .clickable { onAddManual() }
                    .padding(horizontal = 15.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SvgStrokeIcon(MiscIconPaths.PLUS, tint = Tokens.white, size = 14.dp, strokeWidthPx = 2.6f)
                Spacer(Modifier.width(7.dp))
                Text("ثبت دستی", color = Tokens.white, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
            }
        }

        if (transactions.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(22.dp), contentAlignment = Alignment.Center) {
                Text(
                    "هنوز تراکنشی ثبت نشده. اول از تب «تنظیمات» شماره بانک رو فعال کن.",
                    color = Tokens.neutral600, fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(horizontal = 22.dp)) {
                grouped.forEach { (_, dayTxs) ->
                    item {
                        Text(
                            dayLabel(dayTxs.first().date),
                            fontWeight = FontWeight.Black, fontSize = 11.5.sp, color = Tokens.neutral600,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp, bottom = 6.dp)
                                .edgeBorder(bottom = 2.dp, color = Tokens.ink)
                        )
                    }
                    items(dayTxs) { tx ->
                        Column {
                            TransactionRow(
                                tx = tx,
                                onTap = { onToggleExpand(tx.id) }
                            )
                            if (expandedId == tx.id) {
                                ExpandPanel(tx = tx, viewModel = viewModel)
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun TransactionRow(tx: Transaction, onTap: () -> Unit) {
    val isIncome = tx.type == "INCOME"
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onTap() }
            .edgeBorder(bottom = 1.dp, color = Tokens.neutral300)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(38.dp).border(2.dp, Tokens.ink).background(if (isIncome) Tokens.ink else Tokens.white),
            contentAlignment = Alignment.Center
        ) {
            SvgStrokeIcon(
                CategoryIconPaths.forCategory(tx.category),
                tint = if (isIncome) Tokens.white else Tokens.ink,
                size = 19.dp
            )
        }
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(tx.title, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Tokens.ink)
            val src = if (tx.source == "SMS") "پیامک" else "دستی"
            val meta = listOfNotNull(tx.category, tx.bankName, src).joinToString(" · ")
            Text(meta, fontSize = 11.sp, color = Tokens.neutral600)
        }
        Text(
            formatSignedAmountFa(tx.amount, isIncome),
            fontWeight = FontWeight.Black, fontSize = 14.5.sp,
            color = if (isIncome) Tokens.ink else Tokens.accent700
        )
    }
}

@Composable
private fun ExpandPanel(tx: Transaction, viewModel: TransactionViewModel) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Tokens.white)
            .border(2.dp, Tokens.ink)
            .padding(14.dp)
    ) {
        if (tx.source == "SMS" && tx.rawSms != null) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Tokens.neutral100)
                    .then(Modifier.edgeBorder(start = 3.dp, color = Tokens.accent))
                    .padding(horizontal = 12.dp, vertical = 9.dp)
            ) {
                Text(tx.rawSms, fontSize = 11.5.sp, color = Tokens.neutral700, lineHeight = 22.sp)
            }
            Spacer(Modifier.height(11.dp))
        }

        Text("دسته‌بندی:", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = Tokens.neutral600)
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth()) {
            // simple wrap via FlowRow-less Row+wrap workaround: chunk into rows of 3
            Column {
                CATEGORIES.chunked(3).forEach { rowCats ->
                    Row {
                        rowCats.forEach { cat ->
                            HbChip(
                                label = cat,
                                selected = cat == tx.category,
                                onClick = { viewModel.update(tx.copy(category = cat)) },
                                modifier = Modifier.padding(end = 6.dp, bottom = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(5.dp))
        Box(
            Modifier
                .border(2.dp, Tokens.accent)
                .clickable { viewModel.delete(tx) }
                .padding(horizontal = 13.dp, vertical = 6.dp)
        ) {
            Text("حذف تراکنش", color = Tokens.accent700, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
        }
    }
}

@Composable
fun AddTransactionSheet(
    onDismiss: () -> Unit,
    onSave: (amount: Long, type: String, category: String, title: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var isIncome by remember { mutableStateOf(false) }
    var category by remember { mutableStateOf(CATEGORIES.last()) }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0x8C201E1D))
            .clickable(indication = null, interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }) { onDismiss() }
    ) {
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Tokens.bg)
                .then(Modifier.edgeBorder(top = 3.dp, color = Tokens.ink))
                .padding(start = 22.dp, end = 22.dp, top = 20.dp, bottom = 30.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("ثبت تراکنش دستی", fontWeight = FontWeight.Black, fontSize = 17.sp, color = Tokens.ink)
                Text("×", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Tokens.ink,
                    modifier = Modifier.clickable { onDismiss() })
            }
            Spacer(Modifier.height(13.dp))

            HbTextField(title, { title = it }, "عنوان (مثلاً تاکسی)")
            Spacer(Modifier.height(13.dp))
            HbTextField(amountText, { amountText = it.filter { c -> c.isDigit() } }, "مبلغ به تومان")
            Spacer(Modifier.height(13.dp))

            HbSegmentedControl(
                options = listOf("پرداخت", "دریافت"),
                selectedIndex = if (isIncome) 1 else 0,
                onSelect = { i -> isIncome = i == 1; category = if (isIncome) "درآمد" else CATEGORIES.last() }
            )
            Spacer(Modifier.height(13.dp))

            Column {
                CATEGORIES.chunked(3).forEach { rowCats ->
                    Row {
                        rowCats.forEach { cat ->
                            HbChip(
                                label = cat, selected = cat == category, onClick = { category = cat },
                                modifier = Modifier.padding(end = 6.dp, bottom = 6.dp)
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(13.dp))

            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Tokens.accent)
                    .clickable {
                        val amount = amountText.toLongOrNull() ?: 0L
                        if (title.isNotBlank() && amount > 0) {
                            onSave(amount, if (isIncome) "INCOME" else "EXPENSE", category, title.trim())
                        }
                    }
                    .padding(15.dp)
            ) {
                Text("ذخیره", color = Tokens.white, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun HbTextField(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = Tokens.ink),
        modifier = Modifier
            .fillMaxWidth()
            .background(Tokens.white)
            .border(2.dp, Tokens.ink)
            .padding(horizontal = 13.dp, vertical = 12.dp),
        decorationBox = { inner ->
            if (value.isEmpty()) Text(placeholder, color = Tokens.neutral500, fontSize = 14.sp)
            inner()
        }
    )
}

/** One-sided border helper for bottom/start-only rules used throughout this screen. */
private fun Modifier.edgeBorder(bottom: androidx.compose.ui.unit.Dp = 0.dp, top: androidx.compose.ui.unit.Dp = 0.dp, start: androidx.compose.ui.unit.Dp = 0.dp, color: Color): Modifier =
    this.then(Modifier.drawBehind {
        if (bottom > 0.dp) drawLine(color, androidx.compose.ui.geometry.Offset(0f, size.height), androidx.compose.ui.geometry.Offset(size.width, size.height), bottom.toPx())
        if (top > 0.dp) drawLine(color, androidx.compose.ui.geometry.Offset(0f, 0f), androidx.compose.ui.geometry.Offset(size.width, 0f), top.toPx())
        if (start > 0.dp) drawLine(color, androidx.compose.ui.geometry.Offset(0f, 0f), androidx.compose.ui.geometry.Offset(0f, size.height), start.toPx())
    })
