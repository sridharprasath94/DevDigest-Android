package com.flash.devdigest.presentation.trending

sealed class TrendingNewsState {
    object Idle : TrendingNewsState()
    object Loading : TrendingNewsState()
}