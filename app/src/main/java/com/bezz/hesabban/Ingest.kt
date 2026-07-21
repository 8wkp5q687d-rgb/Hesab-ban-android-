package com.bezz.hesabban

import android.content.Context
import com.bezz.hesabban.data.Account
import com.bezz.hesabban.data.AppDatabase
import com.bezz.hesabban.data.Transaction
import com.bezz.hesabban.parser.SmsParser

/**
 * Shared ingestion pipeline used by both the live SmsReceiver and the
 * one-time inbox backfill (SmsImporter), so allow-list/dedupe/balance-sync
 * logic lives in exactly one place.
 *
 * Returns true if a transaction was inserted, false if the SMS was ignored
 * (no allow-list match, not a transaction message, or a duplicate).
 */
object Ingest {

    suspend fun processSms(context: Context, sender: String?, body: String, receivedAt: Long): Boolean {
        val db = AppDatabase.getInstance(context)

        // Sender allow-list gate — per SMS_PARSING.md sec.1, never parse
        // messages from a number the user hasn't explicitly enabled.
        if (sender == null) return false
        val matches = db.senderDao().findMatching(sender)
        if (matches.isEmpty()) return false
        val bank = matches.first()

        val parsed = SmsParser.parse(sender, body) ?: return false

        if (db.transactionDao().existsByHash(parsed.hash) > 0) return false // dedupe

        val category = SmsParser.categorize(parsed.type, parsed.normalizedBody)

        var resolvedBankName = bank.bankName
        if (parsed.accountLast4 != null) {
            val existing = db.accountDao().find(parsed.accountLast4, bank.bankName)
            if (existing != null) {
                if (parsed.balance != null) {
                    db.accountDao().update(existing.copy(balance = parsed.balance, lastUpdated = receivedAt))
                }
            } else {
                db.accountDao().insert(
                    Account(
                        bankName = bank.bankName,
                        last4 = parsed.accountLast4,
                        balance = parsed.balance ?: 0,
                        lastUpdated = receivedAt
                    )
                )
            }
        }

        // SMS rarely names the merchant. Per SMS_PARSING.md sec.7 fallback:
        // when title is unknown, use "خرید کارتی — {بانک}" (user can rename later).
        val title = if (category != "سایر" && category != "درآمد") category else {
            if (parsed.type == "INCOME") "واریز — $resolvedBankName" else "خرید کارتی — $resolvedBankName"
        }

        db.transactionDao().insert(
            Transaction(
                amount = parsed.amount,
                type = parsed.type,
                category = category,
                title = title,
                description = parsed.rawBody.take(140),
                rawSms = parsed.rawBody,
                sender = sender,
                bankName = resolvedBankName,
                accountLast4 = parsed.accountLast4,
                date = receivedAt,
                source = "SMS",
                smsHash = parsed.hash
            )
        )
        return true
    }
}
