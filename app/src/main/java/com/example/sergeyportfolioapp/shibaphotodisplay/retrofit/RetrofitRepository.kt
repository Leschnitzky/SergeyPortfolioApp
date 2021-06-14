package com.example.sergeyportfolioapp.shibaphotodisplay.retrofit

interface RetrofitRepository {
    suspend fun get10Photos() : List<String>
}