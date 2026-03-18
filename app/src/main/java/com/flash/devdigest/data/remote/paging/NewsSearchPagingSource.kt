package com.flash.devdigest.data.remote.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.flash.devdigest.data.local.dao.NewsDao
import com.flash.devdigest.data.remote.api.NewsApi
import com.flash.devdigest.data.remote.dto.toDomainList
import com.flash.devdigest.data.repository.applyFavorites
import com.flash.devdigest.data.repository.getFavoriteIdsSet
import com.flash.devdigest.domain.model.News

class NewsSearchPagingSource(
    private val api: NewsApi,
    private val query: String,
    private val newsDao: NewsDao
) : PagingSource<Int, News>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, News> {
        val page = params.key ?: 0

        return try {
            val response = api.searchNews(query = query, page = page)

            val favoriteIds = newsDao.getFavoriteIdsSet()

            val news = response
                .toDomainList()
                .applyFavorites(favoriteIds)


            Log.d("NewsPager", "Paging Page number ${page} and news count ${news.count()}")

            LoadResult.Page(
                data = news,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (news.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, News>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}