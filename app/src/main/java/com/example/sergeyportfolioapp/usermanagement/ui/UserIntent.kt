package com.example.sergeyportfolioapp.usermanagement.ui

sealed class UserIntent {
    //Login
    class Login(val email: String, val password: String) : UserIntent()
    class ForgotPass(val email: String) : UserIntent()

    //Register
    class Register(val name: String, val email: String, val password: String, val selected: Boolean) : UserIntent()
    class RegisterWithFacebook() : UserIntent()
    class RegisterWithGoogle() : UserIntent()

}