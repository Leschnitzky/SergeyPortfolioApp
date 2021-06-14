package com.example.sergeyportfolioapp.shibaphotodisplay.repository

interface ShibaRepository {
    suspend fun getPhotos() : List<String>
}