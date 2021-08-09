package com.leschnitzky.dailyshiba.fragments

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import app.cash.turbine.test
import com.leschnitzky.dailyshiba.R
import com.leschnitzky.dailyshiba.UserIntent
import com.leschnitzky.dailyshiba.usermanagement.ui.UserViewModel
import com.leschnitzky.dailyshiba.usermanagement.ui.login.LoginFragment
import com.leschnitzky.dailyshiba.usermanagement.ui.login.viewstate.LoginViewState
import com.leschnitzky.dailyshiba.util.hasTextInputLayoutErrorText
import com.leschnitzky.dailyshiba.util.launchFragmentInHiltContainer
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@HiltAndroidTest
@LargeTest
class LoginFragmentTest {


    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @BindValue
    val mockViewModel= mockk<UserViewModel>(relaxed = true)

    val testMutableStateFlow = MutableStateFlow<LoginViewState>(LoginViewState.Idle)
    var testStateFlow : StateFlow<LoginViewState> = testMutableStateFlow.asStateFlow()

    var fragment : LoginFragment? = null
    @Before
    fun init() {
        every { mockViewModel.stateLoginPage } answers { testStateFlow }
        hiltRule.inject()
        fragment = launchFragmentInHiltContainer<LoginFragment>() as LoginFragment

    }

    @ExperimentalCoroutinesApi
    @Test
    fun loginFragment_EveryViewDisplayed_shouldNotFail(){
        onView(withId(R.id.login_greeting_animation_lottie)).check(matches(isDisplayed()))
        onView(withId(R.id.login_welcome_text_view)).check(matches(isDisplayed()))

        onView(withId(R.id.login_google_sign_in_button)).check(matches(isDisplayed()))
        onView(withId(R.id.login_facebook_sign_in_button)).check(matches(isDisplayed()))

        onView(withId(R.id.login_email_input_layout)).check(matches(isDisplayed()))
        onView(withId(R.id.login_password_input_layout)).check(matches(isDisplayed()))

        onView(withId(R.id.login_login_button)).check(matches(isDisplayed()))

        onView(withId(R.id.login_forgot_password_button)).check(matches(isDisplayed()))
        onView(withId(R.id.login_register_button)).check(matches(isDisplayed()))
        onView(withId(R.id.login_terms_and_conds_button)).check(matches(isDisplayed()))

    }

    @Test
    fun testEmailContainingEmailHint() {
        onView(withId(R.id.login_email_input_edit_text)).check(matches(withHint("Email")));
    }

    @Test
    fun testEmailContainingPasswordHint() {
        onView(withId(R.id.login_password_input_edit_text)).check(matches(withHint("Password")));
    }


//
//    @ExperimentalTime
//    @Test
//    fun loginFragment_ClickingLoginButton_SendsIntentToVM() = runBlockingTest {
//        var testChannel = Channel<UserIntent>(Channel.UNLIMITED)
//        every { mockViewModel.intentChannel } answers { testChannel }
//
//        onView(withId(R.id.login_login_button)).perform(click())
//
//        mockViewModel.intentChannel.consumeAsFlow().test{
//            assertEquals(
//                UserIntent.Login("",""),
//                awaitItem()
//            )
//        }
//    }
//
//    @ExperimentalTime
//    @Test
//    fun loginFragment_ClickingFacebookButton_StartsLoginManager() = runBlockingTest {
//        onView(withId(R.id.login_facebook_sign_in_button)).perform(click())
//
////        verify(exactly = 1) { fragment!!.mLoginManager.logInWithReadPermissions(fragment!!,listOf("email", "public_profile")) }
//    }
//
//
//    @ExperimentalTime
//    @ExperimentalCoroutinesApi
//    @Test
//    fun loginFragment_VMSendingEmptyEmailError_DisplayError() = runBlockingTest {
//        testStateFlow.test {
//            testMutableStateFlow.emit( LoginViewState.Error(
//                "The following field is empty",
//                LoginViewState.LoginErrorCode.EMPTY_EMAIL
//                )
//            )
//            cancelAndConsumeRemainingEvents()
//        }
//
//
//        onView(withId(R.id.login_email_input_layout))
//            .check(
//                matches(
//                    hasTextInputLayoutErrorText("The following field is empty")
//                )
//            )
//    }
//
//    @ExperimentalTime
//    @ExperimentalCoroutinesApi
//    @Test
//    fun loginFragment_VMSendingEmptyPassword_DisplayError() = runBlockingTest {
//        testStateFlow.test {
//            testMutableStateFlow.emit( LoginViewState.Error(
//                "Test",
//                LoginViewState.LoginErrorCode.EMPTY_PASSWORD
//            )
//            )
//            cancelAndConsumeRemainingEvents()
//        }
//
//
//        onView(withId(R.id.login_password_input_layout))
//            .check(
//                matches(
//                    hasTextInputLayoutErrorText("Test")
//                )
//            )
//    }
//
//    @ExperimentalTime
//    @ExperimentalCoroutinesApi
//    @Test
//    fun loginFragment_VMSendingFirebaseError_DisplayError() = runBlockingTest {
//        testStateFlow.test {
//            testMutableStateFlow.emit( LoginViewState.Error(
//                "Test",
//                LoginViewState.LoginErrorCode.FIREBASE_ERROR
//            )
//            )
//            cancelAndConsumeRemainingEvents()
//        }
//
//
//        onView(withId(R.id.login_email_input_layout))
//            .check(
//                matches(
//                    hasTextInputLayoutErrorText("Test")
//                )
//            )
//    }
//
//
//
//    @ExperimentalTime
//    @ExperimentalCoroutinesApi
//    @Test
//    fun loginFragment_VMSendingWrongEmailFormatError_DisplayError() = runBlockingTest {
//        testStateFlow.test {
//            testMutableStateFlow.emit( LoginViewState.Error(
//                "Test",
//                LoginViewState.LoginErrorCode.INVALID_EMAIL
//            )
//            )
//            cancelAndConsumeRemainingEvents()
//        }
//
//
//        onView(withId(R.id.login_email_input_layout))
//            .check(
//                matches(
//                    hasTextInputLayoutErrorText("Test")
//                )
//            )
//    }
//
//    @ExperimentalTime
//    @Test
//    fun login_VMSendsLoadingState_ShouldDisplayLogin() = runBlockingTest{
//        testStateFlow.test {
//            testMutableStateFlow.emit( LoginViewState.Loading
//            )
//            cancelAndConsumeRemainingEvents()
//        }
//
//
//        onView(withId(R.id.login_greeting_animation_lottie))
//            .check(
//                matches(
//                    isDisplayed()
//                )
//            )
//    }




}