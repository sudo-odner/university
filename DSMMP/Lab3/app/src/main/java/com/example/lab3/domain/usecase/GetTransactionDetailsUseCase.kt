package com.example.lab3.domain.usecase

import com.example.lab3.domain.model.Transaction
import com.example.lab3.domain.repository.FinanceRepository

class GetTransactionDetailsUseCase(
    private val repository: FinanceRepository
) {
    suspend operator fun invoke(id: String): Transaction? {
        return repository.getTransactionById(id)
    }
}
