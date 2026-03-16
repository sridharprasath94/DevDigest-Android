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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class TrendingNewsIntent {
    data class Search(val query: String) : TrendingNewsIntent()
    data class OnSearchQueryChanged(val query: String) : TrendingNewsIntent()
    data class ToggleFavorite(val news: News) : TrendingNewsIntent()
    object ClearSearch : TrendingNewsIntent()
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

    private val searchQuery = MutableStateFlow("")

    private val _events = MutableSharedFlow<UIError>()
    val events = _events.asSharedFlow()

    private val _searchResults =
        MutableStateFlow<List<News>?>(null)

    val pagedNews: Flow<PagingData<News>> =
        observePagedTrendingNewsUseCase()
            .cachedIn(viewModelScope)

    init {
        observeNews()
        observeSearchQuery()
    }

    fun processIntent(intent: TrendingNewsIntent) {
        when (intent) {
            is TrendingNewsIntent.Search -> performSearch(intent.query)

            is TrendingNewsIntent.OnSearchQueryChanged -> searchQuery.value = intent.query

            TrendingNewsIntent.ClearSearch -> {
                _searchResults.value = null
            }

            is TrendingNewsIntent.ToggleFavorite -> toggleFavorite(intent.news)
        }
    }

    private fun observeNews() {
        viewModelScope.launch {
            _searchResults.collect { results ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        news = results ?: emptyList()
                    )
                }
            }
        }
    }

    private fun observeSearchQuery() {
        viewModelScope.launch {
            searchQuery
                .debounce(400)
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.isBlank()) {
                        processIntent(TrendingNewsIntent.ClearSearch)
                    } else {
                        performSearch(query)
                    }
                }
        }
    }



    private fun performSearch(query: String) {
        if (query.isBlank()) {
            _searchResults.value = null
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            when (val result = searchNewsUseCase(query)) {
                is Result.Success -> {
                    _searchResults.value = result.data
                    _state.update { it.copy(isLoading = false) }
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

    private fun toggleFavorite(news: News) {
        viewModelScope.launch {
            when (val result = toggleFavoriteUseCase(news)) {
                is Result.Success -> {
                    _searchResults.value = _searchResults.value?.map {
                        if (it.id == news.id)
                            it.copy(isFavorite = !it.isFavorite)
                        else it
                    }
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