package com.flash.devdigest.data.repository

import com.flash.devdigest.core.Result
import com.flash.devdigest.data.error.NetworkErrorMapper
import com.flash.devdigest.data.error.NetworkErrorMapper.toDomain
import com.flash.devdigest.data.local.dao.NewsDao
import com.flash.devdigest.data.local.entity.NewsEntity
import com.flash.devdigest.data.local.mapper.toDomain
import com.flash.devdigest.data.local.mapper.toEntity
import com.flash.devdigest.data.remote.api.NewsApi
import com.flash.devdigest.data.remote.dto.NewsResponseDto
import com.flash.devdigest.data.remote.dto.toDomainList
import com.flash.devdigest.domain.model.News
import com.flash.devdigest.domain.repository.NewsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import kotlin.collections.map
import kotlin.coroutines.cancellation.CancellationException

class NewsRepositoryImpl @Inject constructor(
    private val api: NewsApi,
    private val newsDao: NewsDao,
    private val ioDispatcher: CoroutineDispatcher
) : NewsRepository {
    private val refreshMutex = Mutex()

    override fun observeTrendingNews(): Flow<List<News>> {
        return newsDao.observeTrending()
            .map { entities ->
                entities.map { it.toDomain() }
            }
    }

    override fun observeFavoriteNews(): Flow<List<News>> {
        return newsDao.observeFavorites()
            .map { entities ->
                entities.map { it.toDomain() }
            }
    }

    override suspend fun refreshTrendingNews(): Result<Unit> {
        return withContext(ioDispatcher) {
            refreshMutex.withLock {
                try {
                    val response: NewsResponseDto = api.getFrontPage()
                    val favoriteIds = newsDao.getFavoriteIdsSet()
                    val favoriteNews = newsDao.observeFavoriteNewsOnce()

                    val entities = response
                        .toDomainList()
                        .applyFavorites(favoriteIds)
                        .map { it.toEntity() }

                    newsDao.clearNews()
                    newsDao.insertAllNews(entities)

                    // Re‑insert favorite news that are not part of the trending list
                    favoriteNews
                        .filter { fav -> entities.none { it.id == fav.id } }
                        .map { it.toDomain().toEntity() }
                        .let {  newsDao.insertAllNews(it) }

                    Result.Success(Unit)
                } catch (t: Throwable) {
                    if (t is CancellationException) throw t
                    Result.Error(NetworkErrorMapper.fromThrowable(t).toDomain())
                }
            }
        }
    }

    override suspend fun searchNews(query: String): Result<List<News>> {
        return withContext(ioDispatcher) {
            try {
                val response: NewsResponseDto = api.searchNews(query)
                val favoriteIds = newsDao.getFavoriteIdsSet()
                val news: List<News> = response
                    .toDomainList()
                    .applyFavorites(favoriteIds)

                Result.Success(news)
            } catch (t: Throwable) {
                if (t is CancellationException) throw t
                Result.Error(NetworkErrorMapper.fromThrowable(t).toDomain())
            }
        }
    }

    override suspend fun toggleFavorite(news: News): Result<Unit> {
        return withContext(ioDispatcher) {
            try {
                val existing = newsDao.getNewsById(news.id)
                if (existing == null) {
                    newsDao.insert(news.toEntity())
                }
                newsDao.toggleFavorite(news.id)
                Result.Success(Unit)
            } catch (t: Throwable) {
                if (t is CancellationException) throw t
                Result.Error(NetworkErrorMapper.fromThrowable(t).toDomain())
            }

        }
    }
}

private suspend fun NewsDao.getFavoriteIdsSet(): Set<Long> {
    return getFavoriteIds().toSet()
}

private fun List<News>.applyFavorites(favoriteIds: Set<Long>): List<News> {
    return map { news ->
        news.copy(isFavorite = news.id in favoriteIds)
    }
}

private suspend fun NewsDao.observeFavoriteNewsOnce(): List<NewsEntity> {
    return observeFavorites().first()
}
