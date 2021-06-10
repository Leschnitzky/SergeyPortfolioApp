package com.example.sergeyportfolioapp.usermanagement.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sergeyportfolioapp.R
import com.example.sergeyportfolioapp.usermanagement.repository.Repository
import com.example.sergeyportfolioapp.usermanagement.ui.login.viewstate.LoginViewState
import com.example.sergeyportfolioapp.usermanagement.ui.register.viewstate.RegisterViewState
import com.example.sergeyportfolioapp.utils.isValidEmail
import com.google.firebase.auth.FirebaseAuthException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.log

@HiltViewModel
@ExperimentalCoroutinesApi
class UserViewModel @Inject constructor(
    var repo: Repository,
    var resourcesProvider: ResourcesProvider
): ViewModel() {
    private val TAG = "UserViewModel"
    val userIntent = Channel<UserIntent>(Channel.UNLIMITED)

    private val _stateLoginPage = MutableStateFlow<LoginViewState>(LoginViewState.Idle)
    private val _stateRegisterPage = MutableStateFlow<RegisterViewState>(RegisterViewState.Idle)
    private var _userTitle = MutableStateFlow<String>(resourcesProvider.getString(R.string.guest_string))

    val userTitle: StateFlow<String>
        get() = _userTitle
    val stateLoginPage: StateFlow<LoginViewState>
        get() = _stateLoginPage
    val stateRegisterPage: StateFlow<RegisterViewState>
        get() = _stateRegisterPage

    init {
        handleIntent()
    }

    private fun handleIntent() {
        viewModelScope.launch {
            userIntent.consumeAsFlow().collect {
                when (it) {
                    is UserIntent.Login -> logUser(it.email, it.password)
                    is UserIntent.Register -> registerUser(it.name,it.email,it.password,it.selected)
                }
            }

        }
    }

    private fun logUser(email: String, password: String) {
        viewModelScope.launch {
            _stateLoginPage.value = LoginViewState.Loading
            _stateLoginPage.value = try {
                if(!email.isEmpty() && !password.isEmpty()){
                    if(isValidEmail(email)){
                        LoginViewState.LoggedIn(
                            repo.loginUserAndReturnName(email,password).also {
                                withContext(Dispatchers.Default){
                                    Log.d(TAG, "Got user display name: $it")
                                    _userTitle.value = it
                                }
                            }
                        )
                    } else {
                        LoginViewState.Error("Email is invalid", LoginViewState.LoginErrorCode.INVALID_EMAIL)
                    }
                }else {

                    if(password.isEmpty()){
                        LoginViewState.Error("The following field is empty", LoginViewState.LoginErrorCode.EMPTY_PASSWORD)
                    } else {
                        LoginViewState.Error("The following field is empty", LoginViewState.LoginErrorCode.EMPTY_EMAIL)
                    }
                }
             } catch (e: FirebaseAuthException) {
                Log.d(TAG, "logUser: caught Error ${e.javaClass}")
                LoginViewState.Error(e.localizedMessage, LoginViewState.LoginErrorCode.FIREBASE_ERROR)
            }
        }
    }
    private fun registerUser(name: String, email: String, password: String, selected: Boolean){
        Log.d(TAG, "registerUser: $name , $email, $password, $selected")
        viewModelScope.launch {
            _stateRegisterPage.value = RegisterViewState.Loading
            _stateRegisterPage.value = try {
                if(email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty()){
                    if(isValidEmail(email)){
                        if(selected){
                            RegisterViewState.Registered(
                                repo.createUser(email,password,name).also {
                                    _userTitle.value = it
                                })
                        } else {
                            RegisterViewState.Error("Terms and conditions are not checked" ,
                                RegisterViewState.RegisterErrorCode.DIDNT_ACCEPT_TERMS)
                        }
                    } else {
                        RegisterViewState.Error("Email is invalid", RegisterViewState.RegisterErrorCode.INVALID_EMAIL)
                    }
                }else {
                    if(email.isEmpty()){
                        RegisterViewState.Error("The following field is empty", RegisterViewState.RegisterErrorCode.EMPTY_EMAIL)
                    } else if(password.isEmpty()){
                        RegisterViewState.Error("The following field is empty", RegisterViewState.RegisterErrorCode.EMPTY_PASSWORD)
                    } else {
                        RegisterViewState.Error("The following field is empty", RegisterViewState.RegisterErrorCode.EMPTY_NAME)
                    }
                }
            } catch (e: FirebaseAuthException) {
                Log.d(TAG, "logUser: caught Error ${e.javaClass}")
                RegisterViewState.Error(e.localizedMessage, RegisterViewState.RegisterErrorCode.FIREBASE_ERROR)
            }
        }
    }

}