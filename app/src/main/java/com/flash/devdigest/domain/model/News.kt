package com.flash.devdigest.domain.model

data class News(
    val id: Long,
    val title: String,
    val url: String?,
    val author: String,
    val points: Int,
    val comments: Int,
    val createdAt: String,
    val isFavorite: Boolean
)