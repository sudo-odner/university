package com.example.lab3.data.repository

import com.example.lab3.data.datasource.FinanceMockDataSource
import com.example.lab3.domain.model.FinancialOverview
import com.example.lab3.domain.model.Transaction
import com.example.lab3.domain.repository.FinanceRepository
import kotlinx.coroutines.delay

class FinanceRepositoryImpl(
    private val simulateNetworkDelayMs: Long = 600L
) : FinanceRepository {

    private var currentOverview: FinancialOverview = FinanceMockDataSource.createInitialOverview()

    override suspend fun getOverview(): FinancialOverview {
        if (simulateNetworkDelayMs > 0) {
            delay(simulateNetworkDelayMs)
        }
        return currentOverview
    }

    override suspend fun getTransactionById(id: String): Transaction? {
        if (simulateNetworkDelayMs > 0) {
            delay(simulateNetworkDelayMs / 2)
        }
        return currentOverview.recentTransactions.find { it.id == id }
    }

    override suspend fun clearAllData(): FinancialOverview {
        if (simulateNetworkDelayMs > 0) {
            delay(simulateNetworkDelayMs)
        }
        currentOverview = FinanceMockDataSource.createEmptyOverview()
        return currentOverview
    }

    override suspend fun loadMockData(): FinancialOverview {
        if (simulateNetworkDelayMs > 0) {
            delay(simulateNetworkDelayMs)
        }
        currentOverview = FinanceMockDataSource.createInitialOverview()
        return currentOverview
    }
}
