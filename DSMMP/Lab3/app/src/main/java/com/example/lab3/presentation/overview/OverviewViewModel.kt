package com.example.lab3.presentation.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lab3.domain.usecase.GetFinancialOverviewUseCase
import com.example.lab3.domain.usecase.ResetFinanceDataUseCase
import com.example.lab3.presentation.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OverviewViewModel(
    private val getFinancialOverviewUseCase: GetFinancialOverviewUseCase,
    private val resetFinanceDataUseCase: ResetFinanceDataUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<OverviewUiState>(OverviewUiState.Loading)
    val uiState: StateFlow<OverviewUiState> = _uiState.asStateFlow()

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _selectedTransactionId = MutableStateFlow<String?>(null)
    val selectedTransactionId: StateFlow<String?> = _selectedTransactionId.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = OverviewUiState.Loading
            val overview = getFinancialOverviewUseCase()
            if (overview.accounts.isEmpty() && overview.recentTransactions.isEmpty()) {
                _uiState.value = OverviewUiState.Empty
            } else {
                _uiState.value = OverviewUiState.Content(overview)
            }
        }
    }

    fun clearData() {
        viewModelScope.launch {
            _uiState.value = OverviewUiState.Loading
            resetFinanceDataUseCase.clear()
            _uiState.value = OverviewUiState.Empty
            _selectedTransactionId.value = null
        }
    }

    fun populateData() {
        viewModelScope.launch {
            _uiState.value = OverviewUiState.Loading
            val overview = resetFinanceDataUseCase.loadMock()
            _uiState.value = OverviewUiState.Content(overview)
        }
    }

    fun selectTransaction(id: String?) {
        _selectedTransactionId.value = id
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }

    fun toggleNextThemeMode() {
        _themeMode.value = when (_themeMode.value) {
            ThemeMode.SYSTEM -> ThemeMode.LIGHT
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.SYSTEM
        }
    }
}
