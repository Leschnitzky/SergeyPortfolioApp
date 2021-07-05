package com.example.sergeyportfolioapp

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
    object UpdateFavoritesPage : UserIntent()


    object DisplayProfilePicture : UserIntent()
    object GetPhotos : UserIntent()
    object GetNewPhotos : UserIntent()
    object CheckLogin : UserIntent()

}