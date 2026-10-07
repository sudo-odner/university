package com.example.lab3.domain.usecase

import com.example.lab3.domain.model.FinancialOverview
import com.example.lab3.domain.repository.FinanceRepository

class ResetFinanceDataUseCase(
    private val repository: FinanceRepository
) {
    suspend fun clear(): FinancialOverview = repository.clearAllData()
    suspend fun loadMock(): FinancialOverview = repository.loadMockData()
}
