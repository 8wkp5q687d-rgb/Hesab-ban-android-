package com.bezz.hesabban.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(transaction: Transaction): Long

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAll(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    suspend fun getInRange(start: Long, end: Long): List<Transaction>

    @Query("""
        SELECT category, type, SUM(amount) as total
        FROM transactions
        WHERE date BETWEEN :start AND :end
        GROUP BY category, type
        ORDER BY total DESC
    """)
    suspend fun getSummaryByCategory(start: Long, end: Long): List<CategorySummary>

    @Query("SELECT COUNT(*) FROM transactions WHERE smsHash = :hash")
    suspend fun existsByHash(hash: String): Int
}
