package com.leschnitzky.dailyshiba.viewmodel

import android.text.TextUtils
import app.cash.turbine.test
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuthException
import com.leschnitzky.dailyshiba.MainContract
import com.leschnitzky.dailyshiba.TestCoroutineRule
import com.leschnitzky.dailyshiba.UserIntent
import com.leschnitzky.dailyshiba.di.FakeRepositoryImpl
import com.leschnitzky.dailyshiba.di.TestCoroutineContextProvider
import com.leschnitzky.dailyshiba.runBlockingTest
import com.leschnitzky.dailyshiba.usermanagement.repository.Repository
import com.leschnitzky.dailyshiba.usermanagement.repository.firestore.model.UserForFirestore
import com.leschnitzky.dailyshiba.usermanagement.ui.UserViewModel
import com.leschnitzky.dailyshiba.usermanagement.ui.extradetails.state.PhotoDetailsViewState
import com.leschnitzky.dailyshiba.usermanagement.ui.favorites.state.ShibaFavoritesStateView
import com.leschnitzky.dailyshiba.usermanagement.ui.login.viewstate.LoginViewState
import com.leschnitzky.dailyshiba.usermanagement.ui.main.state.ShibaViewState
import com.leschnitzky.dailyshiba.usermanagement.ui.profile.state.ProfileViewState
import com.leschnitzky.dailyshiba.usermanagement.ui.register.viewstate.RegisterViewState
import com.leschnitzky.dailyshiba.utils.CoroutineScopeProviderImpl
import com.leschnitzky.dailyshiba.utils.isValidEmail
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import timber.log.Timber
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime

@RunWith(JUnit4::class)
class UserViewModelTest {

    @get:Rule
    val mainCoroutineRule = TestCoroutineRule()


    lateinit var sutViewModel : UserViewModel
    var mockRepository: Repository = mockk(relaxed = true)
    var spyMock = spyk(mockRepository)
    val testCoroutineContextProvider : TestCoroutineContextProvider = TestCoroutineContextProvider(mainCoroutineRule.testDispatcher)
    val testScope = TestCoroutineScope(mainCoroutineRule.testDispatcher)

    @Before
    fun setup(){
        mockkStatic("com.leschnitzky.dailyshiba.utils.HelperFunctionsKt")
        sutViewModel = UserViewModel(
            spyMock,
            testCoroutineContextProvider,
            CoroutineScopeProviderImpl(
                testScope
            ))
    }

    @ExperimentalTime
    @ExperimentalCoroutinesApi
    @Test
    fun userViewModel_CheckLoginWithUnsignedUser_ShouldEmitCorrectValues() = mainCoroutineRule.runBlockingTest {
        coEvery {
            spyMock.getCurrentUserEmail()
        } returns "Unsigned"

        sutViewModel.intentChannel.trySend(UserIntent.CheckLogin)
        sutViewModel.handleIntent()

        sutViewModel.mainActivityUIState.test {
            assertEquals(
                awaitItem(),
                MainContract.State(
                    MainContract.UserTitleState.Guest,
                    MainContract.UserProfilePicState.DefaultPicture
                )
            )
        }

    }

    @ExperimentalTime
    @ExperimentalCoroutinesApi
    @Test
    fun userViewModel_CheckLoginWithSigned_ShouldEmitCorrectValues() = mainCoroutineRule.runBlockingTest {
        coEvery {
            spyMock.getCurrentUserEmail()
        } returns FakeRepositoryImpl().getCurrentUserEmail()
        coEvery {
            spyMock.getCurrentUserTitleState()
        } returns FakeRepositoryImpl().getCurrentUserTitleState()

        sutViewModel.intentChannel.trySend(UserIntent.CheckLogin)
        sutViewModel.handleIntent()

        sutViewModel.mainActivityUIState.test {
            assertEquals(
                awaitItem(),
                MainContract.State(
                    MainContract.UserTitleState.Member("Serg"),
                    MainContract.UserProfilePicState.NewProfilePic("http://cdn.shibe.online/shibes/09db03fb29ced77549f07f0de875f3d309f38361.jpg")
                )
            )
        }

    }


