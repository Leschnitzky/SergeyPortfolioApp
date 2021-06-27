package com.example.sergeyportfolioapp.usermanagement.repository.retrofit

interface RetrofitRepository {
    suspend fun get10Photos() : List<String>
}