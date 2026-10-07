package com.example.lab3.presentation.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.lab3.data.datasource.FinanceMockDataSource
import com.example.lab3.data.repository.FinanceRepositoryImpl
import com.example.lab3.domain.repository.FinanceRepository
import com.example.lab3.domain.usecase.GetFinancialOverviewUseCase
import com.example.lab3.domain.usecase.ResetFinanceDataUseCase

class OverviewViewModelFactory(
    private val repository: FinanceRepository = FinanceRepositoryImpl()
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OverviewViewModel::class.java)) {
            val getFinancialOverviewUseCase = GetFinancialOverviewUseCase(repository)
            val resetFinanceDataUseCase = ResetFinanceDataUseCase(repository)
            return OverviewViewModel(
                getFinancialOverviewUseCase = getFinancialOverviewUseCase,
                resetFinanceDataUseCase = resetFinanceDataUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
