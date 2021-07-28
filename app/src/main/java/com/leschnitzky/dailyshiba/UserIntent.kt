package com.leschnitzky.dailyshiba

import com.facebook.AccessToken
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task


sealed class UserIntent {



    data class Login(val email: String, val password: String) : UserIntent()
    object LogoutUser : UserIntent()

    data class Register(val name: String, val email: String, val password: String, val selected: Boolean) : UserIntent()


    data class SetProfilePicture(val url: String) : UserIntent()
    data class AddPictureFavorite(val picture: String) : UserIntent()
    data class RemovePictureFavorite(val picture: String): UserIntent()
    data class CheckPhotoInFavorites(val picture: String): UserIntent()
    data class SignInGoogle(val signedInAccountFromIntent: Task<GoogleSignInAccount>?) :UserIntent()
    data class FacebookSignIn(val accessToken: AccessToken?) : UserIntent()
    data class UpdateDisplayName(val editTextValue: String) : UserIntent()
    data class SendResetPassEmail(val email: String) : UserIntent()
    data class UpdateUserSettings(val setting: String, val field: String, val value: String) : UserIntent() {

    }

    object UpdateFavoritesPage : UserIntent()


    object GetPhotos : UserIntent()
    object GetNewPhotos : UserIntent()
    object CheckLogin : UserIntent()
    object GetProfileData : UserIntent()

}