    @ExperimentalCoroutinesApi
    @ExperimentalTime
    @Test
    fun userViewModel_LoginWithGoodValues_EmitSaidValuesToView() = mainCoroutineRule.runBlockingTest{
        // Need to mock higher level functions
        every {
            isValidEmail(any())
        } returns true
        coEvery {
            spyMock.getCurrentUserTitleState()
        } returns FakeRepositoryImpl().getCurrentUserTitleState()

        coEvery {
            spyMock.loginUserAndReturnName(any(),any())
        } returns "Serg"

        sutViewModel.intentChannel.trySend(UserIntent.Login("test123@gmail.com", "123456"))
        sutViewModel.handleIntent()


        sutViewModel.stateLoginPage.test {
            assertEquals(
                awaitItem(),
                LoginViewState.LoggedIn("Serg")
            )
        }
    }

    @ExperimentalCoroutinesApi
    @ExperimentalTime
    @Test
    fun userViewModel_LoginBadEmail_ShouldEmitError() = mainCoroutineRule.runBlockingTest{
        // Need to mock higher level functions
        every {
            isValidEmail(any())
        } returns false

        sutViewModel.intentChannel.trySend(UserIntent.Login("test123", "123456"))
        sutViewModel.handleIntent()


        sutViewModel.stateLoginPage.test {
            assertEquals(
                awaitItem(),
                LoginViewState.Error("Email is invalid", LoginViewState.LoginErrorCode.INVALID_EMAIL)
            )
        }
    }

    @ExperimentalCoroutinesApi
    @ExperimentalTime
    @Test
    fun userViewModel_LoginNoPass_ShouldEmitError() = mainCoroutineRule.runBlockingTest{
        // Need to mock higher level functions
        sutViewModel.intentChannel.trySend(UserIntent.Login("test123@gmail.com", ""))
        sutViewModel.handleIntent()


        sutViewModel.stateLoginPage.test {
            assertEquals(
                awaitItem(),
                LoginViewState.Error("The following field is empty", LoginViewState.LoginErrorCode.EMPTY_PASSWORD)
            )
        }
    }

    @ExperimentalCoroutinesApi
    @ExperimentalTime
    @Test
    fun userViewModel_LoginNoUser_ShouldEmitError() = mainCoroutineRule.runBlockingTest{
        // Need to mock higher level functions
        sutViewModel.intentChannel.trySend(UserIntent.Login("", "123456"))
        sutViewModel.handleIntent()


        sutViewModel.stateLoginPage.test {
            assertEquals(
                awaitItem(),
                LoginViewState.Error("The following field is empty", LoginViewState.LoginErrorCode.EMPTY_EMAIL)
            )
        }
    }

    @ExperimentalTime
    @Test
    fun userViewModel_LoginFakeFirebaseError_ShouldEmitError() = mainCoroutineRule.runBlockingTest{
        val exc = mockk<FirebaseAuthException>(relaxed = true)
        coEvery {
            spyMock.loginUserAndReturnName(any(),any())
        } throws exc
        every {
            isValidEmail(any())
        } returns true

        // Need to mock higher level functions
        sutViewModel.intentChannel.trySend(UserIntent.Login("test123@gmail.com", "123456"))
        sutViewModel.handleIntent()


        sutViewModel.stateLoginPage.test {
            assertEquals(
                awaitItem(),
                LoginViewState.Error("", LoginViewState.LoginErrorCode.FIREBASE_ERROR)
            )
        }
    }


    @ExperimentalTime
    @ExperimentalCoroutinesApi
    @Test
    fun userViewModel_RegisterWithGoodValues_EmitSaidValuesToView() = mainCoroutineRule.runBlockingTest {
        every {
            isValidEmail(any())
        } returns true
        coEvery {
            spyMock.createUser(any(),any(),any())
        } returns "Serg"

        coEvery {
            spyMock.getCurrentUserTitleState()
        } returns FakeRepositoryImpl().getCurrentUserTitleState()

        sutViewModel.intentChannel.trySend(UserIntent.Register("Serg", "serg@gmail.com", "123456", true))
        sutViewModel.handleIntent()

        sutViewModel.mainActivityUIState.test {
            assertEquals(
                awaitItem(),
                MainContract.State(
                    MainContract.UserTitleState.Member("Serg"),
                    MainContract.UserProfilePicState.NewProfilePic("http://cdn.shibe.online/shibes/09db03fb29ced77549f07f0de875f3d309f38361.jpg")
                )
            )
        }

    }

