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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bezz.hesabban.data.Account
import com.bezz.hesabban.data.Sender
import com.bezz.hesabban.ui.theme.HbSegmentedControl
import com.bezz.hesabban.ui.theme.HbToggle
import com.bezz.hesabban.ui.theme.Tokens
import com.bezz.hesabban.viewmodel.TransactionViewModel

@Composable
fun SettingsScreen(viewModel: TransactionViewModel, onRequestSmsImport: () -> Unit) {
    val senders by viewModel.senders.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val importStatus by viewModel.importStatus.collectAsState()

    var showAddSender by remember { mutableStateOf(false) }
    var showAddAccount by remember { mutableStateOf(false) }
    var langIndex by remember { mutableStateOf(0) } // 0 = فارسی — English switch not wired yet [محتمل]

    LazyColumn(Modifier.fillMaxSize().padding(start = 22.dp, end = 22.dp, top = 16.dp, bottom = 24.dp)) {
        item {
            Text("تنظیمات", fontWeight = FontWeight.Black, fontSize = 21.sp, color = Tokens.ink)
            Spacer(Modifier.height(24.dp))

            GroupTitle("زبان")
            Spacer(Modifier.height(12.dp))
            HbSegmentedControl(
                options = listOf("فارسی", "English"),
                selectedIndex = langIndex,
                onSelect = { langIndex = it }
            )
            Spacer(Modifier.height(24.dp))

            GroupTitle("حساب‌ها و کارت‌ها")
            Spacer(Modifier.height(12.dp))
        }

        items(accounts) { acc ->
            AccountRow(acc, onRemove = { viewModel.deleteAccount(acc) })
            Spacer(Modifier.height(8.dp))
        }

        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .border(2.dp, Tokens.neutral500)
                    .clickable { showAddAccount = true }
                    .padding(horizontal = 13.dp, vertical = 11.dp)
            ) {
                Text("+ افزودن حساب", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Tokens.neutral700)
            }
            Spacer(Modifier.height(24.dp))

            GroupTitle("شماره‌های مجاز پیامک")
            Text(
                "فقط پیامک‌های این فرستنده‌ها خوانده می‌شود.",
                fontSize = 12.sp, color = Tokens.neutral600, lineHeight = 21.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(senders) { sender ->
            SenderRow(sender, onToggle = { viewModel.toggleSender(sender) }, onDelete = { viewModel.deleteSender(sender) })
        }

        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .border(2.dp, Tokens.neutral500)
                    .clickable { showAddSender = true }
                    .padding(horizontal = 13.dp, vertical = 11.dp)
                    .padding(top = if (senders.isEmpty()) 0.dp else 8.dp)
            ) {
                Text("+ افزودن شماره", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Tokens.neutral700)
            }

            Spacer(Modifier.height(14.dp))
            Text(
                "وارد کردن پیامک‌های قدیمی ←",
                fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = Tokens.accent700,
                modifier = Modifier.clickable { onRequestSmsImport() }
            )
            importStatus?.let {
                Spacer(Modifier.height(6.dp))
                Text(it, fontSize = 11.5.sp, color = Tokens.neutral600)
            }
        }
    }

    if (showAddSender) {
        AddSenderDialog(onDismiss = { showAddSender = false }, onConfirm = { bank, number ->
            viewModel.addSender(bank, number)
            showAddSender = false
        })
    }
    if (showAddAccount) {
        AddAccountDialog(onDismiss = { showAddAccount = false }, onConfirm = { bank, last4, balance ->
            viewModel.addAccount(bank, last4, balance)
            showAddAccount = false
        })
    }
}

@Composable
private fun GroupTitle(text: String) {
    Text(
        text, fontWeight = FontWeight.Black, fontSize = 13.5.sp, color = Tokens.ink,
        modifier = Modifier.fillMaxWidth().padding(bottom = 7.dp).bottomBorderLine(2.dp, Tokens.ink)
    )
}

@Composable
private fun AccountRow(account: Account, onRemove: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(Tokens.white).border(2.dp, Tokens.ink).padding(horizontal = 13.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.width(26.dp).height(19.dp).background(Tokens.accent))
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(account.bankName, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Tokens.ink)
            Text("•${account.last4} — ${formatAmountFa(account.balance)} تومان", fontSize = 11.sp, color = Tokens.neutral600)
        }
        Text("×", fontWeight = FontWeight.Black, fontSize = 17.sp, color = Tokens.accent700,
            modifier = Modifier.clickable { onRemove() }.padding(2.dp))
    }
}

