package com.flash.devdigest.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flash.devdigest.domain.usecase.ObserveFavoriteNewsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class FavoriteReposViewModel @Inject constructor(
    observeFavoriteNewsUseCase: ObserveFavoriteNewsUseCase
) : ViewModel() {
    val state: StateFlow<FavoriteNewsUiState> =
        observeFavoriteNewsUseCase()
            .map { news ->
                if (news.isEmpty()) {
                    FavoriteNewsUiState.Empty
                } else {
                    FavoriteNewsUiState.Success(
                        news.sortedByDescending { it.isFavorite }
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = FavoriteNewsUiState.Loading
            )
}