package com.leschnitzky.dailyshiba.usermanagement.di

import com.leschnitzky.dailyshiba.usermanagement.repository.Repository
import com.leschnitzky.dailyshiba.usermanagement.repository.firestore.model.UserForFirestore
import com.facebook.AccessToken
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuthException

class FakeRepositoryImpl : Repository {
    override suspend fun loginUserAndReturnName(email: String, password: String): String {
        if((email.equals("test@gmail.com")) && (password.equals("test123"))){
            return "test@gmail.com"
        }
        throw FirebaseAuthException("no user","no_user")
    }

    override suspend fun createUser(email: String, password: String, name: String): String {
        TODO("Not yet implemented")
    }

    override suspend fun createUserInFirestore(email: String, name: String) {
        TODO("Not yet implemented")
    }

    override fun logoutUser() {
        TODO("Not yet implemented")
    }

    override fun getCurrentUserEmail(): String? {
        TODO("Not yet implemented")
    }

    override suspend fun getCurrentUserDisplayName(): String? {
        TODO("Not yet implemented")
    }

    override suspend fun getCurrentUserPhotos(): ArrayList<String> {
        TODO("Not yet implemented")
    }

    override suspend fun getCurrentUserFavorites(): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun updateCurrentUserPhotos(
        list: ArrayList<String>,
        originalUrlList: List<String>
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun updateCurrentUserProfilePicture(profilePic: String) {
        TODO("Not yet implemented")
    }

    override suspend fun getCurrentUserTitleState(): Pair<String, String> {
        TODO("Not yet implemented")
    }

    override suspend fun getCurrentUserURLMap(): Map<String, String> {
        TODO("Not yet implemented")
    }

    override suspend fun getNewPhotosFromServer(): ArrayList<String> {
        TODO("Not yet implemented")
    }

    override suspend fun addPictureToFavorites(picture: String) {
        TODO("Not yet implemented")
    }

    override suspend fun removePictureFromFavorites(picture: String) {
        TODO("Not yet implemented")
    }

    override suspend fun isPhotoInCurrentUserFavorites(picture: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun signInAccountWithGoogle(signedInAccountFromIntent: Task<GoogleSignInAccount>?) {
        TODO("Not yet implemented")
    }

    override suspend fun doesUserExistInFirestore(currentUserEmail: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun getAuthDisplayName(): String {
        TODO("Not yet implemented")
    }

    override suspend fun signInAccountWithFacebook(token: AccessToken?) {
        TODO("Not yet implemented")
    }

    override suspend fun getCurrentUserData(): UserForFirestore {
        TODO("Not yet implemented")
    }

    override suspend fun updateCurrentUserDisplayName(displayName: String) {
        TODO("Not yet implemented")
    }

}
