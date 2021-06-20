package com.example.sergeyportfolioapp.shibaphotodisplay.ui

sealed class PhotoIntent {
    object MorePhotos : PhotoIntent()
    object SetupAsProfile : PhotoIntent()
    object AddToFavorites : PhotoIntent()

}