    @ExperimentalTime
    @ExperimentalCoroutinesApi
    @Test
    fun userViewModel_RegisterWithEmptyName_EmitError() = mainCoroutineRule.runBlockingTest {

        sutViewModel.intentChannel.trySend(UserIntent.Register("", "serg@gmail.com", "123456", true))
        sutViewModel.handleIntent()

        sutViewModel.stateRegisterPage.test {
            assertEquals(
                awaitItem(),
                RegisterViewState.Error("The following field is empty", RegisterViewState.RegisterErrorCode.EMPTY_NAME)
            )
        }

    }


    @ExperimentalTime
    @ExperimentalCoroutinesApi
    @Test
    fun userViewModel_RegisterWithEmptyEmail_EmitError() = mainCoroutineRule.runBlockingTest {

        sutViewModel.intentChannel.trySend(UserIntent.Register("Serg", "", "123456", true))
        sutViewModel.handleIntent()

        sutViewModel.stateRegisterPage.test {
            assertEquals(
                awaitItem(),
                RegisterViewState.Error("The following field is empty", RegisterViewState.RegisterErrorCode.EMPTY_EMAIL)
            )
        }

    }

    @ExperimentalTime
    @ExperimentalCoroutinesApi
    @Test
    fun userViewModel_RegisterWithEmptyPass_EmitError() = mainCoroutineRule.runBlockingTest {

        sutViewModel.intentChannel.trySend(UserIntent.Register("Serg", "serg@gmail.com", "", true))
        sutViewModel.handleIntent()

        sutViewModel.stateRegisterPage.test {
            assertEquals(
                awaitItem(),
                RegisterViewState.Error("The following field is empty", RegisterViewState.RegisterErrorCode.EMPTY_PASSWORD)
            )
        }

    }

    @ExperimentalTime
    @ExperimentalCoroutinesApi
    @Test
    fun userViewModel_RegisterWithUncheckedField_EmitError() = mainCoroutineRule.runBlockingTest {
        every {
            isValidEmail(any())
        } returns true
        sutViewModel.intentChannel.trySend(UserIntent.Register("Serg", "serg@gmail.com", "123456", false))
        sutViewModel.handleIntent()

        sutViewModel.stateRegisterPage.test {
            assertEquals(
                awaitItem(),
                RegisterViewState.Error("Terms and conditions are not checked", RegisterViewState.RegisterErrorCode.DIDNT_ACCEPT_TERMS)
            )
        }

    }

    @ExperimentalTime
    @ExperimentalCoroutinesApi
    @Test
    fun userViewModel_RegisterWithInvalidEmail_EmitError() = mainCoroutineRule.runBlockingTest {
        every {
            isValidEmail(any())
        } returns false
        sutViewModel.intentChannel.trySend(UserIntent.Register("Serg", "sergmail.com", "123456", false))
        sutViewModel.handleIntent()

        sutViewModel.stateRegisterPage.test {
            assertEquals(
                awaitItem(),
                RegisterViewState.Error("Email is invalid", RegisterViewState.RegisterErrorCode.INVALID_EMAIL)
            )
        }

    }

    @ExperimentalTime
    @ExperimentalCoroutinesApi
    @Test
    fun userViewModel_RegisterWithFakeFirebaseError_EmitError() = mainCoroutineRule.runBlockingTest {
        val exc = mockk<FirebaseAuthException>(relaxed = true)
        coEvery {
            spyMock.createUser(any(),any(),any())
        } throws exc
        every {
            isValidEmail(any())
        } returns true

        sutViewModel.intentChannel.trySend(UserIntent.Register("Serg", "sergmail.com", "123456", true))
        sutViewModel.handleIntent()

        sutViewModel.stateRegisterPage.test {
            assertEquals(
                awaitItem(),
                RegisterViewState.Error("", RegisterViewState.RegisterErrorCode.FIREBASE_ERROR)
            )
        }
    }

    @ExperimentalTime
    @ExperimentalCoroutinesApi
    @Test
    fun userViewModel_UpdateProfilePicture_ShouldEmitPicture() = mainCoroutineRule.runBlockingTest {
        coEvery {
            spyMock.getCurrentUserTitleState()
        } returns Pair("Serg","test")
        sutViewModel.intentChannel.trySend(UserIntent.SetProfilePicture("test"))
        sutViewModel.handleIntent()


        coVerify(exactly = 1) {
            spyMock.updateCurrentUserProfilePicture(any())
        }
        sutViewModel.mainActivityUIState.test {
            assertEquals(
                awaitItem(),
                MainContract.State(
                    MainContract.UserTitleState.Member("Serg"),
                    MainContract.UserProfilePicState.NewProfilePic("test")
                )
            )
        }
    }

