package com.example.sergeyportfolioapp.usermanagement.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sergeyportfolioapp.MainContract
import com.example.sergeyportfolioapp.UserIntent
import com.example.sergeyportfolioapp.usermanagement.ui.main.state.ShibaViewState
import com.example.sergeyportfolioapp.usermanagement.repository.Repository
import com.example.sergeyportfolioapp.usermanagement.ui.extradetails.state.PhotoDetailsViewState
import com.example.sergeyportfolioapp.usermanagement.ui.favorites.state.ShibaFavoritesStateView
import com.example.sergeyportfolioapp.usermanagement.ui.login.viewstate.LoginViewState
import com.example.sergeyportfolioapp.usermanagement.ui.profile.state.ProfileViewState
import com.example.sergeyportfolioapp.usermanagement.ui.register.viewstate.RegisterViewState
import com.example.sergeyportfolioapp.utils.isValidEmail
import com.facebook.AccessToken
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
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

    private val TAG = "UserViewModel"

   init {
       handleIntent()
   }

    val intentChannel = Channel<UserIntent>(Channel.UNLIMITED)



    private val _stateFavoritePage = MutableStateFlow<ShibaFavoritesStateView>(ShibaFavoritesStateView.InitState)
    private val _stateLoginPage = MutableStateFlow<LoginViewState>(LoginViewState.Idle)
    private val _stateRegisterPage = MutableStateFlow<RegisterViewState>(RegisterViewState.Idle)
    private val _stateShibaPage = MutableStateFlow<ShibaViewState>(ShibaViewState.Idle)
    private val _stateDetailsPage = MutableStateFlow<PhotoDetailsViewState>(PhotoDetailsViewState.InitState)
    private val _stateProfilePage = MutableStateFlow<ProfileViewState>(ProfileViewState.Idle)

    val stateFavoritePage : StateFlow<ShibaFavoritesStateView>
        get() = _stateFavoritePage
    val stateLoginPage: StateFlow<LoginViewState>
        get() = _stateLoginPage.asStateFlow()
    val stateRegisterPage: StateFlow<RegisterViewState>
        get() = _stateRegisterPage.asStateFlow()
    val stateShibaPage : StateFlow<ShibaViewState>
        get() = _stateShibaPage
    val stateDetailsPage: StateFlow<PhotoDetailsViewState>
        get() = _stateDetailsPage
    val stateProfilePage: StateFlow<ProfileViewState>
        get() = _stateProfilePage



    val mainActivityUIState : StateFlow<MainContract.State>
        get() = _mainActivityUIState
    private val _mainActivityUIState = MutableStateFlow(MainContract.State(
        MainContract.UserTitleState.InitState,
        MainContract.UserProfilePicState.InitState
    ))



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
                    is UserIntent.GetPhotos -> getPhotosForCurrentUser()
                    is UserIntent.GetNewPhotos -> getMorePhotosForCurrentUser()
                    is UserIntent.CheckLogin -> checkUserStatus()
                    is UserIntent.LogoutUser -> logoutUser()
                    is UserIntent.AddPictureFavorite -> addPictureToFavorite(it.picture)
                    is UserIntent.RemovePictureFavorite -> removePictureFromFavorites(it.picture)
                    is UserIntent.CheckPhotoInFavorites -> checkPhotoInFavorites(it.picture)
                    is UserIntent.UpdateFavoritesPage -> updateFavoritesPage()
                    is UserIntent.SignInGoogle -> logUserWithGoogle(it.signedInAccountFromIntent)
                    is UserIntent.FacebookSignIn -> logUserWithFacebook(it.accessToken)
                    is UserIntent.GetProfileData -> broadcastUserProfile()
                    is UserIntent.UpdateDisplayName -> updateDisplayNameForUser(it.editTextValue)
                }
            }
    }

    private fun updateDisplayNameForUser(displayName: String) {
        Log.d(TAG, "updateDisplayNameForUser: ")
        viewModelScope.launch(Dispatchers.IO) {
            _stateProfilePage.value = ProfileViewState.Loading
            repo.updateCurrentUserDisplayName(displayName).also {
                repo.getCurrentUserData().also {
                    _stateProfilePage.value = ProfileViewState.LoadedData(it)
                }.also {
                    withContext(Dispatchers.Main){
                        _mainActivityUIState.value =
                            MainContract.State(
                                MainContract.UserTitleState.MemberNoNavigate(it.displayName),
                                MainContract.UserProfilePicState.NewProfilePic(it.profilePicURI)
                            )
                    }
                }
            }
        }
    }

    private fun broadcastUserProfile() {
        Log.d(TAG, "broadcastUserProfile: ")
        viewModelScope.launch(Dispatchers.IO) {
            _stateProfilePage.value = ProfileViewState.Loading
            repo.getCurrentUserData().also {
                withContext(Dispatchers.Main){
                    _stateProfilePage.value = ProfileViewState.LoadedData(it)

                }
            }
        }
    }

    private fun logUserWithFacebook(token: AccessToken?) {
        Log.d(TAG, "logUserWithFacebook: ")
        viewModelScope.launch(Dispatchers.IO) {
            _stateLoginPage.value = LoginViewState.Loading
            repo.signInAccountWithFacebook(token).also {
                if(!repo.doesUserExistInFirestore(
                        getCurrentUserEmail()
                    )
                ) {
                    repo.createUserInDB(getCurrentUserEmail(), repo.getAuthDisplayName())
                    repo.createUserInFirestore(getCurrentUserEmail(), repo.getAuthDisplayName()).also {
                        _stateLoginPage.value = LoginViewState.Idle
                        repo.getCurrentUserTitleState().also {
                            withContext(Dispatchers.Main){
                                _mainActivityUIState.value =
                                    MainContract.State(
                                        MainContract.UserTitleState.Member(it.first),
                                        MainContract.UserProfilePicState.NewProfilePic(it.second)
                                    )
                            }
                        }
                    }
                } else {
                    repo.getCurrentUserTitleState().also {
                        withContext(Dispatchers.Main){
                            _mainActivityUIState.value =
                                MainContract.State(
                                    MainContract.UserTitleState.Member(it.first),
                                    MainContract.UserProfilePicState.NewProfilePic(it.second)
                                )
                        }
                    }
                }
            }
        }
    }

    private fun logUserWithGoogle(signedInAccountFromIntent: Task<GoogleSignInAccount>?) {
        Log.d(TAG, "logUserWithGoogle: ")
        viewModelScope.launch(Dispatchers.IO) {
            _stateLoginPage.value = LoginViewState.Loading
            repo.signInAccountWithGoogle(signedInAccountFromIntent).also {
                if(!repo.doesUserExistInFirestore(
                        getCurrentUserEmail()
                    )
                ) {
                    repo.createUserInDB(getCurrentUserEmail(), repo.getAuthDisplayName())
                    repo.createUserInFirestore(getCurrentUserEmail(), repo.getAuthDisplayName()).also {
                        _stateLoginPage.value = LoginViewState.Idle
                        repo.getCurrentUserTitleState().also {

                            withContext(Dispatchers.Main){
                                _mainActivityUIState.value =
                                    MainContract.State(
                                        MainContract.UserTitleState.Member(it.first),
                                        MainContract.UserProfilePicState.NewProfilePic(it.second)
                                    )
                            }
                        }
                    }
                } else {
                    repo.getCurrentUserTitleState().also {
                        withContext(Dispatchers.Main){
                            _mainActivityUIState.value =
                                MainContract.State(
                                    MainContract.UserTitleState.Member(it.first),
                                    MainContract.UserProfilePicState.NewProfilePic(it.second)
                                )
                        }
                    }
                }
            }
        }
    }

    private fun updateFavoritesPage() {
        viewModelScope.launch(Dispatchers.IO) {
            _stateFavoritePage.value = ShibaFavoritesStateView.Loading
                repo.getCurrentUserFavorites().also {
                    withContext(Dispatchers.Main){
                        _stateFavoritePage.value = ShibaFavoritesStateView.PhotosLoaded(it)

                    }
                }
        }
    }

    private fun checkPhotoInFavorites(picture: String) {
        _stateDetailsPage.value = PhotoDetailsViewState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            repo.isPhotoInCurrentUserFavorites(picture).also {
                withContext(Dispatchers.Main){
                    if(it){
                        _stateDetailsPage.value = PhotoDetailsViewState.PictureIsFavorite
                    } else {
                        _stateDetailsPage.value = PhotoDetailsViewState.Idle
                    }
                }
            }
        }
    }

    private fun removePictureFromFavorites(picture: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _stateDetailsPage.value = PhotoDetailsViewState.Loading
            repo.removePictureFromFavorites(picture).also {
                withContext(Dispatchers.Main){
                    _stateDetailsPage.value = PhotoDetailsViewState.Idle
                }
            }
        }
    }

    private fun addPictureToFavorite(picture: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _stateDetailsPage.value = PhotoDetailsViewState.Loading
            repo.addPictureToFavorites(picture).also {
                withContext(Dispatchers.Main){
                    _stateDetailsPage.value = PhotoDetailsViewState.PictureIsFavorite
                }
            }
        }
    }


    private suspend fun checkUserStatus() = withContext(viewModelScope.coroutineContext) {
            val email = getCurrentUserEmail()
            Log.d(TAG, "checkUserStatus: $email")
            if(email == "Unsigned"){
                emitGuestUser()
            } else {
                repo.getCurrentUserTitleState().also {
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

    private fun getMorePhotosForCurrentUser() {
        viewModelScope.launch(Dispatchers.IO) {
            _stateShibaPage.value = ShibaViewState.Loading
            repo.getNewPhotosFromServer().let {
                Log.d(TAG, "getPhotosForCurrentUser: updating $it")
                withContext(Dispatchers.Main){
                    _stateShibaPage.value = ShibaViewState.GotPhotos(it)
                }
            }
        }
    }

    private fun getPhotosForCurrentUser() {
        Log.d(TAG, "getPhotosForCurrentUser: ")
        viewModelScope.launch(Dispatchers.IO) {
        _stateShibaPage.value = ShibaViewState.Loading
            repo.getCurrentUserPhotos().let {
                Log.d(TAG, "getPhotosForCurrentUser: updating $it")
                withContext(Dispatchers.Main){
                    _stateShibaPage.value = ShibaViewState.GotPhotos(it)
                }
            }
        }
    }

    private fun logUser(email: String, password: String) {
        Log.d(TAG, "logUser: ")
        viewModelScope.launch  {
            _stateLoginPage.value = LoginViewState.Loading
            Log.d(TAG, "logUser: HERE 1")
            _stateLoginPage.value = try {
                if(!email.isEmpty() && !password.isEmpty()){
                    if(isValidEmail(email)){
                        LoginViewState.LoggedIn(
                            withContext(Dispatchers.IO){
                                repo.loginUserAndReturnName(email,password).also {
                                    Log.d(TAG, "logUser: HERE 2")
                                }.also {
                                    repo.getCurrentUserTitleState().also {
                                            state ->
                                        Log.d(TAG, "logUser: HERE 3")
                                        withContext(Dispatchers.Main){
                                            _mainActivityUIState.value =
                                                MainContract.State(
                                                    MainContract.UserTitleState.Member(state.first),
                                                    MainContract.UserProfilePicState.NewProfilePic(state.second)
                                                )
                                        }
                                    }
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
                                withContext(Dispatchers.IO){
                                    repo.createUser(email,password,name).also {
                                        repo.getCurrentUserTitleState().also {
                                                title ->
                                            Log.d(TAG, "logUser: HERE 3")

                                            withContext(Dispatchers.Main){
                                                _mainActivityUIState.value =
                                                    MainContract.State(
                                                        MainContract.UserTitleState.Member(title.first),
                                                        MainContract.UserProfilePicState.NewProfilePic(title.second)
                                                    )
                                            }
                                        }
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
                    _stateShibaPage.value = ShibaViewState.Idle
                    _stateLoginPage.value = LoginViewState.Idle
                    _mainActivityUIState.value =
                        MainContract.State(
                                    MainContract.UserTitleState.Guest,
                                    MainContract.UserProfilePicState.DefaultPicture
                        ) }
        }
    }

    fun getCurrentUserEmail() : String {
        return repo.getCurrentUserEmail() ?: "Unsigned"
    }



    fun updatePhotosToCurrentUserDB(list: ArrayList<String>, originalUrlList: List<String>) {
        Log.d(TAG, "updatePhotosToCurrentUserDB: $list")
        viewModelScope.launch(Dispatchers.IO)  {
            withContext(Dispatchers.IO){
                repo.updateCurrentUserPhotos(list,originalUrlList)
            }
        }
    }

    private suspend fun updateUserProfilePicture(picture : String) = withContext(Dispatchers.IO){
        _stateDetailsPage.value = PhotoDetailsViewState.Loading
            repo.updateCurrentUserProfilePicture(picture).also {

                val state = repo.getCurrentUserTitleState()
                withContext(Dispatchers.Main){
                    _stateDetailsPage.value = PhotoDetailsViewState.Idle
                    _mainActivityUIState.value =
                        MainContract.State(
                            MainContract.UserTitleState.Member(state.first),
                            MainContract.UserProfilePicState.NewProfilePic(picture)
                        )
                }
            }

    }


    suspend fun getCurrentUserURLMap(): Map<String,String> {
            val map = repo.getCurrentUserURLMap()
            Log.d(TAG, "getCurrentUserURLMap: $map")
            return map
    }


}