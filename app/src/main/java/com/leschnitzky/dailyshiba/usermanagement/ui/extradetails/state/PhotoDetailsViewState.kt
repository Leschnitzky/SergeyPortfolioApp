package com.leschnitzky.dailyshiba.usermanagement.ui.extradetails.state

sealed class PhotoDetailsViewState {
    object InitState: PhotoDetailsViewState()
    object Idle : PhotoDetailsViewState()
    object Loading : PhotoDetailsViewState()
    object PictureIsFavorite : PhotoDetailsViewState()
}