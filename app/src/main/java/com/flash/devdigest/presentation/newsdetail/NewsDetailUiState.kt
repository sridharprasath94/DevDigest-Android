package com.flash.devdigest.presentation.newsdetail

import com.flash.devdigest.domain.model.News

sealed class NewsDetailUiState {
    object Initial :  NewsDetailUiState()
    data class Success(val news: News) :  NewsDetailUiState()
}