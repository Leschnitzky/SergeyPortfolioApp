package com.leschnitzky.dailyshiba.usermanagement.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.viewModelScope
import com.leschnitzky.dailyshiba.MainContract
import com.leschnitzky.dailyshiba.UserIntent
import com.leschnitzky.dailyshiba.usermanagement.ui.main.state.ShibaViewState
import com.leschnitzky.dailyshiba.usermanagement.repository.Repository
import com.leschnitzky.dailyshiba.usermanagement.ui.extradetails.state.PhotoDetailsViewState
import com.leschnitzky.dailyshiba.usermanagement.ui.favorites.state.ShibaFavoritesStateView
import com.leschnitzky.dailyshiba.usermanagement.ui.login.viewstate.LoginViewState
import com.leschnitzky.dailyshiba.usermanagement.ui.profile.state.ProfileViewState
import com.leschnitzky.dailyshiba.usermanagement.ui.register.viewstate.RegisterViewState
import com.leschnitzky.dailyshiba.utils.isValidEmail
import com.facebook.AccessToken
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuthException
import com.leschnitzky.dailyshiba.utils.CoroutineContextProvider
import com.leschnitzky.dailyshiba.utils.CoroutineScopeProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.lang.Error
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext


@HiltViewModel
@ExperimentalCoroutinesApi
class UserViewModel @Inject constructor(
    var repo: Repository,
    var dispatcherProvider : CoroutineContextProvider,
    var coroutineScopeProvider : CoroutineScopeProvider
): ViewModel() {
    private val coroutineScope = getcoroutineScope()


    var currentPositionFavorites: Int = 0
    var currentPosition: Int = 0
    private val TAG = "UserViewModel"

   init {
       handleIntent()
   }

    val intentChannel = Channel<UserIntent>(Channel.UNLIMITED)



    /**
     * Configure CoroutineScope injection for production and testing.
     *
     * @receiver ViewModel provides coroutineScope for production
     * @param coroutineScope null for production, injects TestCoroutineScope for unit tests
     * @return CoroutineScope to launch coroutines on
     */
    private fun ViewModel.getcoroutineScope() : CoroutineScope {
        return if (coroutineScopeProvider.coroutineScope == null){
            Timber.d("corouotineScope is null")
            viewModelScope
        } else {
            Timber.d("corouotineScope not null")
            coroutineScopeProvider.coroutineScope!!
        }
    }

    
    
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



    fun handleIntent() = coroutineScope.launch(dispatcherProvider.ui) {

            intentChannel.consumeAsFlow().collect {
                Timber.d( "handleIntent: Got Intent $it")
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
                    is UserIntent.SendResetPassEmail -> startPasswordReset(it.email)
                    is UserIntent.UpdateUserSettings -> updateUserSettings(it.setting,it.field,it.value)
                }
            }
    }

    private fun updateUserSettings(setting: String, field: String, value: String) {
        Timber.d("Updating user settings")
        coroutineScope.launch(dispatcherProvider.io) {
            _stateProfilePage.value = ProfileViewState.Loading
            repo.updateCurrentUserSettings(setting,field,value).also {

                if(it.isEmpty()) {
                    withContext(dispatcherProvider.ui) {
                        _stateProfilePage.value = ProfileViewState.Idle
                    }
                } else {
                    withContext(dispatcherProvider.ui){
                        _stateProfilePage.value = ProfileViewState.Error(it)
                    }
                }
            }
        }
    }

    private fun startPasswordReset(email: String) {
        Timber.d("Starting password reset")
        coroutineScope.launch(dispatcherProvider.io) {
            _stateLoginPage.value = LoginViewState.Loading
            if(email.isNotEmpty()){
                if(isValidEmail(email)){
                    repo.sendResetEmail(email).also {
                        withContext(dispatcherProvider.ui){
                            _stateLoginPage.value = LoginViewState.ResetEmailSent
                        }
                    }
                } else {
                    _stateLoginPage.value = LoginViewState.Error("Current email is invalid", LoginViewState.LoginErrorCode.INVALID_EMAIL)
                }
            } else {
                _stateLoginPage.value = LoginViewState.Error("Email is empty" , LoginViewState.LoginErrorCode.EMPTY_EMAIL)
            }

        }
    }

    private fun updateDisplayNameForUser(displayName: String) {
        Timber.d( "updateDisplayNameForUser: ")
        coroutineScope.launch(dispatcherProvider.io) {
            _stateProfilePage.value = ProfileViewState.Loading
            repo.updateCurrentUserDisplayName(displayName).also {
                repo.getCurrentUserData().also {
                    _stateProfilePage.value = ProfileViewState.LoadedData(it)
                }.also {
                    withContext(dispatcherProvider.ui){
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
        Timber.d( "broadcastUserProfile: ")
        coroutineScope.launch(dispatcherProvider.io) {
            _stateProfilePage.value = ProfileViewState.Loading
            repo.getCurrentUserData().also {
                withContext(dispatcherProvider.ui){
                    _stateProfilePage.value = ProfileViewState.LoadedData(it)

                }
            }
        }
    }

    private fun logUserWithFacebook(token: AccessToken?) {
        Timber.d( "logUserWithFacebook: ")
        coroutineScope.launch(dispatcherProvider.io) {
            _stateLoginPage.value = LoginViewState.Loading
            repo.signInAccountWithFacebook(token).also {
                if(!repo.doesUserExistInFirestore(
                        getCurrentUserEmail()
                    )
                ) {
                    repo.createUserInFirestore(getCurrentUserEmail(), repo.getAuthDisplayName()).also {
                        _stateLoginPage.value = LoginViewState.Idle
                        repo.getCurrentUserTitleState().also {
                            if(repo.getDBUserData() == null){
                                repo.createUserInDB(getCurrentUserEmail()!!,it.first)
                            }
                            withContext(dispatcherProvider.ui){
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
                        if(repo.getDBUserData() == null){
                            repo.createUserInDB(getCurrentUserEmail()!!,it.first)
                        }
                        withContext(dispatcherProvider.ui){
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
        Timber.d( "logUserWithGoogle: ")
        coroutineScope.launch(dispatcherProvider.io) {
            _stateLoginPage.value = LoginViewState.Loading
            repo.signInAccountWithGoogle(signedInAccountFromIntent).also {
                if(!repo.doesUserExistInFirestore(
                        getCurrentUserEmail()
                    )
                ) {
                    repo.createUserInFirestore(getCurrentUserEmail(), repo.getAuthDisplayName()).also {
                        _stateLoginPage.value = LoginViewState.Idle
                        repo.getCurrentUserTitleState().also {
                            if(repo.getDBUserData() == null){
                                repo.createUserInDB(getCurrentUserEmail()!!,it.first)
                            }

                            withContext(dispatcherProvider.ui){
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
                        if(repo.getDBUserData() == null){
                            repo.createUserInDB(getCurrentUserEmail()!!,it.first)
                        }
                        withContext(dispatcherProvider.ui){
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
        coroutineScope.launch(dispatcherProvider.io) {
            _stateFavoritePage.value = ShibaFavoritesStateView.Loading
                repo.getCurrentUserFavorites().also {
                    withContext(dispatcherProvider.ui){
                        _stateFavoritePage.value = ShibaFavoritesStateView.PhotosLoaded(it)

                    }
                }
        }
    }

    private fun checkPhotoInFavorites(picture: String) {
        _stateDetailsPage.value = PhotoDetailsViewState.Loading
        coroutineScope.launch(dispatcherProvider.io) {
            repo.isPhotoInCurrentUserFavorites(picture).also {
                withContext(dispatcherProvider.ui){
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
        coroutineScope.launch(dispatcherProvider.io) {
            _stateDetailsPage.value = PhotoDetailsViewState.Loading
            repo.removePictureFromFavorites(picture).also {
                withContext(dispatcherProvider.ui){
                    _stateDetailsPage.value = PhotoDetailsViewState.Idle
                }
            }
        }
    }

    private fun addPictureToFavorite(picture: String) {
        coroutineScope.launch(dispatcherProvider.io) {
            _stateDetailsPage.value = PhotoDetailsViewState.Loading
            repo.addPictureToFavorites(picture).also {
                withContext(dispatcherProvider.ui){
                    _stateDetailsPage.value = PhotoDetailsViewState.PictureIsFavorite
                }
            }
        }
    }


    private suspend fun checkUserStatus() = coroutineScope.launch(dispatcherProvider.ui) {
            val email = getCurrentUserEmail()
            Timber.d( "checkUserStatus: $email")
            if(email == "Unsigned"){
                emitGuestUser()
            } else {
                repo.getCurrentUserTitleState().also {
                        pair ->
                    Timber.d( "checkUserStatus: $pair")
                        _mainActivityUIState.value =
                            MainContract.State(
                                MainContract.UserTitleState.Member(pair.first),
                                MainContract.UserProfilePicState.NewProfilePic(pair.second)
                            )

                }
            }
    }


    private suspend fun emitGuestUser(){
        withContext(dispatcherProvider.ui){
            _mainActivityUIState.value = (
                MainContract.State(
                    MainContract.UserTitleState.Guest,
                    MainContract.UserProfilePicState.DefaultPicture
                )
            )
            Timber.d( "checkUserStatus: Emitted Guest on ${this.coroutineContext}")
        }
    }

    private fun getMorePhotosForCurrentUser() {
        coroutineScope.launch(dispatcherProvider.io) {
            _stateShibaPage.value = ShibaViewState.Loading
            repo.getNewPhotosFromServer().let {
                Timber.d( "getPhotosForCurrentUser: updating $it")
                withContext(dispatcherProvider.ui){
                    _stateShibaPage.value = ShibaViewState.GotPhotos(it)
                }
            }
        }
    }

    private fun getPhotosForCurrentUser() {
        Timber.d( "getPhotosForCurrentUser: ")
        coroutineScope.launch(dispatcherProvider.io) {
        _stateShibaPage.value = ShibaViewState.Loading
            repo.getCurrentUserPhotos().let {
                Timber.d( "getPhotosForCurrentUser: updating $it")
                withContext(dispatcherProvider.ui){
                    _stateShibaPage.value = ShibaViewState.GotPhotos(it)
                }
            }
        }
    }

    private fun logUser(email: String, password: String) {
        Timber.d( "logUser: ")
        coroutineScope.launch(dispatcherProvider.io)  {
            _stateLoginPage.value = LoginViewState.Loading
            Timber.d( "logUser: HERE 1")
            _stateLoginPage.value = try {
                if(!email.isEmpty() && !password.isEmpty()){
                    if(isValidEmail(email)){
                        LoginViewState.LoggedIn(
                                repo.loginUserAndReturnName(email,password).also {
                                    repo.getCurrentUserTitleState().also {
                                            state ->
                                        Timber.d( "logUser: HERE 3")
                                        withContext(dispatcherProvider.ui){
                                            _mainActivityUIState.value =
                                                MainContract.State(
                                                    MainContract.UserTitleState.Member(state.first),
                                                    MainContract.UserProfilePicState.NewProfilePic(state.second)
                                                )
                                        }
                                    }
                                }
                        ).also {

                        }
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
                Timber.d( "logUser: caught Error ${e.javaClass}")
                LoginViewState.Error(e.localizedMessage, LoginViewState.LoginErrorCode.FIREBASE_ERROR)
            }
        }
    }
    private fun registerUser(name: String, email: String, password: String, selected: Boolean){
        Timber.d( "registerUser: $name , $email, $password, $selected")
        coroutineScope.launch(dispatcherProvider.io)  {
            _stateRegisterPage.value = RegisterViewState.Loading
            _stateRegisterPage.value = try {
                if(email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty()){
                    if(isValidEmail(email)){
                        if(selected){
                            RegisterViewState.Registered(
                                withContext(dispatcherProvider.io){
                                    repo.createUser(email,password,name).also {
                                        repo.getCurrentUserTitleState().also {
                                                title ->
                                            Timber.d( "logUser: HERE 3")

                                            withContext(dispatcherProvider.ui){
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
                Timber.d( "logUser: caught Error ${e.javaClass}")
                RegisterViewState.Error(e.localizedMessage, RegisterViewState.RegisterErrorCode.FIREBASE_ERROR)
            }
        }
    }

    private fun logoutUser() {
        Timber.d( "logoutUser: ")
        coroutineScope.launch(dispatcherProvider.io) {
                _stateShibaPage.value = ShibaViewState.Loading
            Timber.d( "logoutUser: Sending Guest")
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
        Timber.d( "updatePhotosToCurrentUserDB: $list \n\n\n $originalUrlList")
        coroutineScope.launch(dispatcherProvider.io)  {
            withContext(dispatcherProvider.ui){
                _stateShibaPage.value = ShibaViewState.Loading
            }
            withContext(dispatcherProvider.io){
                repo.updateCurrentUserPhotos(list,originalUrlList).also {
                    withContext(dispatcherProvider.ui){
                        _stateShibaPage.value = ShibaViewState.Idle
                    }
                }
            }
        }
    }

    private suspend fun updateUserProfilePicture(picture : String) = withContext(dispatcherProvider.io){
        _stateDetailsPage.value = PhotoDetailsViewState.Loading
            repo.updateCurrentUserProfilePicture(picture).also {

                val state = repo.getCurrentUserTitleState()
                withContext(dispatcherProvider.ui){
                    _stateDetailsPage.value = PhotoDetailsViewState.Idle
                    _mainActivityUIState.value =
                        MainContract.State(
                            MainContract.UserTitleState.Member(state.first),
                            MainContract.UserProfilePicState.NewProfilePic(picture)
                        )
                }
            }

    }


    fun getCurrentUserURLMap(): Flow<Map<String,String> > {
        return flow {
            coroutineScope.launch(dispatcherProvider.io){
                val map = repo.getCurrentUserURLMap()
                Timber.d( "getCurrentUserURLMap: $map")
                map
            }
        }
    }


}