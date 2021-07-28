package com.leschnitzky.dailyshiba.usermanagement.repository.retrofit

interface RetrofitRepository {
    suspend fun getShibaPhotos(count : Int) : List<String>
    suspend fun getCorgiPhotos(count : Int) : List<String>
    suspend fun getHuskyPhotos(count : Int) : List<String>
    suspend fun getBeaglePhotos(count : Int) : List<String>
}