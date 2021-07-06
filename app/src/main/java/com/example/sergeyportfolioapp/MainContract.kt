package com.example.sergeyportfolioapp

import com.example.sergeyportfolioapp.utils.UiEvent
import com.example.sergeyportfolioapp.utils.UiState

class MainContract {

    sealed class Event: UiEvent {
        object OnLogout : Event()
    }

    data class State(
        val titleState: UserTitleState,
        val profilePicState: UserProfilePicState
    ): UiState

    sealed class UserTitleState{
        object InitState : UserTitleState()
        object Guest : UserTitleState()
        data class Member(val name: String): UserTitleState()
        data class MemberNoNavigate(val displayName: String) : MainContract.UserTitleState()
    }

    sealed class UserProfilePicState {
        data class NewProfilePic(val picture: String) : UserProfilePicState()
        object DefaultPicture : UserProfilePicState()
        object InitState : UserProfilePicState()
    }
}