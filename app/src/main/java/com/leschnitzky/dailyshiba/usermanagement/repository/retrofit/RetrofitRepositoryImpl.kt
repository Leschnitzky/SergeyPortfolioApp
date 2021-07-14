package com.leschnitzky.dailyshiba.usermanagement.repository.retrofit

import android.util.Log
import timber.log.Timber
import javax.inject.Inject


class RetrofitRepositoryImpl @Inject constructor(
    val shibaRetrofit: ShibaRetrofit
): RetrofitRepository {
    private val TAG = "RetrofitRepositoryImpl"
    override suspend fun get10Photos(): List<String> {
        shibaRetrofit.getPhotos()?.let {
            return if (it.isNullOrEmpty()){
                Timber.d( "get10Photos: empty list")
                listOf()
            } else {
                Timber.d( "get10Photos: got something: $it")
                it
            }
        }
        return listOf()
    }
}