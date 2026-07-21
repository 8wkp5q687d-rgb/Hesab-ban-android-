package com.bezz.hesabban.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SenderDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(sender: Sender): Long

    @Update
    suspend fun update(sender: Sender)

    @Delete
    suspend fun delete(sender: Sender)

    @Query("SELECT * FROM senders ORDER BY bankName")
    fun getAll(): Flow<List<Sender>>

    @Query("SELECT * FROM senders WHERE enabled = 1")
    suspend fun getEnabled(): List<Sender>

    // Matches exact number or prefix (bank short-codes are sometimes reported
    // by the OS with country-code variants) — per SMS_PARSING.md sec.1.
    @Query("SELECT * FROM senders WHERE enabled = 1 AND (:number LIKE phoneNumber || '%' OR phoneNumber LIKE :number || '%')")
    suspend fun findMatching(number: String): List<Sender>
}
