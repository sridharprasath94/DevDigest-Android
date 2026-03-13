package com.flash.devdigest.domain.repository

import com.flash.devdigest.domain.model.News
import kotlinx.coroutines.flow.Flow

interface NewsRepository {

    fun observeTrending(): Flow<List<News>>

    fun observeFavorites(): Flow<List<News>>

    suspend fun refreshTrendingNews()

    suspend fun toggleFavorite(id: String)
}