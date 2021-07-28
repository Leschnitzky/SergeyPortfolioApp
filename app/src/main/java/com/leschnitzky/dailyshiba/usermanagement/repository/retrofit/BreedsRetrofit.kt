package com.leschnitzky.dailyshiba.usermanagement.repository.retrofit

import com.leschnitzky.dailyshiba.usermanagement.repository.retrofit.pojo.BreedResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Path

interface BreedsRetrofit {

    @GET("breeds/{breed}/image")
    suspend fun getPhotoByBreed(@Path("breed") breed: String) : BreedResponse
}