    @ExperimentalTime
    @ExperimentalCoroutinesApi
    @Test
    fun userViewModel_GetPhotosForUser_ShouldEmitPictures() = mainCoroutineRule.runBlockingTest {
        coEvery {
            spyMock.getCurrentUserPhotos()
        } returns FakeRepositoryImpl().getCurrentUserPhotos()
        sutViewModel.intentChannel.trySend(UserIntent.GetPhotos)
        sutViewModel.handleIntent()

        sutViewModel.stateShibaPage.test {
            val item = awaitItem() as ShibaViewState.GotPhotos
            assertEquals(item.list.size, 10)
        }
    }


    @ExperimentalTime
    @ExperimentalCoroutinesApi
    @Test
    fun userViewModel_GetNewPhotosForUser_ShouldEmitPictures() = mainCoroutineRule.runBlockingTest {
        coEvery {
            spyMock.getNewPhotosFromServer()
        } returns FakeRepositoryImpl().getCurrentUserPhotos()


        sutViewModel.intentChannel.trySend(UserIntent.GetNewPhotos)
        sutViewModel.handleIntent()

        sutViewModel.stateShibaPage.test {
            val item = awaitItem() as ShibaViewState.GotPhotos
            assertEquals(item.list.size, 10)
        }
    }

    @ExperimentalTime
    @ExperimentalCoroutinesApi
    @Test
    fun userViewModel_Logout_ShouldEmitGuest() = mainCoroutineRule.runBlockingTest {

        sutViewModel.intentChannel.trySend(UserIntent.LogoutUser)
        sutViewModel.handleIntent()

        coVerify(exactly = 1){
            spyMock.logoutUser()
        }

        sutViewModel.mainActivityUIState.test {
            assertEquals(
                awaitItem(),
                MainContract.State(
                    MainContract.UserTitleState.Guest,
                    MainContract.UserProfilePicState.DefaultPicture
                )
            )
        }
    }

    @ExperimentalTime
    @ExperimentalCoroutinesApi
    @Test
    fun userViewModel_AddPictureFavorite_ShouldEmitPicture() = mainCoroutineRule.runBlockingTest {

        sutViewModel.intentChannel.trySend(UserIntent.AddPictureFavorite("test"))
        sutViewModel.handleIntent()

        coVerify(exactly = 1){
            spyMock.addPictureToFavorites(any())
        }

        sutViewModel.stateDetailsPage.test {
            assertEquals(
                awaitItem(),
                PhotoDetailsViewState.PictureIsFavorite
            )
        }
    }

    @ExperimentalTime
    @ExperimentalCoroutinesApi
    @Test
    fun userViewModel_RemovePictureFavorite_ShouldEmitPicture() = mainCoroutineRule.runBlockingTest {

        sutViewModel.intentChannel.trySend(UserIntent.RemovePictureFavorite("test"))
        sutViewModel.handleIntent()

        coVerify(exactly = 1){
            spyMock.removePictureFromFavorites(any())
        }

        sutViewModel.stateDetailsPage.test {
            assertEquals(
                awaitItem(),
                PhotoDetailsViewState.Idle
            )
        }
    }

    @ExperimentalTime
    @ExperimentalCoroutinesApi
    @Test
    fun userViewModel_CheckPhotoFavoriteIsTrue_ShouldEmitPhotoIsFavorite() = mainCoroutineRule.runBlockingTest {
        coEvery {
            spyMock.isPhotoInCurrentUserFavorites(any())
        } returns true

        sutViewModel.intentChannel.trySend(UserIntent.CheckPhotoInFavorites("test"))
        sutViewModel.handleIntent()

        coVerify(exactly = 1){
            spyMock.isPhotoInCurrentUserFavorites(any())
        }
        sutViewModel.stateDetailsPage.test {
            assertEquals(
                awaitItem(),
                PhotoDetailsViewState.PictureIsFavorite
            )
        }
    }

