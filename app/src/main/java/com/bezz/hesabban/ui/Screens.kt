package com.bezz.hesabban.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bezz.hesabban.ui.icons.NavIconPaths
import com.bezz.hesabban.ui.icons.SvgStrokeIcon
import com.bezz.hesabban.ui.theme.Tokens
import com.bezz.hesabban.viewmodel.TransactionViewModel

enum class Tab { HOME, TXS, REPORT, SETTINGS }

/**
 * Root shell — recreates the "Modernist" app frame from
 * design_handoff_hesabban_android/Finance App v2.dc.html: RTL, flat bg,
 * bottom nav with 4 tabs, no shadows/gradients/rounded corners anywhere.
 */
@Composable
fun AppRoot(viewModel: TransactionViewModel, onRequestSmsImport: () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        var tab by remember { mutableStateOf(Tab.HOME) }
        var expandedTxId by remember { mutableStateOf<Long?>(null) }
        var showAddSheet by remember { mutableStateOf(false) }

        Box(Modifier.fillMaxSize().background(Tokens.bg)) {
            Column(Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f)) {
                    when (tab) {
                        Tab.HOME -> DashboardScreen(viewModel)
                        Tab.TXS -> TransactionsScreen(
                            viewModel = viewModel,
                            expandedId = expandedTxId,
                            onToggleExpand = { expandedTxId = if (expandedTxId == it) null else it },
                            onAddManual = { showAddSheet = true }
                        )
                        Tab.REPORT -> ReportScreen(viewModel)
                        Tab.SETTINGS -> SettingsScreen(viewModel, onRequestSmsImport)
                    }
                }
                BottomNav(selected = tab, onSelect = { tab = it; expandedTxId = null })
            }

            if (showAddSheet) {
                AddTransactionSheet(
                    onDismiss = { showAddSheet = false },
                    onSave = { amount, type, category, title ->
                        viewModel.addManual(amount, type, category, title)
                        showAddSheet = false
                    }
                )
            }
        }
    }
}

@Composable
private fun BottomNav(selected: Tab, onSelect: (Tab) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Tokens.white)
            .border(2.dp, Tokens.ink)
            .padding(bottom = 6.dp)
    ) {
        NavItem(NavIconPaths.HOME, "خانه", selected == Tab.HOME, Modifier.weight(1f)) { onSelect(Tab.HOME) }
        NavItem(NavIconPaths.TX, "تراکنش‌ها", selected == Tab.TXS, Modifier.weight(1f)) { onSelect(Tab.TXS) }
        NavItem(NavIconPaths.REPORT, "گزارش", selected == Tab.REPORT, Modifier.weight(1f)) { onSelect(Tab.REPORT) }
        NavItem(NavIconPaths.SETTINGS, "تنظیمات", selected == Tab.SETTINGS, Modifier.weight(1f)) { onSelect(Tab.SETTINGS) }
    }
}

@Composable
private fun NavItem(path: String, label: String, active: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val color = if (active) Tokens.accent else Tokens.neutral500
    val textColor = if (active) Tokens.ink else Tokens.neutral500
    Column(
        modifier = modifier
            .clickable { onClick() }
            .padding(top = 11.dp, bottom = 8.dp, start = 4.dp, end = 4.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        SvgStrokeIcon(path, tint = color, size = 21.dp, strokeWidthPx = 1.9f)
        Spacer(Modifier.height(4.dp))
        Text(label, color = textColor, fontWeight = FontWeight.ExtraBold, fontSize = 10.5.sp)
    }
}
