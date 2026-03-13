package com.flash.devdigest.domain.usecase

import com.flash.devdigest.domain.repository.NewsRepository

class RefreshTrendingNewsUseCase(
    private val repository: NewsRepository
) {

    suspend operator fun invoke() {
        return repository.refreshTrendingNews()
    }
}