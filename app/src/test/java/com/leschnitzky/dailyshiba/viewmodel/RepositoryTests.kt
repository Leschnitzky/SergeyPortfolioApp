package com.leschnitzky.dailyshiba.viewmodel

import com.google.android.gms.auth.api.Auth
import com.google.firebase.auth.FirebaseUser
import com.leschnitzky.dailyshiba.TestCoroutineRule
import com.leschnitzky.dailyshiba.di.TestCoroutineContextProvider
import com.leschnitzky.dailyshiba.runBlockingTest
import com.leschnitzky.dailyshiba.usermanagement.repository.Repository
import com.leschnitzky.dailyshiba.usermanagement.repository.RepositoryImpl
import com.leschnitzky.dailyshiba.usermanagement.repository.firebaseauth.AuthRepository
import com.leschnitzky.dailyshiba.usermanagement.repository.firebaseauth.model.UserForFirebase
import com.leschnitzky.dailyshiba.usermanagement.repository.firestore.FirestoreRepository
import com.leschnitzky.dailyshiba.usermanagement.repository.firestore.model.UserForFirestore
import com.leschnitzky.dailyshiba.usermanagement.repository.retrofit.RetrofitRepository
import com.leschnitzky.dailyshiba.usermanagement.repository.room.UserDao
import com.leschnitzky.dailyshiba.usermanagement.repository.room.UserDao_Impl
import com.leschnitzky.dailyshiba.usermanagement.repository.room.model.User
import com.leschnitzky.dailyshiba.usermanagement.ui.UserViewModel
import com.leschnitzky.dailyshiba.utils.CoroutineScopeProviderImpl
import io.mockk.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.streams.toList
import kotlin.test.assertEquals

@RunWith(JUnit4::class)
class RepositoryTests {
    lateinit var sutRepository : Repository
    @get:Rule
    val mainCoroutineRule = TestCoroutineRule()

    val testCoroutineContextProvider : TestCoroutineContextProvider = TestCoroutineContextProvider(mainCoroutineRule.testDispatcher)
    val mockAuth : AuthRepository = mockk(relaxed = true)
    val spyAuth = spyk(mockAuth)
    val mockDB : UserDao = mockk(relaxed = true)
    val spyDB = spyk(mockDB)
    val mockFS :FirestoreRepository = mockk(relaxed = true)
    val spyFS = spyk(mockFS)
    val mockRF : RetrofitRepository = mockk(relaxed = true)
    val spyRF = spyk(mockRF)

    val mockFirebaseUser :FirebaseUser = mockk(relaxed = true)
    var spyFirebaseUser = spyk(mockFirebaseUser)

    val mockFirestoreUser :UserForFirestore = mockk(relaxed = true)
    var spyFirestoreUser = spyk(mockFirestoreUser)

    @Before
    fun setup(){

        sutRepository = RepositoryImpl(
            testCoroutineContextProvider,
            spyDB,
            spyAuth,
            spyFS,
            spyRF
            )
    }

    @Test
    fun repository_loginAndReturnNameUserAlreadyInDB_ShouldReturnName() = mainCoroutineRule.runBlockingTest{
        coEvery {
            spyFirebaseUser.email
        } returns "serg@gmail.com"
        coEvery {
            spyAuth.getCurrentUser()
        } returns spyFirebaseUser
        coEvery {
            spyDB.getDisplayNameByEmail(any())
        } returns listOf(User("serg@gmail.com","Serg", arrayListOf(), mapOf()))

        val result = sutRepository.loginUserAndReturnName("get", "good")


        assertEquals(result,"Serg")
    }

    @Test
    fun repository_loginAndReturnNameUserNotInDB_ShouldReturnName() = mainCoroutineRule.runBlockingTest{
        coEvery {
            spyFirebaseUser.email
        } returns "serg@gmail.com"
        coEvery {
            spyAuth.getCurrentUser()
        } returns spyFirebaseUser
        coEvery {
            spyDB.getDisplayNameByEmail(any())
        } returns listOf()
        coEvery {
            spyAuth.getUserDisplayName()
        } returns "Serg"
        val result = sutRepository.loginUserAndReturnName("get", "good")

        coVerify(exactly = 1) {
            spyDB.insertUser(any())
        }
        assertEquals(result,"Serg")
    }


    @Test
    fun repository_createUser_ShouldAddUserToFirestoreandDB() = mainCoroutineRule.runBlockingTest{

        sutRepository.createUser("get", "good","great")

        coVerify(exactly = 1) {
            spyDB.insertUser(any())
            spyFS.addNewUserToFirestore(any(),any())
        }
    }

    @Test
    fun repository_Logout_RunsTheCorrectMethod() = mainCoroutineRule.runBlockingTest {
        sutRepository.logoutUser()

        verify(exactly = 1) {
            spyAuth.logOffFromCurrentUser()
        }
    }

    @Test
    fun repositry_GetCurrentUserEmailNotSigned_ShouldReturnNull() {

        every {
            spyFirebaseUser.email
        } returns null

        every {
            spyAuth.getCurrentUser()
        } returns spyFirebaseUser

        assertEquals(sutRepository.getCurrentUserEmail(), null)
    }


    @Test
    fun repositry_GetCurrentUserEmailGotMail_ShouldReturnMail() {

        every {
            spyFirebaseUser.email
        } returns "serg@gmail.com"

        every {
            spyAuth.getCurrentUser()
        } returns spyFirebaseUser

        assertEquals(sutRepository.getCurrentUserEmail(), "serg@gmail.com")
    }

    @Test
    fun repository_GetPhotosFromUserWithNoPhotosInDB_ShouldAddSaveLocally() = mainCoroutineRule.runBlockingTest {
        every {
            spyFirestoreUser.currentPhotosList
        } returns arrayListOf("not empty")

        every {
            spyFirebaseUser.email
        } returns "serg@gmail.com"
        coEvery {
            spyDB.getCurrentPhotosByEmail(any())
        } returns arrayListOf()

        coEvery {
            spyAuth.getCurrentUser()
        } returns spyFirebaseUser
        coEvery {
            spyFS.getUserFromFirestore(any())
        } returns spyFirestoreUser

        val result = sutRepository.getCurrentUserPhotos()
        assertEquals(result[0],"SaveLocally")
    }


    @Test
    fun repository_GetPhotosFromUserWithNoPhotosInFS_ShouldCallNewPhotos() = mainCoroutineRule.runBlockingTest {
        every {
            spyFirestoreUser.currentPhotosList
        } returns arrayListOf()

        coEvery {
            spyRF.getShibaPhotos(any())
        } returns arrayListOf("a","b","c","d")

        every {
            spyFirestoreUser.userSettings
        } returns UserForFirestore.UserSettingsForFirestore.DEFAULT_USER_SETTINGS

        every {
            spyFirebaseUser.email
        } returns "serg@gmail.com"
        coEvery {
            spyDB.getCurrentPhotosByEmail(any())
        } returns arrayListOf()

        coEvery {
            spyAuth.getCurrentUser()
        } returns spyFirebaseUser
        coEvery {
            spyFS.getUserFromFirestore(any())
        } returns spyFirestoreUser

        sutRepository.getCurrentUserPhotos()

        coVerify(exactly = 1) {
            spyRF.getShibaPhotos(9)
        }
    }

}