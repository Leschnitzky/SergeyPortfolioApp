package com.example.sergeyportfolioapp.shibaphotodisplay.di

import com.example.sergeyportfolioapp.shibaphotodisplay.repository.ShibaRepository
import com.example.sergeyportfolioapp.shibaphotodisplay.repository.ShibaRepositoryImpl
import com.example.sergeyportfolioapp.shibaphotodisplay.retrofit.RetrofitRepository
import com.example.sergeyportfolioapp.shibaphotodisplay.retrofit.RetrofitRepositoryImpl
import com.example.sergeyportfolioapp.shibaphotodisplay.retrofit.ShibaRetrofit
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


@Module
@InstallIn(SingletonComponent::class)
internal object ShibaPhotoDisplayModule {

    @Provides
    fun provideRetrofitRepository() : RetrofitRepository{
        return RetrofitRepositoryImpl(provideShibaRetrofit())
    }

    @Provides
    fun provideShibaRepository() : ShibaRepository{
        return ShibaRepositoryImpl(provideRetrofitRepository())
    }

    @Provides
    fun provideShibaRetrofit() : ShibaRetrofit{
        val retrofit = Retrofit.Builder()
            .baseUrl("http://shibe.online/api/")
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build()

        return retrofit.create(ShibaRetrofit::class.java)
    }





}