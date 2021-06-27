package com.example.sergeyportfolioapp.usermanagement.repository

import com.example.sergeyportfolioapp.usermanagement.repository.firestore.model.UserForFirestore
import kotlinx.coroutines.flow.Flow

interface Repository {
    suspend fun loginUserAndReturnName(email: String, password: String) : String
    suspend fun createUser(email: String, password: String, name: String): String
    fun logoutUser()
    fun getCurrentUserEmail() : String?
    suspend fun getCurrentUserDisplayName() : String?
    suspend fun getCurrentUserPhotos(): ArrayList<String>
    suspend fun getCurrentUserFavorites(): Flow<UserForFirestore>
    suspend fun updateCurrentUserPhotos(list: ArrayList<String>, originalUrlList: List<String>)
    suspend fun updateCurrentUserProfilePicture(profilePic : String)
    suspend fun getCurrentUserProfilePic(): Flow<UserForFirestore>
    suspend fun getCurrentUserURLMap() : Map<String,String>

}