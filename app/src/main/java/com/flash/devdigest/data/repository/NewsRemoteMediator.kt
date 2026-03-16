package com.flash.devdigest.data.repository

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.recyclerview.widget.RecyclerView
import androidx.room.withTransaction
import com.flash.devdigest.data.local.AppDatabase
import com.flash.devdigest.data.local.dao.NewsDao
import com.flash.devdigest.data.local.entity.NewsEntity
import com.flash.devdigest.data.local.mapper.toEntity
import com.flash.devdigest.data.remote.api.NewsApi
import com.flash.devdigest.data.remote.dto.toDomainList

@OptIn(ExperimentalPagingApi::class)
class NewsRemoteMediator(
    private val api: NewsApi,
    private val newsDao: NewsDao,
    private val database: AppDatabase
) : RemoteMediator<Int, NewsEntity>() {

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }


    private var currentPage: Int = 0

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, NewsEntity>
    ): MediatorResult {

        val page = when (loadType) {
            LoadType.REFRESH -> {
                currentPage = 0
                0
            }

            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)

            LoadType.APPEND -> {
                val next = currentPage + 1
                currentPage = next
                next
            }
        }
        Log.d("NewsPaging", "LoadType=$loadType | page=$page | mediatorPage=$currentPage")

        return try {
            val response = api.getLatestStories(page = page)
            Log.d(
                "NewsPaging",
                "Page: $page | hits=${response.hits.size} | nbPages=${response.nbPages}"
            )
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

            val endReached = page >= response.nbPages - 1
            Log.d(
                "NewsPaging",
                "EndReached=$endReached | nextPage=${if (endReached) "none" else page + 1}"
            )
            MediatorResult.Success(
                endOfPaginationReached = endReached
            )

        } catch (t: Throwable) {
            MediatorResult.Error(t)
        }
    }
}
