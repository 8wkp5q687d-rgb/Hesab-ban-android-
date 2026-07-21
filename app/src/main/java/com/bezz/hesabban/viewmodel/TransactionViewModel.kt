package com.bezz.hesabban.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bezz.hesabban.SmsImporter
import com.bezz.hesabban.data.Account
import com.bezz.hesabban.data.AppDatabase
import com.bezz.hesabban.data.CategorySummary
import com.bezz.hesabban.data.Sender
import com.bezz.hesabban.data.Transaction
import com.bezz.hesabban.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

/** Categories used across the app — must match SMS_PARSING.md sec.7. */
val CATEGORIES = listOf("خوراک", "حمل‌ونقل", "قبض‌ها", "خرید", "درآمد", "سایر")

class TransactionViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repo = TransactionRepository(db.transactionDao())

    val transactions: StateFlow<List<Transaction>> =
        repo.getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val senders: StateFlow<List<Sender>> =
        db.senderDao().getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accounts: StateFlow<List<Account>> =
        db.accountDao().getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _weeklySummary = MutableStateFlow<List<CategorySummary>>(emptyList())
    val weeklySummary: StateFlow<List<CategorySummary>> = _weeklySummary

    private val _monthlySummary = MutableStateFlow<List<CategorySummary>>(emptyList())
    val monthlySummary: StateFlow<List<CategorySummary>> = _monthlySummary

    private val _importStatus = MutableStateFlow<String?>(null)
    val importStatus: StateFlow<String?> = _importStatus

    init {
        refreshSummaries()
    }

    fun refreshSummaries() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            _weeklySummary.value = repo.summary(now - 7L * 24 * 60 * 60 * 1000, now)

            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            _monthlySummary.value = repo.summary(cal.timeInMillis, now)
        }
    }

    fun addManual(amount: Long, type: String, category: String, description: String) {
        viewModelScope.launch {
            repo.add(
                Transaction(
                    amount = amount,
                    type = type,
                    category = category,
                    description = description,
                    date = System.currentTimeMillis(),
                    source = "MANUAL"
                )
            )
            refreshSummaries()
        }
    }

    fun update(transaction: Transaction) {
        viewModelScope.launch {
            repo.update(transaction)
            refreshSummaries()
        }
    }

    fun delete(transaction: Transaction) {
        viewModelScope.launch {
            repo.delete(transaction)
            refreshSummaries()
        }
    }

    fun importSmsHistory() {
        viewModelScope.launch {
            _importStatus.value = "در حال وارد کردن..."
            val count = SmsImporter.importInboxHistory(getApplication())
            _importStatus.value = "$count تراکنش وارد شد"
            refreshSummaries()
        }
    }

    fun clearImportStatus() {
        _importStatus.value = null
    }

    // --- Sender allow-list management (SMS_PARSING.md sec.1) ---

    fun addSender(bankName: String, phoneNumber: String) {
        viewModelScope.launch {
            db.senderDao().insert(Sender(bankName = bankName, phoneNumber = phoneNumber, enabled = true))
        }
    }

    fun toggleSender(sender: Sender) {
        viewModelScope.launch {
            db.senderDao().update(sender.copy(enabled = !sender.enabled))
        }
    }

    fun deleteSender(sender: Sender) {
        viewModelScope.launch {
            db.senderDao().delete(sender)
        }
    }
}
