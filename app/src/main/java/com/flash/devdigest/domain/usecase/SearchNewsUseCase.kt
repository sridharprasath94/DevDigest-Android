package com.flash.devdigest.domain.usecase


import androidx.paging.PagingData
import com.flash.devdigest.domain.model.News
import com.flash.devdigest.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchNewsUseCase @Inject constructor(
    private val repository: NewsRepository
) {
    operator fun invoke(query: String): Flow<PagingData<News>> {
        return repository.observeSearchNews(query)
    }
}