package com.example.sergeyportfolioapp.usermanagement.repository

interface Repository {
    suspend fun loginUserAndReturnName(email: String, password: String) : String
}