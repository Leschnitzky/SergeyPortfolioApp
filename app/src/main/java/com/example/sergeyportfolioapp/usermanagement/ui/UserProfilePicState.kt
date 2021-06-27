package com.example.sergeyportfolioapp.usermanagement.ui

sealed class UserProfilePicState {
    data class NewProfilePic(val picture: String) : UserProfilePicState()
    object DefaultPicture : UserProfilePicState()
    object InitState : UserProfilePicState()
}