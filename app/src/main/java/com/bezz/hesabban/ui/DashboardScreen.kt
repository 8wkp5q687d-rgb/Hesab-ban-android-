package com.bezz.hesabban.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bezz.hesabban.data.Account
import com.bezz.hesabban.data.Transaction
import com.bezz.hesabban.ui.icons.CategoryIconPaths
import com.bezz.hesabban.ui.icons.MiscIconPaths
import com.bezz.hesabban.ui.icons.SvgStrokeIcon
import com.bezz.hesabban.ui.theme.Tokens
import com.bezz.hesabban.viewmodel.TransactionViewModel

@Composable
fun DashboardScreen(viewModel: TransactionViewModel) {
    val accounts by viewModel.accounts.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val income = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    val expense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val totalBalance = accounts.sumOf { it.balance }

    LazyColumn(Modifier.fillMaxSize()) {
        item {
            Column(Modifier.fillMaxWidth()) {
                // Header: brand + month badge
                Row(
                    Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(14.dp).background(Tokens.accent))
                        Spacer(Modifier.width(9.dp))
                        Text("حساب‌بان", fontWeight = FontWeight.Black, fontSize = 16.5.sp, color = Tokens.ink)
                    }
                    Box(Modifier.border(1.5.dp, Tokens.neutral400).padding(horizontal = 9.dp, vertical = 3.dp)) {
                        Text("این ماه", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Tokens.neutral600)
                    }
                }

                // Total balance
                Column(Modifier.padding(start = 22.dp, end = 22.dp, top = 26.dp)) {
                    Text(
                        "موجودی کل", fontWeight = FontWeight.ExtraBold, fontSize = 11.5.sp,
                        color = Tokens.neutral600
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            toPersianDigits(formatAmountFa(totalBalance)),
                            fontWeight = FontWeight.Black, fontSize = 40.sp, color = Tokens.ink
                        )
                        Text(" تومان", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Tokens.neutral600)
                    }
                }

                // Bank cards row
                Row(
                    Modifier
                        .padding(top = 18.dp)
                        .horizontalScroll(rememberScrollState())
                        .padding(start = 22.dp, end = 22.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    accounts.forEach { acc -> BankCard(acc) }
                }

                // Income / expense strip
                Row(
                    Modifier
                        .padding(top = 18.dp)
                        .fillMaxWidth()
                        .background(Tokens.white)
                        .border(2.dp, Tokens.ink)
                ) {
                    Row(
                        Modifier.weight(1f)
                            .padding(vertical = 13.dp, horizontal = 22.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SvgStrokeIcon(MiscIconPaths.ARROW_UP, tint = Tokens.ink, size = 20.dp, strokeWidthPx = 2f)
                        Spacer(Modifier.width(11.dp))
                        Column {
                            Text("دریافتی", fontWeight = FontWeight.ExtraBold, fontSize = 10.5.sp, color = Tokens.neutral600)
                            Text(toPersianDigits(formatAmountFa(income)), fontWeight = FontWeight.Black, fontSize = 16.sp, color = Tokens.ink)
                        }
                    }
                    Row(
                        Modifier.weight(1f).padding(vertical = 13.dp, horizontal = 22.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SvgStrokeIcon(MiscIconPaths.ARROW_DOWN, tint = Tokens.accent700, size = 20.dp, strokeWidthPx = 2f)
                        Spacer(Modifier.width(11.dp))
                        Column {
                            Text("پرداختی", fontWeight = FontWeight.ExtraBold, fontSize = 10.5.sp, color = Tokens.neutral600)
                            Text(toPersianDigits(formatAmountFa(expense)), fontWeight = FontWeight.Black, fontSize = 16.sp, color = Tokens.accent700)
                        }
                    }
                }

                // Recent transactions header
                Row(
                    Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, top = 20.dp, bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text("تراکنش‌های اخیر", fontWeight = FontWeight.Black, fontSize = 15.sp, color = Tokens.ink)
                    Text("همه ←", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = Tokens.accent700)
                }
            }
        }

        items(transactions.take(4)) { tx ->
            Box(Modifier.padding(horizontal = 22.dp)) {
                DashboardTxRow(tx)
            }
        }

        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
private fun BankCard(account: Account) {
    Column(
        Modifier
            .width(216.dp)
            .height(128.dp)
            .background(Tokens.ink)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(account.bankName, color = Tokens.white, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
            Box(Modifier.width(28.dp).height(20.dp).background(Tokens.accent))
        }
        Column {
            Text(
                "•••• ${account.last4}",
                color = Tokens.white.copy(alpha = 0.65f),
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
            Row {
                Text(toPersianDigits(formatAmountFa(account.balance)), color = Tokens.white, fontWeight = FontWeight.Black, fontSize = 19.sp)
                Text(" تومان", color = Tokens.white.copy(alpha = 0.65f), fontWeight = FontWeight.Bold, fontSize = 10.5.sp)
            }
        }
    }
}

@Composable
fun DashboardTxRow(tx: Transaction) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val isIncome = tx.type == "INCOME"
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
