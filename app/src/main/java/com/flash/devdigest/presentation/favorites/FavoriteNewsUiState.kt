package com.flash.devdigest.presentation.favorites

import com.flash.devdigest.domain.model.News

data class FavoriteNewsUiState(
    val isLoading: Boolean = false,
    val news: List<News> = emptyList(),
    val error: String? = null
)