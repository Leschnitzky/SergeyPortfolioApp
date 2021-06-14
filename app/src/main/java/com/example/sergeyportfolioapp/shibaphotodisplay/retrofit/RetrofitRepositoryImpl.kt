package com.example.sergeyportfolioapp.shibaphotodisplay.retrofit

import android.util.Log
import retrofit2.await
import javax.inject.Inject


class RetrofitRepositoryImpl @Inject constructor(
    val shibaRetrofit: ShibaRetrofit
): RetrofitRepository {
    private val TAG = "RetrofitRepositoryImpl"
    override suspend fun get10Photos(): List<String> {
        shibaRetrofit.getPhotos()?.let {
            return if (it.isNullOrEmpty()){
                Log.d(TAG, "get10Photos: empty list")
                listOf()
            } else {
                Log.d(TAG, "get10Photos: got something: $it")

                it
            }
        }
        return listOf()
    }
}