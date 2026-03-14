package com.flash.devdigest.domain.usecase

import com.flash.devdigest.core.Result
import com.flash.devdigest.domain.model.News
import com.flash.devdigest.domain.repository.NewsRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val repository: NewsRepository
) {
    suspend operator fun invoke(news: News): Result<Unit> {
        return repository.toggleFavorite(news)
    }
}