    @ExperimentalTime
    @ExperimentalCoroutinesApi
    @Test
    fun userViewModel_CheckPhotoFavoriteIsFalse_ShouldEmitPhotoIsFavorite() = mainCoroutineRule.runBlockingTest {
        coEvery {
            spyMock.isPhotoInCurrentUserFavorites(any())
        } returns false

        sutViewModel.intentChannel.trySend(UserIntent.CheckPhotoInFavorites("test"))
        sutViewModel.handleIntent()

        coVerify(exactly = 1){
            spyMock.isPhotoInCurrentUserFavorites(any())
        }
        sutViewModel.stateDetailsPage.test {
            assertEquals(
                awaitItem(),
                PhotoDetailsViewState.Idle
            )
        }
    }

    @ExperimentalTime
    @ExperimentalCoroutinesApi
    @Test
    fun userViewModel_UpdateUserFavorites_ShouldEmitPhotos() = mainCoroutineRule.runBlockingTest {

        coEvery {
            spyMock.getCurrentUserFavorites()
        } returns FakeRepositoryImpl().getCurrentUserPhotos()

        sutViewModel.intentChannel.trySend(UserIntent.UpdateFavoritesPage)
        sutViewModel.handleIntent()

        sutViewModel.stateFavoritePage.test {
            val item = awaitItem() as ShibaFavoritesStateView.PhotosLoaded
            assertEquals(
                10,
                item.list.size
            )
        }
    }

    @ExperimentalTime
    @ExperimentalCoroutinesApi
    @Test
    fun userViewModel_SignInGoogleNoUserInFirestoreNoDB_ShouldAddUserInFirestoreAndDB() = mainCoroutineRule.runBlockingTest {

        coEvery {
            spyMock.doesUserExistInFirestore(any())
        } returns false
        coEvery {
            spyMock.getAuthDisplayName()
        } returns "Serg"
        coEvery {
            spyMock.getCurrentUserTitleState()
        } returns FakeRepositoryImpl().getCurrentUserTitleState()
        coEvery {
            spyMock.getDBUserData()
        } returns null


        sutViewModel.intentChannel.trySend(UserIntent.SignInGoogle(mockk(relaxed = true)))
        sutViewModel.handleIntent()


        coVerify (exactly = 1){
            spyMock.signInAccountWithGoogle(any())
            spyMock.createUserInFirestore(any(),any())
            spyMock.createUserInDB(any(),any())
        }


        sutViewModel.mainActivityUIState.test {
            assertEquals(
                awaitItem(),
                MainContract.State(
                    MainContract.UserTitleState.Member("Serg"),
                    MainContract.UserProfilePicState.NewProfilePic("http://cdn.shibe.online/shibes/09db03fb29ced77549f07f0de875f3d309f38361.jpg")
                )
            )
        }
    }


    @ExperimentalTime
    @ExperimentalCoroutinesApi
    @Test
    fun userViewModel_SignInGoogleNoUserInFirestoreYesDB_ShouldAddUserOnlyInFirestore() = mainCoroutineRule.runBlockingTest {

        coEvery {
            spyMock.doesUserExistInFirestore(any())
        } returns false
        coEvery {
            spyMock.getAuthDisplayName()
        } returns "Serg"
        coEvery {
            spyMock.getCurrentUserTitleState()
        } returns FakeRepositoryImpl().getCurrentUserTitleState()
        coEvery {
            spyMock.getDBUserData()
        } returns FakeRepositoryImpl().getDBUserData()


        sutViewModel.intentChannel.trySend(UserIntent.SignInGoogle(mockk(relaxed = true)))
        sutViewModel.handleIntent()


        coVerify (exactly = 1){
            spyMock.signInAccountWithGoogle(any())
            spyMock.createUserInFirestore(any(),any())
        }
        coVerify(exactly = 0) {
            spyMock.createUserInDB(any(),any())
        }


        sutViewModel.mainActivityUIState.test {
            assertEquals(
                awaitItem(),
                MainContract.State(
                    MainContract.UserTitleState.Member("Serg"),
                    MainContract.UserProfilePicState.NewProfilePic("http://cdn.shibe.online/shibes/09db03fb29ced77549f07f0de875f3d309f38361.jpg")
                )
            )
        }
    }

