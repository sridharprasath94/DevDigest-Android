package com.flash.devdigest.presentation.trending

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flash.devdigest.domain.usecase.ObserveTrendingNewsUseCase
import com.flash.devdigest.domain.usecase.RefreshTrendingNewsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.flash.devdigest.core.Result
import javax.inject.Inject

sealed class TrendingNewsIntent {
    object Load : TrendingNewsIntent()
    object Refresh : TrendingNewsIntent()
}

@HiltViewModel
class TrendingNewsViewModel @Inject constructor(
    private val observeTrendingNewsUseCase: ObserveTrendingNewsUseCase,
    private val refreshTrendingNewsUseCase: RefreshTrendingNewsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TrendingNewsState())
    val state: StateFlow<TrendingNewsState> = _state.asStateFlow()

    init {
        observeNews()
        processIntent(TrendingNewsIntent.Load)
    }

    fun processIntent(intent: TrendingNewsIntent) {
        when (intent) {
            TrendingNewsIntent.Load -> refresh()
            TrendingNewsIntent.Refresh -> refresh()
        }
    }

    private fun observeNews() {
        viewModelScope.launch {
            observeTrendingNewsUseCase().collect { news ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        news = news,
                        error = null
                    )
                }
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = refreshTrendingNewsUseCase()) {
                is Result.Success -> {
                    // No-op. Room Flow will emit updated data which observeNews() collects.
                    Unit
                }

                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = UiError.fromDomain(result.error)
                        )
                    }
                }
            }
        }
    }
}