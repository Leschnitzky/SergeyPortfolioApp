package com.leschnitzky.dailyshiba.usermanagement.repository.retrofit

import retrofit2.http.GET
import retrofit2.http.Query


interface ShibaRetrofit {
    @GET("shibes?&urls=true&httpsUrls=false")
    suspend fun getPhotos(
        @Query("count") count : Int,
    ): List<String>

}