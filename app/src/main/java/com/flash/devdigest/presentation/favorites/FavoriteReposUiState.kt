package com.flash.devdigest.presentation.favorites

import com.flash.devdigest.domain.model.News

data class FavoriteReposUiState(
    val isLoading: Boolean = false,
    val repos: List<News> = emptyList(),
    val error: String? = null
)