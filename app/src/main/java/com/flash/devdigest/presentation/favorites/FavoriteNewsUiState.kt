package com.flash.devdigest.presentation.favorites

import com.flash.devdigest.domain.model.News

sealed class FavoriteNewsUiState {
    object Empty : FavoriteNewsUiState()
    object Loading : FavoriteNewsUiState()
    data class Success(val repos: List<News>) : FavoriteNewsUiState()
}