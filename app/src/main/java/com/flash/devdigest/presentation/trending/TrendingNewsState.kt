package com.flash.devdigest.presentation.trending

import com.flash.devdigest.domain.model.News
import com.flash.devdigest.domain.error.DomainError

 data class TrendingNewsState(
     val isLoading: Boolean = false,
     val news: List<News> = emptyList(),
 )