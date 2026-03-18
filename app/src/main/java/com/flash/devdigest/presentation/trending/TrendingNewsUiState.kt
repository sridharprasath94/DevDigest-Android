package com.flash.devdigest.presentation.trending

sealed class TrendingNewsUiState {
    object Idle : TrendingNewsUiState()
    object Loading : TrendingNewsUiState()
}