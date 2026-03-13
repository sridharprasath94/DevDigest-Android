package com.flash.devdigest.data.repository

import com.flash.devdigest.core.Result
import com.flash.devdigest.data.error.NetworkErrorMapper
import com.flash.devdigest.data.error.NetworkErrorMapper.toDomain
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
import kotlin.coroutines.cancellation.CancellationException

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

    override suspend fun refreshTrendingNews(): Result<Unit> {
        return withContext(ioDispatcher) {
            refreshMutex.withLock {
                try {
                    val favoriteIds = newsDao.getFavoriteIds().toSet()

                    val entities = api.getFrontPage()
                        .toDomainList()
                        .map { news ->
                            news.copy(isFavorite = favoriteIds.contains(news.id))
                        }
                        .map { it.toEntity() }

                    newsDao.insertAll(entities)
                    Result.Success(Unit)
                } catch (t: Throwable) {
                    if (t is CancellationException) throw t
                    Result.Error(NetworkErrorMapper.fromThrowable(t).toDomain())
                }
            }
        }

    }

    override suspend fun toggleFavorite(news: News): Result<Unit> {
        return withContext(ioDispatcher) {
            try {
                newsDao.toggleFavorite(news.id)
                Result.Success(Unit)
            } catch (t: Throwable) {
                if (t is CancellationException) throw t
                Result.Error(NetworkErrorMapper.fromThrowable(t).toDomain())
            }

        }
    }

    override suspend fun searchNews(query: String): Result<List<News>> {
        return withContext(ioDispatcher) {
            try {
                val news = api.searchNews(query)
                    .toDomainList()

                Result.Success(news)
            } catch (t: Throwable) {
                if (t is CancellationException) throw t
                Result.Error(NetworkErrorMapper.fromThrowable(t).toDomain())
            }
        }
    }
}