    @ExperimentalTime
    @ExperimentalCoroutinesApi
    @Test
    fun userViewModel_SignInGoogleAlreadyUserInFSAndDB_ShouldJustEmitValue() = mainCoroutineRule.runBlockingTest {

        coEvery {
            spyMock.doesUserExistInFirestore(any())
        } returns true
        coEvery {
            spyMock.getAuthDisplayName()
        } returns "Serg"
        coEvery {
            spyMock.getCurrentUserTitleState()
        } returns FakeRepositoryImpl().getCurrentUserTitleState()
        coEvery {
            spyMock.getDBUserData()
        } returns FakeRepositoryImpl().getDBUserData()


        sutViewModel.intentChannel.trySend(UserIntent.SignInGoogle(mockk(relaxed = true)))
        sutViewModel.handleIntent()


        coVerify (exactly = 1){
            spyMock.signInAccountWithGoogle(any())
        }
        coVerify(exactly = 0) {
            spyMock.createUserInFirestore(any(),any())
            spyMock.createUserInDB(any(),any())
        }


        sutViewModel.mainActivityUIState.test {
            assertEquals(
                awaitItem(),
                MainContract.State(
                    MainContract.UserTitleState.Member("Serg"),
                    MainContract.UserProfilePicState.NewProfilePic("http://cdn.shibe.online/shibes/09db03fb29ced77549f07f0de875f3d309f38361.jpg")
                )
            )
        }
    }

    @ExperimentalTime
    @ExperimentalCoroutinesApi
    @Test
    fun userViewModel_SignInGoogleAlreadyUserInFSAndNotDB_ShouldAddUserInDB() = mainCoroutineRule.runBlockingTest {

        coEvery {
            spyMock.doesUserExistInFirestore(any())
        } returns true
        coEvery {
            spyMock.getAuthDisplayName()
        } returns "Serg"
        coEvery {
            spyMock.getCurrentUserTitleState()
        } returns FakeRepositoryImpl().getCurrentUserTitleState()
        coEvery {
            spyMock.getDBUserData()
        } returns null

        sutViewModel.intentChannel.trySend(UserIntent.SignInGoogle(mockk(relaxed = true)))
        sutViewModel.handleIntent()


        coVerify (exactly = 1){
            spyMock.signInAccountWithGoogle(any())
            spyMock.createUserInDB(any(),any())

        }
        coVerify(exactly = 0) {
            spyMock.createUserInFirestore(any(),any())
        }


        sutViewModel.mainActivityUIState.test {
            assertEquals(
                awaitItem(),
                MainContract.State(
                    MainContract.UserTitleState.Member("Serg"),
                    MainContract.UserProfilePicState.NewProfilePic("http://cdn.shibe.online/shibes/09db03fb29ced77549f07f0de875f3d309f38361.jpg")
                )
            )
        }
    }


    @ExperimentalTime
    @ExperimentalCoroutinesApi
    @Test
    fun userViewModel_SignInFacebookNoUserInFirestoreNoDB_ShouldAddUserInFirestoreAndDB() = mainCoroutineRule.runBlockingTest {

        coEvery {
            spyMock.doesUserExistInFirestore(any())
        } returns false
        coEvery {
            spyMock.getAuthDisplayName()
        } returns "Serg"
        coEvery {
            spyMock.getCurrentUserTitleState()
        } returns FakeRepositoryImpl().getCurrentUserTitleState()
        coEvery {
            spyMock.getDBUserData()
        } returns null


        sutViewModel.intentChannel.trySend(UserIntent.FacebookSignIn(mockk(relaxed = true)))
        sutViewModel.handleIntent()


        coVerify (exactly = 1){
            spyMock.signInAccountWithFacebook(any())
            spyMock.createUserInFirestore(any(),any())
            spyMock.createUserInDB(any(),any())
        }


        sutViewModel.mainActivityUIState.test {
            assertEquals(
                awaitItem(),
                MainContract.State(
                    MainContract.UserTitleState.Member("Serg"),
                    MainContract.UserProfilePicState.NewProfilePic("http://cdn.shibe.online/shibes/09db03fb29ced77549f07f0de875f3d309f38361.jpg")
                )
            )
        }
    }


