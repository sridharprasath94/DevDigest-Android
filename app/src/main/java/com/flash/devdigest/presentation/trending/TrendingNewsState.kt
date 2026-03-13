package com.flash.devdigest.presentation.trending

import com.flash.devdigest.domain.model.News
import com.flash.devdigest.domain.error.DomainError

 data class TrendingNewsState(
     val isLoading: Boolean = false,
     val news: List<News> = emptyList(),
     val error: UiError? = null
 )

sealed class UiError {

    object NetworkUnavailable : UiError()

    object RateLimitExceeded : UiError()

    object InvalidRequest : UiError()

    object Unknown : UiError()

    companion object {
        fun fromDomain(error: DomainError): UiError = when (error) {
            DomainError.NetworkUnavailable -> NetworkUnavailable
            DomainError.RateLimitExceeded -> RateLimitExceeded
            DomainError.InvalidRequest -> InvalidRequest
            DomainError.Unknown -> Unknown
        }
    }
}