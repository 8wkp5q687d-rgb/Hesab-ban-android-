package com.bezz.hesabban.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "transactions", indices = [Index(value = ["smsHash"], unique = true)])
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Long,          // normalized to Toman
    val type: String,          // "INCOME" or "EXPENSE"
    val category: String,      // خوراک، حمل‌ونقل، قبض‌ها، خرید، درآمد، سایر
    val title: String,         // short row title, e.g. "تاکسی" or "خرید کارتی — بانک ملت"
    val description: String,   // meta/detail text (unused directly by UI now, kept for compat)
    val rawSms: String? = null,
    val sender: String? = null,     // originating SMS number, if any
    val bankName: String? = null,   // resolved from sender allow-list, if matched
    val accountLast4: String? = null, // last 4 digits of card/account, if parsed
    val date: Long,            // epoch millis
    val source: String,        // "SMS" or "MANUAL"
    // sha1(sender + normalizedBody) — dedupe key per SMS_PARSING.md sec.6.
    // Nullable so manual entries don't collide (SQLite allows multiple NULLs in a unique index).
    val smsHash: String? = null
)

data class CategorySummary(
    val category: String,
    val type: String,
    val total: Long
)
