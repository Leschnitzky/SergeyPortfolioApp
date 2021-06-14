package com.example.sergeyportfolioapp.shibaphotodisplay.retrofit

import com.google.firebase.database.core.Repo
import retrofit2.Call

import retrofit2.http.GET


interface ShibaRetrofit {
    @GET("shibes?count=10&urls=true&httpsUrls=false")
    suspend fun getPhotos(): List<String>?

}