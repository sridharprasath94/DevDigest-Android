package com.flash.devdigest.data.error


import com.flash.devdigest.domain.error.DomainError
import retrofit2.HttpException
import java.io.IOException

object NetworkErrorMapper {
    fun fromThrowable(t: Throwable): NetworkError {
        return when (t) {
            is HttpException -> {
                when (t.code()) {
                    403 -> NetworkError.RateLimitExceeded
                    else -> NetworkError.InvalidStatusCode(t.code())
                }
            }

            is IOException -> NetworkError.Network(t)

            else -> NetworkError.Unknown(t)
        }
    }

    fun NetworkError.toDomain(): DomainError {
        return when (this) {
            is NetworkError.Network ->
                DomainError.NetworkUnavailable

            is NetworkError.RateLimitExceeded ->
                DomainError.RateLimitExceeded

            is NetworkError.InvalidStatusCode ->
                DomainError.InvalidRequest

            else ->
                DomainError.Unknown
        }
    }
}

