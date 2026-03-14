package com.flash.devdigest.domain.repository

import com.flash.devdigest.domain.model.News
import kotlinx.coroutines.flow.Flow
import com.flash.devdigest.core.Result

interface NewsRepository {

    fun observeTrendingNews(): Flow<List<News>>

    suspend fun refreshTrendingNews(): Result<Unit>

    suspend fun searchNews(query: String): Result<List<News>>

    suspend fun toggleFavorite(news: News): Result<Unit>

    fun observeFavoriteNews(): Flow<List<News>>
}