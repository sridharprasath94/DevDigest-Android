package com.flash.devdigest.domain.usecase

import com.flash.devdigest.domain.model.News
import com.flash.devdigest.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow

class GetTrendingNewsUseCase(
    private val repository: NewsRepository
) {

    operator fun invoke(): Flow<List<News>> {
        return repository.observeTrending()
    }
}