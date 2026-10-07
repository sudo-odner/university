package com.example.lab3.domain.repository

import com.example.lab3.domain.model.FinancialOverview
import com.example.lab3.domain.model.Transaction

interface FinanceRepository {
    suspend fun getOverview(): FinancialOverview
    suspend fun getTransactionById(id: String): Transaction?
    suspend fun clearAllData(): FinancialOverview
    suspend fun loadMockData(): FinancialOverview
}
