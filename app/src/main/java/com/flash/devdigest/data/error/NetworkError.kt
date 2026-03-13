package com.flash.devdigest.data.error

/**
 * Data-layer network errors (do NOT expose these outside data layer in final architecture).
 * We'll later map these to DomainError in domain layer when DomainError is added.
 */
sealed interface NetworkError {
    object InvalidResponse : NetworkError
    data class InvalidStatusCode(val code: Int) : NetworkError
    object RateLimitExceeded : NetworkError
    data class Network(val throwable: Throwable) : NetworkError
    data class Decoding(val throwable: Throwable) : NetworkError
    data class Unknown(val throwable: Throwable) : NetworkError

    fun toUserMessage(): String = when (this) {
        InvalidResponse -> "Invalid server response."
        is InvalidStatusCode -> "Server returned error code: $code."
        RateLimitExceeded -> "GitHub API rate limit exceeded. Please try again later."
        is Network -> throwable.localizedMessage ?: "Network error"
        is Decoding -> "Failed to decode server response."
        is Unknown -> throwable.localizedMessage ?: "Unknown error"
    }
}