    @ExperimentalTime
    @ExperimentalCoroutinesApi
    @Test
    fun userViewModel_SignInFacebookNoUserInFirestoreYesDB_ShouldAddUserOnlyInFirestore() = mainCoroutineRule.runBlockingTest {

        coEvery {
            spyMock.doesUserExistInFirestore(any())
        } returns false
        coEvery {
            spyMock.getAuthDisplayName()
        } returns "Serg"
        coEvery {
            spyMock.getCurrentUserTitleState()
        } returns FakeRepositoryImpl().getCurrentUserTitleState()
        coEvery {
            spyMock.getDBUserData()
        } returns FakeRepositoryImpl().getDBUserData()


        sutViewModel.intentChannel.trySend(UserIntent.FacebookSignIn(mockk(relaxed = true)))
        sutViewModel.handleIntent()


        coVerify (exactly = 1){
            spyMock.signInAccountWithFacebook(any())
            spyMock.createUserInFirestore(any(),any())
        }
        coVerify(exactly = 0) {
            spyMock.createUserInDB(any(),any())
        }


        sutViewModel.mainActivityUIState.test {
            assertEquals(
                awaitItem(),
                MainContract.State(
                    MainContract.UserTitleState.Member("Serg"),
                    MainContract.UserProfilePicState.NewProfilePic("http://cdn.shibe.online/shibes/09db03fb29ced77549f07f0de875f3d309f38361.jpg")
                )
            )
        }
    }

    @ExperimentalTime
    @ExperimentalCoroutinesApi
    @Test
    fun userViewModel_SignInFacebookAlreadyUserInFSAndDB_ShouldJustEmitValue() = mainCoroutineRule.runBlockingTest {

        coEvery {
            spyMock.doesUserExistInFirestore(any())
        } returns true
        coEvery {
            spyMock.getAuthDisplayName()
        } returns "Serg"
        coEvery {
            spyMock.getCurrentUserTitleState()
        } returns FakeRepositoryImpl().getCurrentUserTitleState()
        coEvery {
            spyMock.getDBUserData()
        } returns FakeRepositoryImpl().getDBUserData()


        sutViewModel.intentChannel.trySend(UserIntent.FacebookSignIn(mockk(relaxed = true)))
        sutViewModel.handleIntent()


        coVerify (exactly = 1){
            spyMock.signInAccountWithFacebook(any())
        }
        coVerify(exactly = 0) {
            spyMock.createUserInFirestore(any(),any())
            spyMock.createUserInDB(any(),any())
        }


        sutViewModel.mainActivityUIState.test {
            assertEquals(
                awaitItem(),
                MainContract.State(
                    MainContract.UserTitleState.Member("Serg"),
                    MainContract.UserProfilePicState.NewProfilePic("http://cdn.shibe.online/shibes/09db03fb29ced77549f07f0de875f3d309f38361.jpg")
                )
            )
        }
    }

    @ExperimentalTime
    @ExperimentalCoroutinesApi
    @Test
    fun userViewModel_SignInFacebookAlreadyUserInFSAndNotDB_ShouldAddUserInDB() = mainCoroutineRule.runBlockingTest {

        coEvery {
            spyMock.doesUserExistInFirestore(any())
        } returns true
        coEvery {
            spyMock.getAuthDisplayName()
        } returns "Serg"
        coEvery {
            spyMock.getCurrentUserTitleState()
        } returns FakeRepositoryImpl().getCurrentUserTitleState()
        coEvery {
            spyMock.getDBUserData()
        } returns null

        sutViewModel.intentChannel.trySend(UserIntent.FacebookSignIn(mockk(relaxed = true)))
        sutViewModel.handleIntent()


        coVerify (exactly = 1){
            spyMock.signInAccountWithFacebook(any())
            spyMock.createUserInDB(any(),any())

        }
        coVerify(exactly = 0) {
            spyMock.createUserInFirestore(any(),any())
        }


        sutViewModel.mainActivityUIState.test {
            assertEquals(
                awaitItem(),
                MainContract.State(
                    MainContract.UserTitleState.Member("Serg"),
                    MainContract.UserProfilePicState.NewProfilePic("http://cdn.shibe.online/shibes/09db03fb29ced77549f07f0de875f3d309f38361.jpg")
                )
            )
        }
    }

    @ExperimentalTime
    @ExperimentalCoroutinesApi
    @Test
    fun userViewModel_GetCurrentUserData_ShouldJustEmitValues() = mainCoroutineRule.runBlockingTest {
        coEvery {
            spyMock.getCurrentUserData()
        } returns FakeRepositoryImpl().getCurrentUserData()


        sutViewModel.intentChannel.trySend(UserIntent.GetProfileData)
        sutViewModel.handleIntent()


        sutViewModel.stateProfilePage.test {
            assertEquals(
                awaitItem(),
                ProfileViewState.LoadedData(
                    UserForFirestore(
                        "test@gmail.com",
                        "Serg"
                    )
                )
            )
        }
    }

