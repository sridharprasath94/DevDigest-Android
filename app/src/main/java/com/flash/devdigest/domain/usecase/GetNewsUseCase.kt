package com.flash.devdigest.domain.usecase

import com.flash.devdigest.domain.repository.NewsRepository
import javax.inject.Inject

class GetNewsUseCase @Inject constructor(
    private val repository: NewsRepository
) {
    suspend operator fun invoke(id: Long) = repository.getNewsById(id = id)
}