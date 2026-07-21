package com.bezz.hesabban.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bezz.hesabban.data.CategorySummary
import com.bezz.hesabban.data.Sender
import com.bezz.hesabban.data.Transaction
import com.bezz.hesabban.viewmodel.CATEGORIES
import com.bezz.hesabban.viewmodel.TransactionViewModel

private val Color0Green = Color(0xFF2E7D32)
private val Color0Red = Color(0xFFC62828)

enum class Tab { HOME, REPORTS, SETTINGS }

/**
 * NOTE ON DESIGN FIDELITY [قطعی]: this is default Material3 styling, not the
 * "Modernist" flat/zero-radius/Vazirmatn look specified in
 * design_handoff_hesabban_android/README.md. Recreating that pixel-perfect is
 * a separate, sizable UI pass (custom shapes, bundled font, RTL layout
 * direction, custom toggle/chip components) — not done in this cut. The data
 * layer and parsing logic below DO follow SMS_PARSING.md / ANDROID_GUIDE.md.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(viewModel: TransactionViewModel, onRequestSmsImport: () -> Unit) {
    var tab by remember { mutableStateOf(Tab.HOME) }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("حساب‌بان") }) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = tab == Tab.HOME, onClick = { tab = Tab.HOME }, icon = {}, label = { Text("خانه") })
                NavigationBarItem(selected = tab == Tab.REPORTS, onClick = { tab = Tab.REPORTS }, icon = {}, label = { Text("گزارش‌ها") })
                NavigationBarItem(selected = tab == Tab.SETTINGS, onClick = { tab = Tab.SETTINGS }, icon = {}, label = { Text("تنظیمات") })
            }
        },
        floatingActionButton = {
            if (tab == Tab.HOME) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "افزودن")
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (tab) {
                Tab.HOME -> HomeScreen(viewModel, onRequestSmsImport)
                Tab.REPORTS -> ReportsScreen(viewModel)
                Tab.SETTINGS -> SettingsScreen(viewModel)
            }
        }
    }

    if (showAddDialog) {
        AddTransactionDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { amount, type, category, desc ->
                viewModel.addManual(amount, type, category, desc)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun HomeScreen(viewModel: TransactionViewModel, onRequestSmsImport: () -> Unit) {
    val transactions by viewModel.transactions.collectAsState()
    val importStatus by viewModel.importStatus.collectAsState()
    val accounts by viewModel.accounts.collectAsState()

    Column(Modifier.fillMaxSize().padding(12.dp)) {
        if (accounts.isNotEmpty()) {
            val total = accounts.sumOf { it.balance }
            Text("مانده کل: ${formatToman(total)}", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("تراکنش‌های اخیر", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = onRequestSmsImport) {
                Icon(Icons.Filled.Refresh, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("وارد کردن از پیامک")
            }
        }

        importStatus?.let {
            Text(it, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(8.dp))

        if (transactions.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("هنوز تراکنشی ثبت نشده. اول از تب «تنظیمات» شماره بانک رو فعال کن، بعد از پیامک وارد کن یا با + دستی اضافه کن.")
            }
        } else {
            LazyColumn {
                items(transactions) { tx ->
                    TransactionRow(tx, onDelete = { viewModel.delete(tx) })
                    Divider()
                }
            }
        }
    }
}

@Composable
fun TransactionRow(tx: Transaction, onDelete: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(tx.category, style = MaterialTheme.typography.bodyLarge)
            val meta = listOfNotNull(tx.bankName, if (tx.source == "SMS") "پیامک" else "دستی").joinToString(" · ")
            Text(meta, style = MaterialTheme.typography.bodySmall)
            Text(formatDate(tx.date), style = MaterialTheme.typography.labelSmall)
        }
        Column(horizontalAlignment = Alignment.End) {
            val sign = if (tx.type == "INCOME") "+" else "-"
            val color = if (tx.type == "INCOME") Color0Green else Color0Red
            Text("$sign${formatToman(tx.amount)}", color = color, style = MaterialTheme.typography.bodyLarge)
            TextButton(onClick = onDelete) { Text("حذف") }
        }
    }
}

@Composable
fun ReportsScreen(viewModel: TransactionViewModel) {
    val weekly by viewModel.weeklySummary.collectAsState()
    val monthly by viewModel.monthlySummary.collectAsState()

    LazyColumn(Modifier.fillMaxSize().padding(12.dp)) {
        item {
            Text("گزارش هفتگی", style = MaterialTheme.typography.titleMedium)
            SummaryBlock(weekly)
            Spacer(Modifier.height(16.dp))
            Text("گزارش ماهانه", style = MaterialTheme.typography.titleMedium)
            SummaryBlock(monthly)
        }
    }
}

@Composable
fun SummaryBlock(items: List<CategorySummary>) {
    if (items.isEmpty()) {
        Text("داده‌ای برای این بازه نیست.", style = MaterialTheme.typography.bodySmall)
        return
    }
    val income = items.filter { it.type == "INCOME" }.sumOf { it.total }
    val expense = items.filter { it.type == "EXPENSE" }.sumOf { it.total }

    Column(Modifier.padding(vertical = 8.dp)) {
        Text("درآمد: ${formatToman(income)}", color = Color0Green)
        Text("هزینه: ${formatToman(expense)}", color = Color0Red)
        Text("مانده: ${formatToman(income - expense)}", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(8.dp))
        items.forEach { s ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${s.category} (${if (s.type == "INCOME") "درآمد" else "هزینه"})")
                Text(formatToman(s.total))
            }
        }
    }
}

@Composable
fun SettingsScreen(viewModel: TransactionViewModel) {
    val senders by viewModel.senders.collectAsState()
    var showAddSender by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(12.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("شماره‌های مجاز پیامک بانک", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = { showAddSender = true }) { Text("افزودن +") }
        }
        Text(
            "فقط پیامک‌های شماره‌های فعال زیر پردازش می‌شن.",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(8.dp))

        if (senders.isEmpty()) {
            Text("هنوز شماره‌ای اضافه نشده.")
        } else {
            LazyColumn {
                items(senders) { s -> SenderRow(s, viewModel) }
            }
        }
    }

    if (showAddSender) {
        AddSenderDialog(
            onDismiss = { showAddSender = false },
            onConfirm = { bank, number ->
                viewModel.addSender(bank, number)
                showAddSender = false
            }
        )
    }
}

@Composable
fun SenderRow(sender: Sender, viewModel: TransactionViewModel) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(sender.bankName, style = MaterialTheme.typography.bodyLarge)
            Text(sender.phoneNumber, style = MaterialTheme.typography.bodySmall)
        }
        Switch(checked = sender.enabled, onCheckedChange = { viewModel.toggleSender(sender) })
        TextButton(onClick = { viewModel.deleteSender(sender) }) { Text("حذف") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSenderDialog(onDismiss: () -> Unit, onConfirm: (bankName: String, phoneNumber: String) -> Unit) {
    var bank by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("افزودن شماره بانک") },
        text = {
            Column {
                OutlinedTextField(value = bank, onValueChange = { bank = it }, label = { Text("نام بانک") }, singleLine = true)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = number, onValueChange = { number = it }, label = { Text("شماره فرستنده پیامک") }, singleLine = true)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (bank.isNotBlank() && number.isNotBlank()) onConfirm(bank.trim(), number.trim())
            }) { Text("ثبت") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onConfirm: (amount: Long, type: String, category: String, desc: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var isIncome by remember { mutableStateOf(false) }
    var category by remember { mutableStateOf(CATEGORIES.last()) } // "سایر"
    var desc by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("افزودن تراکنش دستی") },
        text = {
            Column {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() } },
                    label = { Text("مبلغ (تومان)") },
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("هزینه")
                    Switch(checked = isIncome, onCheckedChange = {
                        isIncome = it
                        category = if (it) "درآمد" else CATEGORIES.last()
                    })
                    Text("درآمد")
                }
                Spacer(Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("دسته‌بندی") },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                        CATEGORIES.forEach { c ->
                            DropdownMenuItem(text = { Text(c) }, onClick = {
                                category = c
                                categoryExpanded = false
                            })
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("توضیح") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amount = amountText.toLongOrNull() ?: 0L
                if (amount > 0) onConfirm(amount, if (isIncome) "INCOME" else "EXPENSE", category, desc)
            }) { Text("ثبت") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}
