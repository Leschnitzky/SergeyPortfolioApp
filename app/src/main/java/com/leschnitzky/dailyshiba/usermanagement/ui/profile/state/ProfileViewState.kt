package com.leschnitzky.dailyshiba.usermanagement.ui.profile.state

import com.leschnitzky.dailyshiba.usermanagement.repository.firestore.model.UserForFirestore

sealed class ProfileViewState {
    data class LoadedData(val userData: UserForFirestore) : ProfileViewState()
    data class Error(val error: String) : ProfileViewState()

    object Idle : ProfileViewState()
    object Loading : ProfileViewState()
}