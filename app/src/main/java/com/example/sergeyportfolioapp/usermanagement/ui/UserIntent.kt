package com.example.sergeyportfolioapp.usermanagement.ui

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
    object DisplayProfilePicture : UserIntent()
    object GetPhotos : UserIntent()
    object GetNewPhotos : UserIntent()
    object CheckLogin : UserIntent()

}