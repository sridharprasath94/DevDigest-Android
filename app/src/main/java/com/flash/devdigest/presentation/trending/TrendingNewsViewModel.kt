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
    data class Search(val query: String) : TrendingNewsIntent()
    data class ToggleFavorite(val id: String) : TrendingNewsIntent()
    object ClearSearch : TrendingNewsIntent()
}

@HiltViewModel
class TrendingNewsViewModel @Inject constructor(
    private val observeTrendingNewsUseCase: ObserveTrendingNewsUseCase,
    private val refreshTrendingNewsUseCase: RefreshTrendingNewsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TrendingNewsState())
    val state: StateFlow<TrendingNewsState> = _state.asStateFlow()

    private val _searchResults = MutableStateFlow<List<com.flash.devdigest.domain.model.News>?>(null)

    init {
        observeNews()
        processIntent(TrendingNewsIntent.Load)
    }

    fun processIntent(intent: TrendingNewsIntent) {
        when (intent) {
            TrendingNewsIntent.Load -> refresh()
            TrendingNewsIntent.Refresh -> refresh()

            is TrendingNewsIntent.Search -> search(intent.query)

            TrendingNewsIntent.ClearSearch -> {
                _searchResults.value = null
            }

            is TrendingNewsIntent.ToggleFavorite -> toggleFavorite(intent.id)
        }
    }

    private fun observeNews() {
        viewModelScope.launch {
            observeTrendingNewsUseCase().collect { news ->
                val baseList = _searchResults.value ?: news

                _state.update {
                    it.copy(
                        isLoading = false,
                        news = baseList,
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
    private fun search(query: String) {
        val current = _state.value.news

        if (query.isBlank()) {
            _searchResults.value = null
            return
        }

        _searchResults.value = current.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.author.contains(query, ignoreCase = true)
        }
    }

    private fun toggleFavorite(id: String) {
        val updated = _state.value.news.map {
            if (it.id == id) it.copy(isFavorite = !it.isFavorite) else it
        }

        _state.update { it.copy(news = updated) }
    }
}