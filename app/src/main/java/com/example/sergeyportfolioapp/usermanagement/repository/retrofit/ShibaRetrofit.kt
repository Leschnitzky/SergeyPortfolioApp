package com.example.sergeyportfolioapp.usermanagement.repository.retrofit

import retrofit2.http.GET


interface ShibaRetrofit {
    @GET("shibes?count=10&urls=true&httpsUrls=false")
    suspend fun getPhotos(): List<String>?

}