package com.flash.devdigest.domain.repository

import androidx.paging.PagingData
import com.flash.devdigest.domain.model.News
import kotlinx.coroutines.flow.Flow
import com.flash.devdigest.core.Result

interface NewsRepository {
    fun observePagedTrendingNews() : Flow<PagingData<News>>

    fun observeSearchNews(query: String): Flow<PagingData<News>>

    fun observeFavoriteNews(): Flow<List<News>>

    suspend fun toggleFavorite(news: News): Result<Unit>
}