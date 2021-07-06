package com.example.sergeyportfolioapp

import com.facebook.AccessToken
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.auth.User


sealed class UserIntent {



    //Login
    data class Login(val email: String, val password: String) : UserIntent()
    object LogoutUser : UserIntent()
    data class ForgotPass(val email: String) : UserIntent()
    //Register
    data class Register(val name: String, val email: String, val password: String, val selected: Boolean) : UserIntent()
    object RegisterWithFacebook : UserIntent()
    object RegisterWithGoogle : UserIntent()

    //Set as Profile Picture

    data class SetProfilePicture(val url: String) : UserIntent()
    data class AddPictureFavorite(val picture: String) : UserIntent()
    data class RemovePictureFavorite(val picture: String): UserIntent()
    data class CheckPhotoInFavorites(val picture: String): UserIntent()
    data class SignInGoogle(val signedInAccountFromIntent: Task<GoogleSignInAccount>?) :UserIntent()
    data class FacebookSignIn(val accessToken: AccessToken?) : UserIntent()
    data class UpdateDisplayName(val editTextValue: String) : UserIntent()

    object UpdateFavoritesPage : UserIntent()


    object DisplayProfilePicture : UserIntent()
    object GetPhotos : UserIntent()
    object GetNewPhotos : UserIntent()
    object CheckLogin : UserIntent()
    object GetProfileData : UserIntent()

}