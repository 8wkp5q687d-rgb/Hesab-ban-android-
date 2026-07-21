package com.bezz.hesabban.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Allow-list entry: only SMS from an enabled sender is ever parsed.
 * Per SMS_PARSING.md sec.1 — never scan the whole inbox by default.
 */
@Entity(tableName = "senders", indices = [Index(value = ["phoneNumber"], unique = true)])
data class Sender(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bankName: String,
    val phoneNumber: String,
    val enabled: Boolean = true
)
