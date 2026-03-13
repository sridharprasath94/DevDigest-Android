package com.flash.devdigest.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.flash.devdigest.data.local.entity.NewsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsDao {

    @Query("SELECT * FROM news ORDER BY points DESC")
    fun observeTrending(): Flow<List<NewsEntity>>

    @Query("SELECT * FROM news WHERE isFavorite = 1")
    fun observeFavorites(): Flow<List<NewsEntity>>

    @Query("SELECT id FROM news WHERE isFavorite = 1")
    suspend fun getFavoriteIds(): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(news: List<NewsEntity>)

    @Query("UPDATE news SET isFavorite = NOT isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: Long)
}