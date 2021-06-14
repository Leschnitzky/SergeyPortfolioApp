package com.example.sergeyportfolioapp.usermanagement.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sergeyportfolioapp.usermanagement.repository.Repository
import com.example.sergeyportfolioapp.usermanagement.room.model.User
import com.example.sergeyportfolioapp.usermanagement.ui.login.viewstate.LoginViewState
import com.example.sergeyportfolioapp.usermanagement.ui.register.viewstate.RegisterViewState
import com.example.sergeyportfolioapp.utils.isValidEmail
import com.google.firebase.auth.FirebaseAuthException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import javax.inject.Inject


@HiltViewModel
@ExperimentalCoroutinesApi
class UserViewModel @Inject constructor(
    var repo: Repository,
    var resourcesProvider: ResourcesProvider
): ViewModel() {

    enum class FragmentDisplayNumber(val number: Int){
        LoginFragment(1),
        RegisterFragment(2)
    }

    private val TAG = "UserViewModel"

    val userIntent = Channel<UserIntent>(Channel.UNLIMITED)

    private val _stateLoginPage = MutableStateFlow<LoginViewState>(LoginViewState.Idle)
    private val _stateRegisterPage = MutableStateFlow<RegisterViewState>(RegisterViewState.Idle)

    private val _userTitle = MutableSharedFlow<UserTitleState>()

    val userTitle : SharedFlow<UserTitleState>
        get() = _userTitle

    val stateLoginPage: StateFlow<LoginViewState>
        get() = _stateLoginPage
    val stateRegisterPage: StateFlow<RegisterViewState>
        get() = _stateRegisterPage


    var currentFragmentNumber : Int = FragmentDisplayNumber.LoginFragment.number

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
                                _userTitle.emit(UserTitleState.Member(it))
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
                                        _userTitle.emit(UserTitleState.Member(it))
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

    fun logoutUser() {
        Log.d(TAG, "logoutUser: ")
                    repo.logoutUser()
        viewModelScope.launch{
            Log.d(TAG, "logoutUser: Sending Guest")
            _userTitle.emit(UserTitleState.Guest)
        }
    }

    override fun onCleared() {
        Log.d(TAG, "onCleared:")
        super.onCleared()
    }


    fun getCurrentUserEmail() : String{
        val email = repo.getCurrentUserEmail()
        if(email == null){
            return "Unsigned"
        } else {
            return email
        }
    }

    fun checkIfUserLoggedIn() {
        viewModelScope.launch {
           repo.getCurrentUserDisplayName().let {
                if(it == null){
                    _userTitle.emit(UserTitleState.Guest)
                } else {
                    _userTitle.emit(UserTitleState.Member(it))
                }
           }
        }

    }


}