package com.example.sergeyportfolioapp.usermanagement.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sergeyportfolioapp.MainContract
import com.example.sergeyportfolioapp.UserIntent
import com.example.sergeyportfolioapp.usermanagement.ui.main.state.ShibaViewState
import com.example.sergeyportfolioapp.usermanagement.repository.Repository
import com.example.sergeyportfolioapp.usermanagement.ui.login.viewstate.LoginViewState
import com.example.sergeyportfolioapp.usermanagement.ui.register.viewstate.RegisterViewState
import com.example.sergeyportfolioapp.utils.isValidEmail
import com.google.firebase.auth.FirebaseAuthException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext


@HiltViewModel
@ExperimentalCoroutinesApi
class UserViewModel @Inject constructor(
    var repo: Repository,
    var resourcesProvider: ResourcesProvider
): ViewModel() {

    private val TAG = "UserViewModel"

   init {
       handleIntent()
   }

    val intentChannel = Channel<UserIntent>(Channel.UNLIMITED)



    private val _stateLoginPage = MutableStateFlow<LoginViewState>(LoginViewState.Idle)
    private val _stateRegisterPage = MutableStateFlow<RegisterViewState>(RegisterViewState.Idle)
    private val _stateShibaPage = MutableStateFlow<ShibaViewState>(ShibaViewState.Idle)

    val stateLoginPage: StateFlow<LoginViewState>
        get() = _stateLoginPage.asStateFlow()
    val stateRegisterPage: StateFlow<RegisterViewState>
        get() = _stateRegisterPage.asStateFlow()
    val stateShibaPage : StateFlow<ShibaViewState>
        get() = _stateShibaPage


    val mainActivityUIState : StateFlow<MainContract.State>
        get() = _mainActivityUIState
    private val _mainActivityUIState = MutableStateFlow(MainContract.State(
        MainContract.UserTitleState.InitState,
        MainContract.UserProfilePicState.InitState
    ))

    val currPhotosInDB : StateFlow<ShibaViewState>
        get() = _currPhotosInDB.asStateFlow()
    private val _currPhotosInDB = MutableStateFlow<ShibaViewState>(ShibaViewState.Idle)



    private fun handleIntent() = viewModelScope.launch {

            intentChannel.consumeAsFlow().collect {
                Log.d(TAG, "handleIntent: Got Intent $it")
                when (it) {
                    is UserIntent.Login -> logUser(it.email, it.password)
                    is UserIntent.Register -> registerUser(
                        it.name,
                        it.email,
                        it.password,
                        it.selected
                    )
                    is UserIntent.SetProfilePicture -> updateUserProfilePicture(it.url)
//                    is UserIntent.DisplayProfilePicture -> loadCurrentUserProfilePicture()
//                    is UserIntent.GetPhotos -> getPhotosForCurrentUser()
                    is UserIntent.CheckLogin -> checkUserStatus()
                    is UserIntent.LogoutUser -> logoutUser()
                }
            }
    }

    private suspend fun checkUserStatus() = withContext(viewModelScope.coroutineContext) {
            val email = getCurrentUserEmail()
            Log.d(TAG, "checkUserStatus: $email")
            if(email == "Unsigned"){
                emitGuestUser()
            } else {
                repo.getCurrentUserTitleState().collectLatest {
                        pair ->
                    Log.d(TAG, "checkUserStatus: $pair")
                        _mainActivityUIState.value =
                            MainContract.State(
                                MainContract.UserTitleState.Member(pair.first),
                                MainContract.UserProfilePicState.NewProfilePic(pair.second)
                            )

                }
            }
    }


    private suspend fun emitGuestUser(){
        withContext(viewModelScope.coroutineContext){
            _mainActivityUIState.value = (
                MainContract.State(
                    MainContract.UserTitleState.Guest,
                    MainContract.UserProfilePicState.DefaultPicture
                )
            )
            Log.d(TAG, "checkUserStatus: Emitted Guest on ${this.coroutineContext}")
        }
    }

//    private fun getPhotosForCurrentUser() {
//        _stateShibaPage.value = ShibaViewState.Loading
//        GlobalScope.launch {
//            repo.getCurrentUserPhotos().let {
//                _stateShibaPage.value = ShibaViewState.GotPhotos(it)
//            }
//        }
//    }

    private fun logUser(email: String, password: String) {
        viewModelScope.launch  {
            _stateLoginPage.value = LoginViewState.Loading
            Log.d(TAG, "logUser: HERE 1")
            _stateLoginPage.value = try {
                if(!email.isEmpty() && !password.isEmpty()){
                    if(isValidEmail(email)){
                        LoginViewState.LoggedIn(
                            repo.loginUserAndReturnName(email,password).also {
                                Log.d(TAG, "logUser: HERE 2")
                            }.also {
                                repo.getCurrentUserTitleState().collect {
                                    state ->
                                    Log.d(TAG, "logUser: HERE 3")
                                    _mainActivityUIState.value =
                                        MainContract.State(
                                            MainContract.UserTitleState.Member(state.first),
                                            MainContract.UserProfilePicState.NewProfilePic(state.second)
                                    )
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
        viewModelScope.launch  {
            _stateRegisterPage.value = RegisterViewState.Loading
            _stateRegisterPage.value = try {
                if(email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty()){
                    if(isValidEmail(email)){
                        if(selected){
                            RegisterViewState.Registered(
                                repo.createUser(email,password,name).also {
                                    repo.getCurrentUserTitleState().collect {
                                            title ->
                                        Log.d(TAG, "logUser: HERE 3")
                                        _mainActivityUIState.value =
                                            MainContract.State(
                                                MainContract.UserTitleState.Member(title.first),
                                                MainContract.UserProfilePicState.NewProfilePic(title.second)
                                            )
                                    }
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

    private fun logoutUser() {
        Log.d(TAG, "logoutUser: ")
        viewModelScope.launch {
                _stateShibaPage.value = ShibaViewState.Loading
            Log.d(TAG, "logoutUser: Sending Guest")
                repo.logoutUser().also {
                    _mainActivityUIState.value =
                        MainContract.State(
                                    MainContract.UserTitleState.Guest,
                                    MainContract.UserProfilePicState.DefaultPicture
                        )
                    _stateShibaPage.value = ShibaViewState.Idle
                }
        }
    }

    fun getCurrentUserEmail() : String {
        return repo.getCurrentUserEmail() ?: "Unsigned"
    }

    suspend fun getUserDisplayName() : String? {
        return repo.getCurrentUserDisplayName()
    }



    fun updatePhotosToCurrentUserDB(list: ArrayList<String>, originalUrlList: List<String>) {
        Log.d(TAG, "updatePhotosToCurrentUserDB: $list")
        viewModelScope.launch(Dispatchers.IO)  {
            withContext(Dispatchers.IO){
                repo.updateCurrentUserPhotos(list,originalUrlList)
            }
        }
    }

    private suspend fun updateUserProfilePicture(picture : String){
            repo.updateCurrentUserProfilePicture(picture).also {  }

    }

    fun isUserConnected(): Boolean {
        return repo.getCurrentUserEmail() != null
    }


}