package com.example.sergeyportfolioapp.usermanagement.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sergeyportfolioapp.usermanagement.ui.main.state.ShibaViewState
import com.example.sergeyportfolioapp.usermanagement.repository.Repository
import com.example.sergeyportfolioapp.usermanagement.ui.login.viewstate.LoginViewState
import com.example.sergeyportfolioapp.usermanagement.ui.register.viewstate.RegisterViewState
import com.example.sergeyportfolioapp.utils.GlobalTags.Companion.TAG_PROFILE_PIC
import com.example.sergeyportfolioapp.utils.isValidEmail
import com.google.firebase.auth.FirebaseAuthException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Scope


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

    val scope = viewModelScope

    private val TAG = "UserViewModel"

    val _intentChannel = Channel<UserIntent>(Channel.UNLIMITED)

   init {
       handleIntent()
   }

    private val _stateLoginPage = MutableStateFlow<LoginViewState>(LoginViewState.Idle)
    private val _stateRegisterPage = MutableStateFlow<RegisterViewState>(RegisterViewState.Idle)
    private val _stateShibaPage = MutableStateFlow<ShibaViewState>(ShibaViewState.Idle)

    val stateLoginPage: StateFlow<LoginViewState>
        get() = _stateLoginPage.asStateFlow()
    val stateRegisterPage: StateFlow<RegisterViewState>
        get() = _stateRegisterPage.asStateFlow()
    val stateShibaPage : StateFlow<ShibaViewState>
        get() = _stateShibaPage



    val userTitle : SharedFlow<UserTitleState>
        get() = _userTitle.asSharedFlow()
    private val _userTitle = MutableSharedFlow<UserTitleState>()



    val currPhotosInDB : StateFlow<ShibaViewState>
        get() = _currPhotosInDB.asStateFlow()
    private val _currPhotosInDB = MutableStateFlow<ShibaViewState>(ShibaViewState.Idle)

    val favoritePhotosInDB : StateFlow<ArrayList<String>>
        get() = _favoritePhotosInDB.asStateFlow()
    private val _favoritePhotosInDB = MutableStateFlow<ArrayList<String>>(arrayListOf())

    val currentProfilePicture : SharedFlow<UserProfilePicState>
        get() = _currentProfilePicture.asSharedFlow()
    private var _currentProfilePicture = MutableSharedFlow<UserProfilePicState>()


    private fun handleIntent() {
        viewModelScope.launch {
            _intentChannel.consumeAsFlow().collect {
                Log.d(TAG, "handleIntent: Got Intent $it")
                when (it) {
                    is UserIntent.Login -> logUser(it.email, it.password)
                    is UserIntent.Register -> registerUser(it.name,it.email,it.password,it.selected)
                    is UserIntent.SetProfilePicture -> updateUserProfilePicture(it.url)
//                    is UserIntent.DisplayProfilePicture -> loadCurrentUserProfilePicture()
                    is UserIntent.GetPhotos -> getPhotosForCurrentUser()
                    is UserIntent.CheckLogin -> checkUserStatus()
                    is UserIntent.LogoutUser -> logoutUser()
                }
            }
        }
    }

    private suspend fun checkUserStatus() {
        val email = getCurrentUserEmail()
            if(email == "Guest"){
                _userTitle.emit(UserTitleState.Guest)
                _currentProfilePicture.emit(UserProfilePicState.DefaultPicture)
            } else {
                withContext(Dispatchers.IO){
                    getUserDisplayName().let { name ->
                        if(name == null){
                            if(email == "Unsigned"){
                                viewModelScope.launch {
                                    _userTitle.emit(UserTitleState.Guest)
                                    _currentProfilePicture.emit(UserProfilePicState.DefaultPicture)
                                }

                            } else {
                                _userTitle.emit(UserTitleState.Member(email))
                                repo.getCurrentUserProfilePic().collect {
                                    viewModelScope.launch {
                                        if(it.profilePicURI.isEmpty()){
                                            _currentProfilePicture.emit(UserProfilePicState.DefaultPicture)
                                        } else {
                                            _currentProfilePicture.emit(UserProfilePicState.NewProfilePic(it.profilePicURI))

                                        }
                                    }
                                }
                            }
                        } else {
                            _userTitle.emit(UserTitleState.Member(name))
                            repo.getCurrentUserProfilePic().collect {

                                viewModelScope.launch {
                                    if(it.profilePicURI.isEmpty()){
                                        _currentProfilePicture.emit(UserProfilePicState.DefaultPicture)

                                    } else {
                                        _currentProfilePicture.emit(UserProfilePicState.NewProfilePic(it.profilePicURI))

                                    }
                                }

                            }
                        }


                    }
                }
            }
    }

    private fun getPhotosForCurrentUser() {
//        _stateShibaPage.value = ShibaViewState.Loading
//        viewModelScope.launch {
//            repo.getCurrentUserPhotos().let {
//                _stateShibaPage.value = ShibaViewState.GotPhotos(it)
//            }
//        }
    }

    private fun loadCurrentUserProfilePicture() {
        Log.d("$TAG.$TAG_PROFILE_PIC", "loadCurrentUserProfilePicture: ")
        _stateShibaPage.value = ShibaViewState.Loading
        viewModelScope.launch {
            repo.getCurrentUserProfilePic().collect {
                Log.d("$TAG.$TAG_PROFILE_PIC", "loadCurrentUserProfilePicture: 2 ")
                withContext(MainScope().coroutineContext){
                    if(it.profilePicURI.isNullOrEmpty()){
                        _currentProfilePicture.emit(UserProfilePicState.DefaultPicture)

                    } else {
                        _currentProfilePicture.emit(UserProfilePicState.NewProfilePic(it.profilePicURI))

                    }
                }
            }
        }
    }

    private fun logUser(email: String, password: String) {
        viewModelScope.launch(MainScope().coroutineContext)  {
            _stateLoginPage.value = LoginViewState.Loading
            Log.d(TAG, "logUser: HERE 1")
            _stateLoginPage.value = try {
                if(!email.isEmpty() && !password.isEmpty()){
                    if(isValidEmail(email)){
                        LoginViewState.LoggedIn(
                            repo.loginUserAndReturnName(email,password).also {
                                Log.d(TAG, "logUser: HERE 2")
                                _userTitle.emit(UserTitleState.Member(it))
                            }.also {

                                repo.getCurrentUserProfilePic().collect {
                                    Log.d(TAG, "logUser: HERE 3")
                                    _currentProfilePicture.emit(UserProfilePicState.NewProfilePic(it.profilePicURI))
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
        viewModelScope.launch(Dispatchers.IO)  {
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

    private fun logoutUser() {
        Log.d(TAG, "logoutUser: ")

            viewModelScope.launch {
                _stateShibaPage.value = ShibaViewState.Loading
            Log.d(TAG, "logoutUser: Sending Guest")
                repo.logoutUser().also {
                    withContext(MainScope().coroutineContext){
                        _userTitle.emit(UserTitleState.Guest)
                        _currentProfilePicture.emit(UserProfilePicState.DefaultPicture)
                        _stateShibaPage.value = ShibaViewState.Idle
                    }
                }
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
            repo.updateCurrentUserProfilePicture(picture).let {
                _currentProfilePicture.emit(UserProfilePicState.NewProfilePic(picture))
            }

    }

    suspend fun getCurrentUserURLMap() : Map<String,String> {
        return repo.getCurrentUserURLMap()
    }


    suspend fun getProfilePictureFlow(): Flow<UserProfilePicState> {
        return repo.getCurrentUserProfilePic().map {
            UserProfilePicState.NewProfilePic(it.profilePicURI)
        }
    }




}