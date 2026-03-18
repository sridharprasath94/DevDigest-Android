package com.flash.devdigest.presentation.error

import com.flash.devdigest.domain.error.DomainError

sealed class UIError(val message: String) {
    object NewsNotFoundError : UIError("News is not found")

    object NoInternet : UIError("No internet connection. Please check your network.")

    object RateLimited : UIError("Rate limit exceeded. Please try again later.")

    object BadRequest : UIError("Something went wrong with the request.")

    object Unknown : UIError("An unexpected error occurred. Please try again.")

    companion object {

        fun from(domainError: DomainError): UIError {
            return when (domainError) {
                DomainError.NewsNotFoundError -> NewsNotFoundError
                DomainError.NetworkUnavailable -> NoInternet
                DomainError.RateLimitExceeded -> RateLimited
                DomainError.InvalidRequest -> BadRequest
                DomainError.Unknown -> Unknown
            }
        }
    }
}