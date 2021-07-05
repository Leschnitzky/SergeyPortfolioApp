package com.example.sergeyportfolioapp.usermanagement.ui.extradetails.state

sealed class PhotoDetailsViewState {
    object InitState: PhotoDetailsViewState()
    object Idle : PhotoDetailsViewState()
    object Loading : PhotoDetailsViewState()
    object PictureIsFavorite : PhotoDetailsViewState()
}