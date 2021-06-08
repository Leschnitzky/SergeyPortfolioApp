package com.example.sergeyportfolioapp.usermanagement.ui.login.viewstate

sealed class LoginViewState {

    object Idle : LoginViewState()
    object Loading : LoginViewState()
    data class LoggedIn(val name: String) : LoginViewState()
    data class Error(val error: String?) : LoginViewState()

}