    @ExperimentalTime
    @ExperimentalCoroutinesApi
    @Test
    fun userViewModel_UpdateDisplayName_ShouldEmitNewDisplayName() = mainCoroutineRule.runBlockingTest {
        coEvery {
            spyMock.getCurrentUserData()
        } returns UserForFirestore("test@gmail.com", "NewName")


        sutViewModel.intentChannel.trySend(UserIntent.UpdateDisplayName("NewName"))
        sutViewModel.handleIntent()

        coVerify(exactly = 1) {
            spyMock.updateCurrentUserDisplayName("NewName")
        }
        sutViewModel.stateProfilePage.test {
            assertEquals(
                awaitItem(),
                ProfileViewState.LoadedData(
                    UserForFirestore(
                        "test@gmail.com",
                        "NewName"
                    )
                )
            )
        }
    }

    @ExperimentalTime
    @ExperimentalCoroutinesApi
    @Test
    fun userViewModel_PasswordResetEmptyEmail_ShouldEmitError() = mainCoroutineRule.runBlockingTest {

        sutViewModel.intentChannel.trySend(UserIntent.SendResetPassEmail(""))
        sutViewModel.handleIntent()

        coVerify(exactly = 0) {
            spyMock.sendResetEmail(any())
        }
        sutViewModel.stateLoginPage.test {
            assertEquals(
                awaitItem(),
                LoginViewState.Error("Email is empty",LoginViewState.LoginErrorCode.EMPTY_EMAIL)
            )
        }
    }


    @ExperimentalTime
    @ExperimentalCoroutinesApi
    @Test
    fun userViewModel_PasswordResetInvalidEmail_ShouldEmitError() = mainCoroutineRule.runBlockingTest {
        every {
            isValidEmail(any())
        } returns false

        sutViewModel.intentChannel.trySend(UserIntent.SendResetPassEmail("test@gmail.com"))
        sutViewModel.handleIntent()

        coVerify(exactly = 0) {
            spyMock.sendResetEmail(any())
        }
        sutViewModel.stateLoginPage.test {
            assertEquals(
                awaitItem(),
                LoginViewState.Error("Current email is invalid",LoginViewState.LoginErrorCode.INVALID_EMAIL)
            )
        }
    }


    @ExperimentalTime
    @ExperimentalCoroutinesApi
    @Test
    fun userViewModel_PasswordResetGoodEmail_ShouldEmitEmail() = mainCoroutineRule.runBlockingTest {
        every {
            isValidEmail(any())
        } returns true

        sutViewModel.intentChannel.trySend(UserIntent.SendResetPassEmail("test@gmail.com"))
        sutViewModel.handleIntent()

        coVerify(exactly = 1) {
            spyMock.sendResetEmail(any())
        }
        sutViewModel.stateLoginPage.test {
            assertEquals(
                awaitItem(),
                LoginViewState.ResetEmailSent
            )
        }
    }


    @ExperimentalTime
    @ExperimentalCoroutinesApi
    @Test
    fun userViewModel_UpdateUserSettingsError_ShouldEmitError() = mainCoroutineRule.runBlockingTest {
        coEvery {
            spyMock.updateCurrentUserSettings(any(),any(),any())
        } returns "Error"
        sutViewModel.intentChannel.trySend(UserIntent.UpdateUserSettings("test","test","test"))
        sutViewModel.handleIntent()

        coVerify(exactly = 1) {
            spyMock.updateCurrentUserSettings(any(),any(),any())
        }
        sutViewModel.stateProfilePage.test {
            assertEquals(
                awaitItem(),
                ProfileViewState.Error("Error")
            )
        }
    }

    @ExperimentalTime
    @ExperimentalCoroutinesApi
    @Test
    fun userViewModel_UpdateUserSettingsNoError_ShouldEmitThatSettingsIdle() = mainCoroutineRule.runBlockingTest {
        coEvery {
            spyMock.updateCurrentUserSettings(any(),any(),any())
        } returns ""
        sutViewModel.intentChannel.trySend(UserIntent.UpdateUserSettings("test","test","test"))
        sutViewModel.handleIntent()

        coVerify(exactly = 1) {
            spyMock.updateCurrentUserSettings(any(),any(),any())
        }

        sutViewModel.stateProfilePage.test {
            assertEquals(
                awaitItem(),
                ProfileViewState.Idle
            )
        }
    }









}