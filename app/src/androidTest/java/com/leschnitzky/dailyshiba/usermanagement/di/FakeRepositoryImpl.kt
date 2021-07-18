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
        if((email.equals("test@gmail.com")) && (password.equals("test123"))){
            return "Serg"
        }
        return "Error"
    }

    override suspend fun createUserInFirestore(email: String, name: String) {
        return;
    }

    override fun logoutUser() {
        return;
    }

    override fun getCurrentUserEmail(): String? {
        return "test@gmail.com"
    }

    override suspend fun getCurrentUserDisplayName(): String? {
        return "Serg"
    }

    override suspend fun getCurrentUserPhotos(): ArrayList<String> {
        return arrayListOf(
            "http://cdn.shibe.online/shibes/09db03fb29ced77549f07f0de875f3d309f38361.jpg",
            "http://cdn.shibe.online/shibes/3d592b4309dc9e208e2e47e9dec668ade61e2ebe.jpg",
            "http://cdn.shibe.online/shibes/737fd8d2817a9bde97c93cf04f18aae6ee0effab.jpg",
            "http://cdn.shibe.online/shibes/725fdc830f817bb55e49d7e260ed4b752525e4e8.jpg",
            "http://cdn.shibe.online/shibes/fcc1f9bcc9848555d945cc09677ed2622b7120e5.jpg",
            "http://cdn.shibe.online/shibes/dc8c0bda4916f7a739d19715e9d6d447f6eba52d.jpg",
            "http://cdn.shibe.online/shibes/1eb4af9c79fa11197e9fe6f9abc875eacfb131fc.jpg",
            "http://cdn.shibe.online/shibes/5a317041238e15bf458b7360f57de79d857f1ed3.jpg",
            "http://cdn.shibe.online/shibes/9fa96c84c20910c605e0dd63edb698dc128e1d0f.jpg",
            "http://cdn.shibe.online/shibes/669c555fa863b5c5b0a398979a9f1a22c2cc081c.jpg"
        )
    }

    override suspend fun getCurrentUserFavorites(): List<String> {
        return arrayListOf(
            "http://cdn.shibe.online/shibes/09db03fb29ced77549f07f0de875f3d309f38361.jpg",
            "http://cdn.shibe.online/shibes/3d592b4309dc9e208e2e47e9dec668ade61e2ebe.jpg",
            "http://cdn.shibe.online/shibes/737fd8d2817a9bde97c93cf04f18aae6ee0effab.jpg",
            "http://cdn.shibe.online/shibes/725fdc830f817bb55e49d7e260ed4b752525e4e8.jpg",
            "http://cdn.shibe.online/shibes/fcc1f9bcc9848555d945cc09677ed2622b7120e5.jpg",
            "http://cdn.shibe.online/shibes/dc8c0bda4916f7a739d19715e9d6d447f6eba52d.jpg",
            "http://cdn.shibe.online/shibes/1eb4af9c79fa11197e9fe6f9abc875eacfb131fc.jpg",
            "http://cdn.shibe.online/shibes/5a317041238e15bf458b7360f57de79d857f1ed3.jpg",
            "http://cdn.shibe.online/shibes/9fa96c84c20910c605e0dd63edb698dc128e1d0f.jpg",
            "http://cdn.shibe.online/shibes/669c555fa863b5c5b0a398979a9f1a22c2cc081c.jpg"
        )
    }

    override suspend fun updateCurrentUserPhotos(
        list: ArrayList<String>,
        originalUrlList: List<String>
    ) {
        return
    }

    override suspend fun updateCurrentUserProfilePicture(profilePic: String) {
        return
    }

    override suspend fun getCurrentUserTitleState(): Pair<String, String> {
        return Pair("Serg","http://cdn.shibe.online/shibes/09db03fb29ced77549f07f0de875f3d309f38361.jpg")
    }

    override suspend fun getCurrentUserURLMap(): Map<String, String> {
        return mapOf()
    }

    override suspend fun getNewPhotosFromServer(): ArrayList<String> {
        return arrayListOf(
            "http://cdn.shibe.online/shibes/09db03fb29ced77549f07f0de875f3d309f38361.jpg",
            "http://cdn.shibe.online/shibes/3d592b4309dc9e208e2e47e9dec668ade61e2ebe.jpg",
            "http://cdn.shibe.online/shibes/737fd8d2817a9bde97c93cf04f18aae6ee0effab.jpg",
            "http://cdn.shibe.online/shibes/725fdc830f817bb55e49d7e260ed4b752525e4e8.jpg",
            "http://cdn.shibe.online/shibes/fcc1f9bcc9848555d945cc09677ed2622b7120e5.jpg",
            "http://cdn.shibe.online/shibes/dc8c0bda4916f7a739d19715e9d6d447f6eba52d.jpg",
            "http://cdn.shibe.online/shibes/1eb4af9c79fa11197e9fe6f9abc875eacfb131fc.jpg",
            "http://cdn.shibe.online/shibes/5a317041238e15bf458b7360f57de79d857f1ed3.jpg",
            "http://cdn.shibe.online/shibes/9fa96c84c20910c605e0dd63edb698dc128e1d0f.jpg",
            "http://cdn.shibe.online/shibes/669c555fa863b5c5b0a398979a9f1a22c2cc081c.jpg"
        )
    }

    override suspend fun addPictureToFavorites(picture: String) {
        return
    }

    override suspend fun removePictureFromFavorites(picture: String) {
        return
    }

    override suspend fun isPhotoInCurrentUserFavorites(picture: String): Boolean {
        return true
    }

    override suspend fun signInAccountWithGoogle(signedInAccountFromIntent: Task<GoogleSignInAccount>?) {
        return
    }

    override suspend fun doesUserExistInFirestore(currentUserEmail: String): Boolean {
        return true
    }

    override fun getAuthDisplayName(): String {
        return "Serg"
    }

    override suspend fun signInAccountWithFacebook(token: AccessToken?) {
        return
    }

    override suspend fun getCurrentUserData(): UserForFirestore {
        return UserForFirestore(
            "test@gmail.com",
            "Serg"
        )
    }

    override suspend fun updateCurrentUserDisplayName(displayName: String) {
        return
    }

    override suspend fun createUserInDB(currentUserEmail: String, authDisplayName: String) {
        return
    }

}
