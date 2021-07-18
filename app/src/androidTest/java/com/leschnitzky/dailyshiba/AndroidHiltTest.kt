package com.leschnitzky.dailyshiba

import androidx.fragment.app.FragmentFactory
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import com.leschnitzky.dailyshiba.usermanagement.ui.login.LoginFragment
import com.leschnitzky.dailyshiba.util.hasTextInputLayoutErrorText
import com.leschnitzky.dailyshiba.util.isToast
import com.leschnitzky.dailyshiba.util.launchFragmentInHiltContainer
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@HiltAndroidTest
@LargeTest
class LoginActivityTest {

    @get:Rule()
    val hiltRule = HiltAndroidRule(this)




    @Before
    fun init() {
        hiltRule.inject()
        launchFragmentInHiltContainer<LoginFragment>(factory = FragmentFactory())

    }

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

        onView(withId(R.id.emailInput)).check(matches(hasTextInputLayoutErrorText("One of the fields is empty")))
    }

    @Test
    fun testEmptyEmailField(){
        onView(withId(R.id.passwordInputEditText))
            .perform(typeText("test"))
            .perform(closeSoftKeyboard())
        onView(withId(R.id.button)).perform(click())

        onView(withId(R.id.emailInput)).check(matches(hasTextInputLayoutErrorText("One of the fields is empty")))
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

        onView(withId(R.id.emailInput)).check(matches(hasTextInputLayoutErrorText("no_user")))
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

        onView(withText("Hello, test@gmail.com!")).inRoot(isToast()).check(matches(isDisplayed()))
    }


}