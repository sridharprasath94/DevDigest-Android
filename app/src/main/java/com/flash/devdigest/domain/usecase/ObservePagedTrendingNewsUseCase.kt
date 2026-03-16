package com.flash.devdigest.domain.usecase

import androidx.paging.PagingData
import com.flash.devdigest.domain.model.News
import com.flash.devdigest.domain.repository.NewsRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObservePagedTrendingNewsUseCase @Inject constructor(
    private val repository: NewsRepository
) {
    operator fun invoke(): Flow<PagingData<News>> {
        return repository.observePagedTrendingNews()
    }
}