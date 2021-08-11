package com.leschnitzky.dailyshiba.viewmodel

import app.cash.turbine.test
import com.leschnitzky.dailyshiba.MainContract
import com.leschnitzky.dailyshiba.TestCoroutineRule
import com.leschnitzky.dailyshiba.UserIntent
import com.leschnitzky.dailyshiba.di.FakeRepositoryImpl
import com.leschnitzky.dailyshiba.di.TestCoroutineContextProvider
import com.leschnitzky.dailyshiba.runBlockingTest
import com.leschnitzky.dailyshiba.usermanagement.repository.Repository
import com.leschnitzky.dailyshiba.usermanagement.ui.UserViewModel
import com.leschnitzky.dailyshiba.utils.CoroutineScopeProviderImpl
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime

@RunWith(JUnit4::class)
class UserViewModelTest {

    @get:Rule
    val mainCoroutineRule = TestCoroutineRule()


    lateinit var sutViewModel : UserViewModel
    var mockRepository: Repository = mockk()
    val testCoroutineContextProvider : TestCoroutineContextProvider = TestCoroutineContextProvider(mainCoroutineRule.testDispatcher)
    val testScope = TestCoroutineScope(mainCoroutineRule.testDispatcher)



    @ExperimentalTime
    @ExperimentalCoroutinesApi
    @Test
    fun userViewModel_CheckLogin_ShouldEmitCorrectValues() = mainCoroutineRule.runBlockingTest {
        val mock = spyk(mockRepository)
        coEvery { mock.getCurrentUserTitleState() }.returns(FakeRepositoryImpl().getCurrentUserTitleState())
        sutViewModel = UserViewModel(mock, testCoroutineContextProvider,
            CoroutineScopeProviderImpl(
            testScope
        ))

        sutViewModel.intentChannel.send(UserIntent.CheckLogin)

        sutViewModel.mainActivityUIState.test {
            assertEquals(
                awaitItem(),
                MainContract.State(
                    MainContract.UserTitleState.InitState,
                    MainContract.UserProfilePicState.InitState)
                )

        }


    }


    @Test
    fun userViewModel_GetDisplayNameWhenLoggedOff_ReturnsUnsigned(){
        assertEquals("tes2t", "tes2t")
    }
}