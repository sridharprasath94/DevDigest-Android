package com.flash.devdigest.presentation.trending

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.flash.devdigest.core.Result
import com.flash.devdigest.domain.model.News
import com.flash.devdigest.domain.usecase.ObservePagedTrendingNewsUseCase
import com.flash.devdigest.domain.usecase.SearchNewsUseCase
import com.flash.devdigest.domain.usecase.ToggleFavoriteUseCase
import com.flash.devdigest.presentation.error.UIError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class TrendingNewsIntent {
    data class OnSearchQueryChanged(val query: String) : TrendingNewsIntent()
    data class ToggleFavorite(val news: News) : TrendingNewsIntent()
}

@OptIn(FlowPreview::class)
@HiltViewModel
class TrendingNewsViewModel @Inject constructor(
    observePagedTrendingNewsUseCase: ObservePagedTrendingNewsUseCase,
    private val searchNewsUseCase: SearchNewsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TrendingNewsState())
    val state: StateFlow<TrendingNewsState> = _state.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    private val _events = MutableSharedFlow<UIError>()
    val events = _events.asSharedFlow()

    private val _searchResults = MutableStateFlow<PagingData<News>?>(null)

    private val _pagedNews: Flow<PagingData<News>> =
        observePagedTrendingNewsUseCase()
            .cachedIn(viewModelScope)

    val newsFlow: Flow<PagingData<News>> =
        combine(
            _pagedNews,
            _searchResults
        ) { paging, search ->
            search ?: paging
        }
    private var searchJob: Job? = null

    init {
        observeSearchQuery()
    }

    fun processIntent(intent: TrendingNewsIntent) {
        when (intent) {
            is TrendingNewsIntent.OnSearchQueryChanged -> _searchQuery.value = intent.query

            is TrendingNewsIntent.ToggleFavorite -> toggleFavorite(intent.news)
        }
    }


    private fun observeSearchQuery() {
        viewModelScope.launch {
            _searchQuery
                .debounce(400)
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.isBlank()) {
                        clearSearch()
                    } else {
                        performSearch(query)
                    }
                }
        }
    }

    private fun performSearch(query: String) {
        if (query.isBlank()) {
            clearSearch()
            return
        }

        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            searchNewsUseCase(query)
                .cachedIn(viewModelScope)
                .collectLatest { pagingData ->
                    _searchResults.value = pagingData
                    _state.update { it.copy(isLoading = false) }
                }
        }
    }

    fun clearSearch() {
        _searchResults.value = null
        _state.update {
            it.copy(isLoading = false)
        }
    }

    private fun toggleFavorite(news: News) {
        viewModelScope.launch {
            when (val result = toggleFavoriteUseCase(news)) {
                is Result.Success -> {
                }

                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                        )
                    }
                    _events.emit(UIError.from(result.error))
                }
            }
        }
    }
}