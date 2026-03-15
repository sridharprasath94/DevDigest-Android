package com.flash.devdigest.data.repository

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.paging.map
import androidx.room.RoomDatabase
import androidx.room.withTransaction
import com.flash.devdigest.core.Result
import com.flash.devdigest.data.error.NetworkErrorMapper
import com.flash.devdigest.data.error.NetworkErrorMapper.toDomain
import com.flash.devdigest.data.local.AppDatabase
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
    private val database: AppDatabase,
    private val api: NewsApi,
    private val newsDao: NewsDao,
    private val ioDispatcher: CoroutineDispatcher
) : NewsRepository {
    private val refreshMutex = Mutex()


    @OptIn(ExperimentalPagingApi::class)
    override fun observePagedTrendingNews(): Flow<PagingData<News>> {
        return Pager(
            config = PagingConfig(
                pageSize = 30,
                prefetchDistance = 10,
                enablePlaceholders = false
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
                        .let { newsDao.insertAllNews(it) }

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


@OptIn(ExperimentalPagingApi::class)
class NewsRemoteMediator(
    private val api: NewsApi,
    private val newsDao: NewsDao,
    private val database: RoomDatabase
) : RemoteMediator<Int, NewsEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, NewsEntity>
    ): MediatorResult {

        val page = when (loadType) {
            LoadType.REFRESH -> 1
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {

                val lastItem = state.lastItemOrNull()
                    ?: return MediatorResult.Success(endOfPaginationReached = true)

                val nextPage = (state.pages.sumOf { it.data.size } / state.config.pageSize) + 1
                nextPage
            }
        }

        return try {
            Log.d("NewsAdapter", "Page: ${page}, Page count ${state.pages.size}")
            val response = api.getFrontPage(page = page, hitsPerPage = state.config.pageSize)

            // Preserve existing favorites before refresh
            val favoriteIds = newsDao.getFavoriteIdsSet()
            val favoriteRepos = newsDao.observeFavoriteNewsOnce()

            database.withTransaction {

                if (loadType == LoadType.REFRESH) {
                    newsDao.clearNews()
                }

                val entities = response
                    .toDomainList()
                    .applyFavorites(favoriteIds)
                    .map { it.toEntity() }

                newsDao.insertAllNews(entities)

                // Only restore favorites during REFRESH to avoid invalidating Paging repeatedly
                if (loadType == LoadType.REFRESH) {
                    favoriteRepos
                        .filter { fav -> entities.none { it.id == fav.id } }
                        .let { newsDao.insertAllNews(it) }
                }
            }

            MediatorResult.Success(
                endOfPaginationReached = response.toDomainList().isEmpty()
            )

        } catch (t: Throwable) {
            MediatorResult.Error(t)
        }
    }
}
