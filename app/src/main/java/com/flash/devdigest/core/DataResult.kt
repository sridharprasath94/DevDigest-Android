package com.flash.devdigest.core

import com.flash.devdigest.domain.error.DomainError


sealed class DataResult<out T> {

    data class Success<T>(val data: T) : DataResult<T>()

    data class Error(val error: DomainError) : DataResult<Nothing>()
}