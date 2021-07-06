package com.example.sergeyportfolioapp.usermanagement.ui.profile.state

import com.example.sergeyportfolioapp.usermanagement.repository.firestore.model.UserForFirestore

sealed class ProfileViewState {
    data class LoadedData(val userData: UserForFirestore) : ProfileViewState()

    object Idle : ProfileViewState()
    object Loading : ProfileViewState()
}