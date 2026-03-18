package com.flash.devdigest.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.flash.devdigest.core.DataResult
import com.flash.devdigest.data.error.NetworkErrorMapper
import com.flash.devdigest.data.error.NetworkErrorMapper.toDomain
import com.flash.devdigest.data.local.AppDatabase
import com.flash.devdigest.data.local.dao.NewsDao
import com.flash.devdigest.data.local.entity.NewsEntity
import com.flash.devdigest.data.local.mapper.toDomain
import com.flash.devdigest.data.local.mapper.toEntity
import com.flash.devdigest.data.remote.api.NewsApi
import com.flash.devdigest.data.remote.paging.NewsRemoteMediator
import com.flash.devdigest.data.remote.paging.NewsSearchPagingSource
import com.flash.devdigest.domain.model.News
import com.flash.devdigest.domain.repository.NewsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class NewsRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val api: NewsApi,
    private val newsDao: NewsDao,
    private val ioDispatcher: CoroutineDispatcher
) : NewsRepository {
    @OptIn(ExperimentalPagingApi::class)
    override fun observePagedTrendingNews(): Flow<PagingData<News>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 20,
                prefetchDistance = 2,
                enablePlaceholders = false,
                maxSize = PagingConfig.MAX_SIZE_UNBOUNDED
            ),
            remoteMediator = NewsRemoteMediator(
                api = api,
                newsDao = newsDao,
                database = database
            ),
            pagingSourceFactory = { newsDao.pagingSource() }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }

    override fun observeFavoriteNews(): Flow<List<News>> {
        return newsDao.observeFavorites()
            .map { entities ->
                entities.map { it.toDomain() }
            }
    }

    override fun observeSearchNews(query: String): Flow<PagingData<News>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 20,
                prefetchDistance = 2,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                NewsSearchPagingSource(
                    api = api,
                    query = query,
                    newsDao = newsDao
                )
            }
        ).flow
    }

    override suspend fun toggleFavorite(news: News): DataResult<Unit> {
        return withContext(ioDispatcher) {
            try {
                val existing = newsDao.getNewsById(news.id)
                if (existing == null) {
                    newsDao.insert(news.toEntity())
                }
                newsDao.toggleFavorite(news.id)
                DataResult.Success(Unit)
            } catch (t: Throwable) {
                if (t is CancellationException) throw t
                DataResult.Error(NetworkErrorMapper.fromThrowable(t).toDomain())
            }

        }
    }
}

suspend fun NewsDao.getFavoriteIdsSet(): Set<Long> {
    return getFavoriteIds().toSet()
}

fun List<News>.applyFavorites(favoriteIds: Set<Long>): List<News> {
    return map { news ->
        news.copy(isFavorite = news.id in favoriteIds)
    }
}

suspend fun NewsDao.observeFavoriteNewsOnce(): List<NewsEntity> {
    return observeFavorites().first()
}


