package com.example.sergeyportfolioapp.usermanagement.ui.favorites.state

sealed class ShibaFavoritesStateView {
    object InitState : ShibaFavoritesStateView()
    object Idle : ShibaFavoritesStateView()
    object Loading : ShibaFavoritesStateView()
    data class PhotosLoaded(val list : List<String>) : ShibaFavoritesStateView()
}
