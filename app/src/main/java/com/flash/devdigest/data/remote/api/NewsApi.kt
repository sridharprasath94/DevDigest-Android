package com.flash.devdigest.data.remote.api

import com.flash.devdigest.data.remote.dto.NewsResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApi {

    @GET("search")
    suspend fun getFrontPage(
        @Query("tags") tags: String = "front_page"
    ): NewsResponseDto
}