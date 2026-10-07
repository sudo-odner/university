package com.example.lab3.presentation.overview

import com.example.lab3.domain.model.FinancialOverview

sealed interface OverviewUiState {
    data object Loading : OverviewUiState
    data object Empty : OverviewUiState
    data class Content(val overview: FinancialOverview) : OverviewUiState
}
