package com.leschnitzky.dailyshiba.tests.UITest.fragments

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId

import androidx.test.filters.LargeTest
import app.cash.turbine.test
import com.leschnitzky.dailyshiba.R
import com.leschnitzky.dailyshiba.UserIntent
import com.leschnitzky.dailyshiba.usermanagement.ui.UserViewModel
import com.leschnitzky.dailyshiba.usermanagement.ui.register.RegisterFragment
import com.leschnitzky.dailyshiba.usermanagement.ui.register.viewstate.RegisterViewState
import com.leschnitzky.dailyshiba.util.hasTextInputLayoutErrorText
import com.leschnitzky.dailyshiba.util.launchFragmentInHiltContainer
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime

@HiltAndroidTest
@LargeTest
class RegisterFragmentTest {

    @get:Rule()
    val hiltRule = HiltAndroidRule(this)

    @BindValue
    val mockViewModel= mockk<UserViewModel>(relaxed = true)

    val testMutableStateFlow = MutableStateFlow<RegisterViewState>(RegisterViewState.Idle)
    var testStateFlow : StateFlow<RegisterViewState> = testMutableStateFlow.asStateFlow()

    var registerFragment : RegisterFragment? = null
    @ExperimentalCoroutinesApi
    @Before
    fun init() {
        every { mockViewModel.stateRegisterPage } answers { testStateFlow }
        hiltRule.inject()
        registerFragment = launchFragmentInHiltContainer<RegisterFragment>() as RegisterFragment
    }



    @Test
    fun registerFragment_AllViewsDisplayed(){
        onView(withId(R.id.register_greeting_text_view)).check(matches(isDisplayed()))

        onView(withId(R.id.register_page_name_layout)).check(matches(isDisplayed()))
        onView(withId(R.id.register_page_email_layout)).check(matches(isDisplayed()))
        onView(withId(R.id.register_page_password_layout)).check(matches(isDisplayed()))

        onView(withId(R.id.register_terms_radio_button)).check(matches(isDisplayed()))

        onView(withId(R.id.register_complete_button)).check(matches(isDisplayed()))
    }

    @Test
    fun register_EmailHint_ShouldBeDisplayed() {
        onView(withId(R.id.register_page_email_layout_input_text)).check(matches(ViewMatchers.withHint("Email")));
    }

    @Test
    fun register_NameHint_ShouldBeDisplayed() {
        onView(withId(R.id.register_page_name_layout_edit_text)).check(matches(ViewMatchers.withHint("Full name")));
    }
    @Test
    fun register_PasswordHint_ShouldBeDisplayed() {
        onView(withId(R.id.register_page_password_layout_edit_text)).check(matches(ViewMatchers.withHint("Password")));
    }

    @ExperimentalTime
    @Test
    fun registerFragment_registerButtonSendsIntent_ShouldSend() = runBlockingTest {
        var testChannel = Channel<UserIntent>(Channel.UNLIMITED)
        every { mockViewModel.intentChannel } answers { testChannel }

        onView(withId(R.id.register_complete_button)).perform(click())

        mockViewModel.intentChannel.consumeAsFlow().test {
            assertEquals(
                UserIntent.Register("","","",false),
                awaitItem()
            )
        }
    }

    @ExperimentalTime
    @Test
    fun registerFragment_VMSendsInvalidEmail_ShouldDisplayError() = runBlockingTest {
        testStateFlow.test {
            testMutableStateFlow.emit( RegisterViewState.Error(
                "Test",
                RegisterViewState.RegisterErrorCode.INVALID_EMAIL
            )
            )
            cancelAndConsumeRemainingEvents()
        }


        onView(withId(R.id.register_page_email_layout))
            .check(
                matches(
                    hasTextInputLayoutErrorText("Test")
                )
            )
    }


    @ExperimentalTime
    @Test
    fun registerFragment_VMSendsFirebaseError_ShouldDisplayError() = runBlockingTest {
        testStateFlow.test {
            testMutableStateFlow.emit( RegisterViewState.Error(
                "Test",
                RegisterViewState.RegisterErrorCode.FIREBASE_ERROR
            )
            )
            cancelAndConsumeRemainingEvents()
        }


        onView(withId(R.id.register_page_name_layout))
            .check(
                matches(
                    hasTextInputLayoutErrorText("Test")
                )
            )
    }

    @ExperimentalTime
    @Test
    fun registerFragment_VMSendsEmptyEmailError_ShouldDisplayError() = runBlockingTest {
        testStateFlow.test {
            testMutableStateFlow.emit( RegisterViewState.Error(
                "Test",
                RegisterViewState.RegisterErrorCode.EMPTY_EMAIL
            )
            )
            cancelAndConsumeRemainingEvents()
        }


        onView(withId(R.id.register_page_email_layout))
            .check(
                matches(
                    hasTextInputLayoutErrorText("Test")
                )
            )
    }

    @ExperimentalTime
    @Test
    fun registerFragment_VMSendsEmptyPasswordError_ShouldDisplayError() = runBlockingTest {
        testStateFlow.test {
            testMutableStateFlow.emit( RegisterViewState.Error(
                "Test",
                RegisterViewState.RegisterErrorCode.EMPTY_PASSWORD
            )
            )
            cancelAndConsumeRemainingEvents()
        }


        onView(withId(R.id.register_page_password_layout))
            .check(
                matches(
                    hasTextInputLayoutErrorText("Test")
                )
            )
    }

    @ExperimentalTime
    @Test
    fun registerFragment_VMSendsEmptyNameError_ShouldDisplayError() = runBlockingTest {
        testStateFlow.test {
            testMutableStateFlow.emit( RegisterViewState.Error(
                "Test",
                RegisterViewState.RegisterErrorCode.EMPTY_NAME
            )
            )
            cancelAndConsumeRemainingEvents()
        }


        onView(withId(R.id.register_page_name_layout))
            .check(
                matches(
                    hasTextInputLayoutErrorText("Test")
                )
            )
    }

    @ExperimentalTime
    @Test
    fun registerFragment_VMSendsNotAcceptTermsError_ShouldDisplayError() = runBlockingTest {
        testStateFlow.test {
            testMutableStateFlow.emit( RegisterViewState.Error(
                "Test",
                RegisterViewState.RegisterErrorCode.DIDNT_ACCEPT_TERMS
            )
            )
            cancelAndConsumeRemainingEvents()
        }


        onView(withId(R.id.register_page_name_layout))
            .check(
                matches(
                    hasTextInputLayoutErrorText("Test")
                )
            )
    }

    @ExperimentalTime
    @Test
    fun register_VMEmitsLoading_ShouldDisplayLoadingAnimation() = runBlockingTest {
        testStateFlow.test {
            testMutableStateFlow.emit( RegisterViewState.Loading
            )
            cancelAndConsumeRemainingEvents()
        }


        onView(withId(R.id.register_page_loading_animation))
            .check(
                matches(
                   isDisplayed()
                )
            )
    }



}