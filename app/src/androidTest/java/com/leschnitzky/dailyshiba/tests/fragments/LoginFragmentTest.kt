package com.leschnitzky.dailyshiba.tests.fragments

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import com.leschnitzky.dailyshiba.R
import com.leschnitzky.dailyshiba.usermanagement.ui.login.LoginFragment
import com.leschnitzky.dailyshiba.util.hasTextInputLayoutErrorText
import com.leschnitzky.dailyshiba.util.launchFragmentInHiltContainer
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.core.IsNot.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import timber.log.Timber
import kotlin.test.assertEquals


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@HiltAndroidTest
@LargeTest
class LoginFragmentTest {


    @get:Rule()
    val hiltRule = HiltAndroidRule(this)

    var fragment : LoginFragment? = null
    @Before
    fun init() {

        hiltRule.inject()
        fragment = launchFragmentInHiltContainer<LoginFragment>() as LoginFragment

    }

    @ExperimentalCoroutinesApi
    @Test
    fun testGoogleAndFacebookButtonsAreDisplayed(){
        onView(withId(R.id.google_sign_in)).check(matches(isDisplayed()))
        onView(withId(R.id.facebook_sign_in)).check(matches(isDisplayed()))
    }

    @Test
    fun testEmailContainingEmailHint() {

        onView(withId(R.id.emailInputEditText)).check(matches(withHint("Email")));
    }

    @Test
    fun testEmailContainingPasswordHint() {

        onView(withId(R.id.passwordInputEditText)).check(matches(withHint("Password")));
    }

    @Test
    fun testPassWrongEmailFormatToEmailField(){
        onView(withId(R.id.emailInputEditText))
            .perform(typeText("emailgmail.com"))
            .perform(closeSoftKeyboard())
        onView(withId(R.id.passwordInputEditText))
            .perform(typeText("test"))
            .perform(closeSoftKeyboard())

        onView(withId(R.id.button)).perform(click())

        onView(withId(R.id.emailInput)).check(matches(hasTextInputLayoutErrorText("Email is invalid")))
    }

    @Test
    fun testEmptyPasswordField(){
        onView(withId(R.id.emailInputEditText))
            .perform(typeText("emailgmail.com"))
            .perform(closeSoftKeyboard())
        onView(withId(R.id.button)).perform(click())

        onView(withId(R.id.passwordInput)).check(matches(hasTextInputLayoutErrorText("The following field is empty")))
    }

    @Test
    fun testEmptyEmailField(){
        onView(withId(R.id.passwordInputEditText))
            .perform(typeText("test"))
            .perform(closeSoftKeyboard())

        onView(withId(R.id.button)).perform(click())

        onView(withId(R.id.emailInput)).check(matches(hasTextInputLayoutErrorText("The following field is empty")))
    }

    @Test
    fun testWrongCredentials(){
        onView(withId(R.id.emailInputEditText))
            .perform(typeText("test@gmail.com"))
            .perform(closeSoftKeyboard())
        onView(withId(R.id.passwordInputEditText))
            .perform(typeText("test"))
            .perform(closeSoftKeyboard())

        onView(withId(R.id.button)).perform(click())

        onView(withId(R.id.emailInput))
            .check(
                matches(
                    hasTextInputLayoutErrorText("There is no user record corresponding to this identifier. The user may have been deleted.")))
    }

    @Test
    fun testCorrectCredentials(){

        onView(withId(R.id.emailInputEditText))
            .perform(typeText("test@gmail.com"))
            .perform(closeSoftKeyboard())
        onView(withId(R.id.passwordInputEditText))
            .perform(typeText("test123"))
            .perform(closeSoftKeyboard())

        onView(withId(R.id.button)).perform(click())


        assertEquals(fragment!!.userViewModel.getCurrentUserEmail(), "test@gmail.com")



    }


}