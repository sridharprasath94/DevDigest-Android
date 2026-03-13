package com.flash.devdigest.domain.usecase

import com.flash.devdigest.core.Result
import com.flash.devdigest.domain.repository.NewsRepository
import javax.inject.Inject

class RefreshTrendingNewsUseCase @Inject constructor(
    private val repository: NewsRepository
) {

    suspend operator fun invoke(): Result<Unit> {
        return repository.refreshTrendingNews()
    }
}