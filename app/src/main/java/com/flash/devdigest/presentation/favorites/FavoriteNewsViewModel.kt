package com.flash.devdigest.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flash.devdigest.domain.model.News
import com.flash.devdigest.domain.usecase.ObserveFavoriteNewsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class FavoriteReposViewModel @Inject constructor(
    observeFavoriteNewsUseCase: ObserveFavoriteNewsUseCase
) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    private val favoriteReposFlow: StateFlow<List<News>> =
        observeFavoriteNewsUseCase()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val uiState: StateFlow<FavoriteNewsUiState> =
        combine(
            favoriteReposFlow,
            _isLoading,
        ) { favorites, isLoading ->
            FavoriteNewsUiState(
                isLoading = isLoading,
                news = favorites.sortedBy {
                    if (it.isFavorite) 0 else 1
                },
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FavoriteNewsUiState(isLoading = true, news = emptyList())
        )
}