package com.example.sergeyportfolioapp.usermanagement.ui.login.viewstate

sealed class LoginViewState {

    enum class LoginErrorCode(val value: Int){
        INVALID_EMAIL(0),
        EMPTY_PASSWORD(1),
        EMPTY_EMAIL(2),
        FIREBASE_ERROR(3)

    }

    object Idle : LoginViewState()
    object Loading : LoginViewState()
    data class LoggedIn(val name: String) : LoginViewState()
    data class Error(val error: String?, val error_code: LoginErrorCode) : LoginViewState()

}
