package com.bezz.hesabban.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(account: Account): Long

    @Update
    suspend fun update(account: Account)

    @Delete
    suspend fun delete(account: Account)

    @Query("SELECT * FROM accounts ORDER BY bankName")
    fun getAll(): Flow<List<Account>>

    @Query("SELECT * FROM accounts WHERE last4 = :last4 AND bankName = :bankName LIMIT 1")
    suspend fun find(last4: String, bankName: String): Account?

    @Query("SELECT COALESCE(SUM(balance), 0) FROM accounts")
    suspend fun totalBalance(): Long
}
