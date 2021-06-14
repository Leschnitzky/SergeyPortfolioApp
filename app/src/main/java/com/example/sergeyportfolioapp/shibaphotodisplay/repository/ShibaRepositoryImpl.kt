package com.example.sergeyportfolioapp.shibaphotodisplay.repository

import com.example.sergeyportfolioapp.shibaphotodisplay.retrofit.RetrofitRepository
import javax.inject.Inject

class ShibaRepositoryImpl @Inject constructor(
    val retrofitRepository: RetrofitRepository
): ShibaRepository {
    override suspend fun getPhotos(): List<String> {
        return retrofitRepository.get10Photos()
    }
}