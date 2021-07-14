package com.leschnitzky.dailyshiba.usermanagement.repository.retrofit

interface RetrofitRepository {
    suspend fun get10Photos() : List<String>
}