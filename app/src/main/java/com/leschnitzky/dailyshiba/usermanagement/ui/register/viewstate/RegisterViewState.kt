package com.leschnitzky.dailyshiba.usermanagement.ui.register.viewstate

sealed class RegisterViewState{

    enum class RegisterErrorCode(public val value: Int) {
        INVALID_EMAIL(0),
        EMPTY_EMAIL(1),
        EMPTY_PASSWORD(2),
        EMPTY_NAME(3),
        FIREBASE_ERROR(4),
        DIDNT_ACCEPT_TERMS(5)

    }

    object Idle : RegisterViewState()
    object Loading : RegisterViewState()
    object LoggedOut : RegisterViewState()
    data class Registered(val name: String) : RegisterViewState()
    data class Error(val error: String?, val error_code: RegisterErrorCode) : RegisterViewState()

}
