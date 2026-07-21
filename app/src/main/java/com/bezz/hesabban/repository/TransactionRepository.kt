package com.bezz.hesabban.repository

import com.bezz.hesabban.data.CategorySummary
import com.bezz.hesabban.data.Transaction
import com.bezz.hesabban.data.TransactionDao
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val dao: TransactionDao) {

    fun getAll(): Flow<List<Transaction>> = dao.getAll()

    suspend fun add(transaction: Transaction) = dao.insert(transaction)

    suspend fun update(transaction: Transaction) = dao.update(transaction)

    suspend fun delete(transaction: Transaction) = dao.delete(transaction)

    suspend fun summary(start: Long, end: Long): List<CategorySummary> =
        dao.getSummaryByCategory(start, end)
}
