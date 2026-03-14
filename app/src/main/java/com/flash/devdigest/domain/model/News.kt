package com.flash.devdigest.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class News(
    val id: Long,
    val title: String,
    val url: String?,
    val author: String,
    val points: Int,
    val comments: Int,
    val createdAt: String,
    val isFavorite: Boolean
) : Parcelable