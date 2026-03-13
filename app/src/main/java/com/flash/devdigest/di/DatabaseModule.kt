package com.flash.devdigest.di

import android.content.Context
import androidx.room.Room
import com.flash.devdigest.data.local.AppDatabase
import com.flash.devdigest.data.local.dao.NewsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "news_trending_db"
        ).build()

    @Provides
    fun provideRepoDao(db: AppDatabase): NewsDao =
        db.newsDao()
}