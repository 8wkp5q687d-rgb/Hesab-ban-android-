package com.bezz.hesabban.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A bank card/account, auto-detected from SMS (last-4 digits) or added manually.
 * balance is authoritative from the latest "مانده/موجودی" seen in an SMS for that
 * account — see SMS_PARSING.md sec.5.
 */
@Entity(tableName = "accounts", indices = [Index(value = ["last4", "bankName"], unique = true)])
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bankName: String,
    val last4: String,
    val balance: Long = 0, // Toman
    val lastUpdated: Long = System.currentTimeMillis()
)
