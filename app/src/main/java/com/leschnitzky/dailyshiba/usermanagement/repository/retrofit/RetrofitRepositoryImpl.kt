package com.leschnitzky.dailyshiba.usermanagement.repository.retrofit

import android.util.Log
import com.leschnitzky.dailyshiba.utils.API_KEY_DOG
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject


class RetrofitRepositoryImpl @Inject constructor(
    val shibaRetrofit: ShibaRetrofit,
    val breedsRetrofit: BreedsRetrofit
): RetrofitRepository {
    private val TAG = "RetrofitRepositoryImpl"
    override suspend fun getShibaPhotos(count : Int): List<String> {
        shibaRetrofit.getPhotos(
            count
        )?.let { it ->
            Timber.d("$it")
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

    override suspend fun getCorgiPhotos(count: Int): List<String> {
        return getBasePhotos(count,"corgi")
    }

    override suspend fun getHuskyPhotos(count: Int): List<String> {
        return getBasePhotos(count,"husky")
    }

    override suspend fun getBeaglePhotos(count: Int): List<String> {
        return getBasePhotos(count,"beagle")

    }

    private suspend fun getBasePhotos(count: Int, breed: String) : List<String>{
        return withContext(Dispatchers.IO){
            val list = arrayListOf<String>()
            (1..count).map {
                async(Dispatchers.IO) {
                    list.add(breedsRetrofit.getPhotoByBreed(breed).response.url)
                }
            }.awaitAll()
            list
        }
    }


}