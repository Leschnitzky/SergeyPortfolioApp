package com.example.sergeyportfolioapp.shibaphotodisplay.ui.main.state

sealed class ShibaViewState {
    object Idle : ShibaViewState()
    data class GotPhotos(val list: List<String>) : ShibaViewState()
    data class Error(val error: String) : ShibaViewState()
    object Loading : ShibaViewState()
}