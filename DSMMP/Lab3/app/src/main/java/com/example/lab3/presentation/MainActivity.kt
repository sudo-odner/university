package com.example.lab3.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.lab3.presentation.details.TransactionDetailScreen
import com.example.lab3.presentation.overview.OverviewScreen
import com.example.lab3.presentation.overview.OverviewUiState
import com.example.lab3.presentation.overview.OverviewViewModel
import com.example.lab3.presentation.overview.OverviewViewModelFactory
import com.example.lab3.presentation.theme.FinanceAppTheme
import com.example.lab3.presentation.theme.ThemeMode

class MainActivity : ComponentActivity() {

    private val viewModel: OverviewViewModel by viewModels {
        OverviewViewModelFactory()
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val windowSizeClass = calculateWindowSizeClass(activity = this)
            val themeMode by viewModel.themeMode.collectAsState()
            val uiState by viewModel.uiState.collectAsState()
            val selectedTxId by viewModel.selectedTransactionId.collectAsState()

            val isDark = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            FinanceAppTheme(darkTheme = isDark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AnimatedContent(
                        targetState = selectedTxId,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "screen_transition"
                    ) { targetId ->
                        if (targetId == null) {
                            OverviewScreen(
                                uiState = uiState,
                                themeMode = themeMode,
                                windowWidthSizeClass = windowSizeClass.widthSizeClass,
                                onTransactionClick = { txId -> viewModel.selectTransaction(txId) },
                                onPopulateMockData = { viewModel.populateData() },
                                onClearData = { viewModel.clearData() },
                                onToggleThemeMode = { viewModel.toggleNextThemeMode() }
                            )
                        } else {
                            val selectedTx = (uiState as? OverviewUiState.Content)?.overview
                                ?.recentTransactions
                                ?.find { it.id == targetId }

                            TransactionDetailScreen(
                                transaction = selectedTx,
                                onBackClick = { viewModel.selectTransaction(null) }
                            )
                        }
                    }
                }
            }
        }
    }
}
