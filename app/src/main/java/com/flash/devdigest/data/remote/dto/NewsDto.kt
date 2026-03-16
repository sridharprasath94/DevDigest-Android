package com.flash.devdigest.data.remote.dto

import com.flash.devdigest.domain.model.News
import com.google.gson.annotations.SerializedName

data class NewsResponseDto(
    val hits: List<NewsDto>,
    val nbHits: Int,
    val nbPages: Int
)

data class NewsDto(
    val objectID: String,
    val title: String?,
    val url: String?,
    val author: String?,
    val points: Int?,
    @SerializedName("num_comments")  val numComments: Int?,
    @SerializedName("created_at")  val createdAt: String?
)


/**
 * DTO -> Domain mapping
 */
fun NewsDto.toDomain(isFavorite: Boolean = false): News {
    return News(
        id = objectID.toLong(),
        title = title ?: "No Title",
        url = url ?: "",
        author = author ?: "Unknown",
        points = points ?: 0,
        comments = numComments ?: 0,
        createdAt = createdAt ?: "",
        isFavorite = isFavorite
    )
}

fun NewsResponseDto.toDomainList(): List<News> = hits.map { it.toDomain(isFavorite = false) }