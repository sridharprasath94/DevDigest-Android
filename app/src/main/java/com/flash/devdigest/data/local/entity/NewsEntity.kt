package com.flash.devdigest.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "news")
data class NewsEntity(

    @PrimaryKey
    val id: String,

    val title: String,

    val url: String?,

    val author: String,

    val points: Int,

    val comments: Int,

    val createdAt: String,

    val isFavorite: Boolean
)