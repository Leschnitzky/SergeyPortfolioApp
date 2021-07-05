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
    suspend fun getCurrentUserFavorites(): List<String>
    suspend fun updateCurrentUserPhotos(list: ArrayList<String>, originalUrlList: List<String>)
    suspend fun updateCurrentUserProfilePicture(profilePic : String)

    suspend fun getCurrentUserTitleState() : Pair<String, String>
    suspend fun getCurrentUserURLMap() : Map<String,String>
    suspend fun getNewPhotosFromServer(): ArrayList<String>
    suspend fun addPictureToFavorites(picture: String)
    suspend fun removePictureFromFavorites(picture: String)
    suspend fun isPhotoInCurrentUserFavorites(picture: String): Boolean
}