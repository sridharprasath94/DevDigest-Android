package com.flash.devdigest.data.repository

import com.flash.devdigest.data.local.dao.NewsDao
import com.flash.devdigest.data.local.mapper.toDomain
import com.flash.devdigest.data.local.mapper.toEntity
import com.flash.devdigest.data.remote.api.NewsApi
import com.flash.devdigest.data.remote.dto.toDomainList
import com.flash.devdigest.domain.model.News
import com.flash.devdigest.domain.repository.NewsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class NewsRepositoryImpl @Inject constructor(
    private val api: NewsApi,
    private val newsDao: NewsDao,
    private val ioDispatcher: CoroutineDispatcher
) : NewsRepository {
    private val refreshMutex = Mutex()
    override fun observeTrending(): Flow<List<News>> {
        return newsDao.observeTrending()
            .map { entities ->
                entities.map { it.toDomain() }
            }
    }

    override fun observeFavorites(): Flow<List<News>> {
        return newsDao.observeFavorites()
            .map { entities ->
                entities.map { it.toDomain() }
            }
    }

    override suspend fun refreshTrendingNews() {
        refreshMutex.withLock {
            withContext(ioDispatcher) {
                val entities = api.getFrontPage()
                    .toDomainList()
                    .map { it.toEntity() }

                newsDao.insertAll(entities)
            }
        }
    }

    override suspend fun toggleFavorite(id: String) {
        withContext(ioDispatcher) {
            newsDao.toggleFavorite(id)
        }
    }
}