@Composable
private fun SenderRow(sender: Sender, onToggle: () -> Unit, onDelete: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().bottomBorderLine(1.dp, Tokens.neutral300).padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(sender.bankName, fontWeight = FontWeight.ExtraBold, fontSize = 14.5.sp, color = Tokens.ink)
            Text(sender.phoneNumber, fontSize = 11.5.sp, color = Tokens.neutral600)
        }
        HbToggle(checked = sender.enabled, onCheckedChange = { onToggle() })
        Spacer(Modifier.width(10.dp))
        Text("حذف", fontSize = 11.sp, color = Tokens.accent700, modifier = Modifier.clickable { onDelete() })
    }
}

@Composable
private fun AddSenderDialog(onDismiss: () -> Unit, onConfirm: (bank: String, number: String) -> Unit) {
    var bank by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    SimpleDialog(title = "افزودن شماره بانک", onDismiss = onDismiss, onConfirm = {
        if (bank.isNotBlank() && number.isNotBlank()) onConfirm(bank.trim(), number.trim())
    }) {
        PlainField(bank, { bank = it }, "نام بانک")
        Spacer(Modifier.height(10.dp))
        PlainField(number, { number = it }, "شماره فرستنده پیامک")
    }
}

@Composable
private fun AddAccountDialog(onDismiss: () -> Unit, onConfirm: (bank: String, last4: String, balance: Long) -> Unit) {
    var bank by remember { mutableStateOf("") }
    var last4 by remember { mutableStateOf("") }
    var balance by remember { mutableStateOf("") }
    SimpleDialog(title = "افزودن حساب", onDismiss = onDismiss, onConfirm = {
        if (bank.isNotBlank() && last4.length == 4) onConfirm(bank.trim(), last4, balance.toLongOrNull() ?: 0L)
    }) {
        PlainField(bank, { bank = it }, "نام بانک")
        Spacer(Modifier.height(10.dp))
        PlainField(last4, { v -> last4 = v.filter { it.isDigit() }.take(4) }, "۴ رقم آخر کارت")
        Spacer(Modifier.height(10.dp))
        PlainField(balance, { v -> balance = v.filter { it.isDigit() } }, "موجودی فعلی (تومان)")
    }
}

@Composable
private fun SimpleDialog(title: String, onDismiss: () -> Unit, onConfirm: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Box(Modifier.fillMaxSize().background(Tokens.ink.copy(alpha = 0.55f)).clickable { onDismiss() }, contentAlignment = Alignment.Center) {
        Column(
            Modifier
                .padding(32.dp)
                .fillMaxWidth()
                .background(Tokens.bg)
                .border(2.dp, Tokens.ink)
                .clickable(enabled = false) {}
                .padding(20.dp)
        ) {
            Text(title, fontWeight = FontWeight.Black, fontSize = 16.sp, color = Tokens.ink)
            Spacer(Modifier.height(14.dp))
            content()
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("انصراف", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Tokens.neutral600,
                    modifier = Modifier.clickable { onDismiss() })
                Box(Modifier.background(Tokens.accent).clickable { onConfirm() }.padding(horizontal = 16.dp, vertical = 9.dp)) {
                    Text("ثبت", color = Tokens.white, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun PlainField(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = Tokens.ink),
        modifier = Modifier.fillMaxWidth().background(Tokens.white).border(2.dp, Tokens.ink).padding(horizontal = 13.dp, vertical = 11.dp),
        decorationBox = { inner ->
            if (value.isEmpty()) Text(placeholder, color = Tokens.neutral500, fontSize = 14.sp)
            inner()
        }
    )
}

/** One-sided border helper (bottom only) for this screen's rules. */
private fun Modifier.bottomBorderLine(width: androidx.compose.ui.unit.Dp, color: androidx.compose.ui.graphics.Color): Modifier =
    this.then(Modifier.drawBehind {
        drawLine(color, androidx.compose.ui.geometry.Offset(0f, size.height), androidx.compose.ui.geometry.Offset(size.width, size.height), width.toPx())
    })
