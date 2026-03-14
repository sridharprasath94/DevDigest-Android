package com.flash.devdigest.domain.usecase

import com.flash.devdigest.domain.repository.NewsRepository
import javax.inject.Inject

class ObserveFavoriteNewsUseCase @Inject constructor(
    private val newsRepository: NewsRepository
) {
    operator fun invoke() = newsRepository.observeFavoriteNews()
}