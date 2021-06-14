package com.example.sergeyportfolioapp.usermanagement.repository

interface Repository {
    suspend fun loginUserAndReturnName(email: String, password: String) : String
    suspend fun createUser(email: String, password: String, name: String): String
    fun logoutUser()
    fun getCurrentUserEmail() : String?
    suspend fun getCurrentUserDisplayName() : String?
}