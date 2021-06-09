package com.example.sergeyportfolioapp.usermanagement.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sergeyportfolioapp.R
import com.example.sergeyportfolioapp.usermanagement.repository.Repository
import com.example.sergeyportfolioapp.usermanagement.ui.login.intent.LoginIntent
import com.example.sergeyportfolioapp.usermanagement.ui.login.viewstate.LoginViewState
import com.example.sergeyportfolioapp.utils.isValidEmail
import com.google.firebase.auth.FirebaseAuthException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class UserViewModel @Inject constructor(
    var repo: Repository,
    var resourcesProvider: ResourcesProvider
): ViewModel() {
    private val TAG = "UserViewModel"
    val userIntent = Channel<LoginIntent>(Channel.UNLIMITED)
    private val _state = MutableStateFlow<LoginViewState>(LoginViewState.Idle)
    private var _userTitle = MutableStateFlow<String>(resourcesProvider.getString(R.string.guest_string))
    val userTitle: StateFlow<String>
        get() = _userTitle

    val state: StateFlow<LoginViewState>
        get() = _state

    init {
        handleIntent()
    }

    private fun handleIntent() {
        viewModelScope.launch {
            userIntent.consumeAsFlow().collect {
                when (it) {
                    is LoginIntent.Login -> logUser(it.email, it.password)

                }
            }
        }
    }

    private fun logUser(email: String, password: String) {
        viewModelScope.launch {
            _state.value = LoginViewState.Loading
            _state.value = try {
                if(!email.isEmpty() && !password.isEmpty()){
                    if(isValidEmail(email)){
                        LoginViewState.LoggedIn(repo.loginUserAndReturnName(email,password))
                    } else {
                        LoginViewState.Error("Email is invalid")
                    }
                }else {
                    LoginViewState.Error("One of the fields is empty")
                }
             } catch (e: FirebaseAuthException) {
                Log.d(TAG, "logUser: caught Error ${e.javaClass}")
                LoginViewState.Error(e.localizedMessage)
            }
        }

    }

}