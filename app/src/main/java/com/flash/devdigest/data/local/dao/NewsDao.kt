package com.flash.devdigest.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.flash.devdigest.data.local.entity.NewsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsDao {
    @Query(
        """
SELECT * FROM news
ORDER BY isFavorite DESC, createdAt DESC
"""
    )
    fun pagingSource(): PagingSource<Int, NewsEntity>

    @Query("SELECT COUNT(*) FROM news")
    suspend fun getCount(): Int

    @Query("SELECT * FROM news ORDER BY createdAt DESC")
    fun observeTrending(): Flow<List<NewsEntity>>

    @Query("SELECT * FROM news WHERE id = :id LIMIT 1")
    suspend fun getNewsById(id: Long): NewsEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllNews(news: List<NewsEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(news: NewsEntity)

    @Query("DELETE FROM news WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM news")
    suspend fun clearNews()

    @Query("SELECT id FROM news WHERE isFavorite = 1")
    suspend fun getFavoriteIds(): List<Long>

    @Query("SELECT * FROM news WHERE isFavorite = 1")
    fun observeFavorites(): Flow<List<NewsEntity>>

    @Query("UPDATE news SET isFavorite = NOT isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: Long)
}