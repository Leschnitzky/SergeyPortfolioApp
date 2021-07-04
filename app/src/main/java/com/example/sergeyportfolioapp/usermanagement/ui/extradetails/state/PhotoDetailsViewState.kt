package com.example.sergeyportfolioapp.usermanagement.ui.extradetails.state

sealed class PhotoDetailsViewState {
    object Idle : PhotoDetailsViewState()
    object Loading : PhotoDetailsViewState()
}