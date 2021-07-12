package com.example.sergeyportfolioapp.usermanagement.repository

import com.example.sergeyportfolioapp.usermanagement.repository.firestore.model.UserForFirestore
import com.facebook.AccessToken
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.flow.Flow

interface Repository {

    suspend fun loginUserAndReturnName(email: String, password: String) : String
    suspend fun createUser(email: String, password: String, name: String): String
    suspend fun createUserInFirestore(email: String, name: String)
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
    suspend fun signInAccountWithGoogle(signedInAccountFromIntent: Task<GoogleSignInAccount>?)
    suspend fun doesUserExistInFirestore(currentUserEmail: String) : Boolean
    fun getAuthDisplayName(): String
    suspend fun signInAccountWithFacebook(token: AccessToken?)
    suspend fun getCurrentUserData(): UserForFirestore
    suspend fun updateCurrentUserDisplayName(displayName: String)
    suspend fun createUserInDB(currentUserEmail: String, authDisplayName: String)
}