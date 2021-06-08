package com.example.sergeyportfolioapp.usermanagement.ui.login.intent

sealed class LoginIntent {

    class Login(val email: String, val password: String) : LoginIntent()

}