package com.flash.devdigest.domain.error

sealed class DomainError {
    object NewsNotFoundError : DomainError()

    object NetworkUnavailable : DomainError()

    object RateLimitExceeded : DomainError()

    object InvalidRequest : DomainError()

    object Unknown : DomainError()
}