package com.example.lab3.domain.usecase

import com.example.lab3.domain.model.FinancialOverview
import com.example.lab3.domain.repository.FinanceRepository

class GetFinancialOverviewUseCase(
    private val repository: FinanceRepository
) {
    suspend operator fun invoke(): FinancialOverview {
        return repository.getOverview()
    }
}
