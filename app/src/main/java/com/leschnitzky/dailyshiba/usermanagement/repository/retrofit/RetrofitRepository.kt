package com.leschnitzky.dailyshiba.usermanagement.repository.retrofit

import kotlinx.coroutines.CoroutineDispatcher

interface RetrofitRepository {
    suspend fun getShibaPhotos(count : Int) : List<String>
    suspend fun getCorgiPhotos(count : Int, dispatcher: CoroutineDispatcher) : List<String>
    suspend fun getHuskyPhotos(count : Int, dispatcher: CoroutineDispatcher) : List<String>
    suspend fun getBeaglePhotos(count : Int, dispatcher: CoroutineDispatcher) : List<String>
}