package com.example.sergeyportfolioapp.usermanagement.ui

sealed class UserTitleState{
    object InitState : UserTitleState()
    object Guest : UserTitleState()
    data class Member(val name: String): UserTitleState()
}
