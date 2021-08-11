package com.leschnitzky.dailyshiba.usermanagement.repository.retrofit

import kotlinx.coroutines.CoroutineDispatcher
import kotlin.coroutines.CoroutineContext

interface RetrofitRepository {
    suspend fun getShibaPhotos(count : Int) : List<String>
    suspend fun getCorgiPhotos(count : Int, dispatcher: CoroutineContext) : List<String>
    suspend fun getHuskyPhotos(count : Int, dispatcher: CoroutineContext) : List<String>
    suspend fun getBeaglePhotos(count : Int, dispatcher: CoroutineContext) : List<String>
}