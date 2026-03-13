package com.flash.devdigest.data.local.mapper

import com.flash.devdigest.data.local.entity.NewsEntity
import com.flash.devdigest.domain.model.News

fun NewsEntity.toDomain(): News {
    return News(
        id = id,
        title = title,
        url = url,
        author = author,
        points = points,
        comments = comments,
        createdAt = createdAt,
        isFavorite = isFavorite
    )
}

fun News.toEntity(): NewsEntity {
    return NewsEntity(
        id = id,
        title = title,
        url = url,
        author = author,
        points = points,
        comments = comments,
        createdAt = createdAt,
        isFavorite = isFavorite
    )
}