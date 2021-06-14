package com.example.sergeyportfolioapp.shibaphotodisplay.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sergeyportfolioapp.shibaphotodisplay.repository.ShibaRepository
import com.example.sergeyportfolioapp.shibaphotodisplay.ui.main.state.ShibaViewState
import com.example.sergeyportfolioapp.usermanagement.ui.UserIntent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject
import kotlin.math.log

@HiltViewModel
@ExperimentalCoroutinesApi
class PhotoViewModel @Inject constructor(
    val shibaRepository: ShibaRepository
) : ViewModel(){
    private val TAG = "PhotoViewModel"

    val userIntent = Channel<PhotoIntent>(Channel.UNLIMITED)

    private var _photoLoadState = MutableStateFlow<ShibaViewState>(ShibaViewState.Idle)

    val photoLoadState : StateFlow<ShibaViewState>
        get() = _photoLoadState


    init {
        handleIntents()
    }

    private fun handleIntents() {
            viewModelScope.launch {
                userIntent.consumeAsFlow().collect {
                    when (it) {
                        is PhotoIntent.MorePhotos -> getMorePhotos()
                    }
                }
            }
    }

    private fun getMorePhotos() {
        Log.d(TAG, "getMorePhotos: Start")
        viewModelScope.launch {
            _photoLoadState.value = ShibaViewState.Loading
            try {
                _photoLoadState.value = ShibaViewState.GotPhotos(
                    shibaRepository.getPhotos()
                )
            } catch ( e: Exception){
                Log.d(TAG, "getMorePhotos: ${e.message}")
                _photoLoadState.value = ShibaViewState.Error(e.message!!)
            }
        }
    }

}