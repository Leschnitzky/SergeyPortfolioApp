package com.example.sergeyportfolioapp

import android.content.Context
import androidx.fragment.app.FragmentFactory
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.example.sergeyportfolioapp.usermanagement.ui.login.LoginFragment
import com.example.sergeyportfolioapp.util.hasTextInputLayoutErrorText
import com.example.sergeyportfolioapp.util.isToast
import com.example.sergeyportfolioapp.util.launchFragmentInHiltContainer
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
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
class AndroidHiltTest {

    @get:Rule()
    val hiltRule = HiltAndroidRule(this)




    @Before
    fun init() {
        hiltRule.inject()
        launchFragmentInHiltContainer<LoginFragment>(factory = FragmentFactory())

    }

    @Test
    fun TestEmailContainingEmailHint() {
        onView(withId(R.id.emailInputEditText)).check(matches(withHint("Email")));
    }

    @Test
    fun TestEmailContainingPasswordHint() {
        onView(withId(R.id.passwordInputEditText)).check(matches(withHint("Password")));
    }

    @Test
    fun TestPassWrongEmailFormatToEmailField(){
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
    fun TestEmptyPasswordField(){
        onView(withId(R.id.emailInputEditText))
            .perform(typeText("emailgmail.com"))
            .perform(closeSoftKeyboard())
        onView(withId(R.id.button)).perform(click())

        onView(withId(R.id.emailInput)).check(matches(hasTextInputLayoutErrorText("One of the fields is empty")))
    }

    @Test
    fun TestEmptyEmailField(){
        onView(withId(R.id.passwordInputEditText))
            .perform(typeText("test"))
            .perform(closeSoftKeyboard())
        onView(withId(R.id.button)).perform(click())

        onView(withId(R.id.emailInput)).check(matches(hasTextInputLayoutErrorText("One of the fields is empty")))
    }

    @Test
    fun TestWrongCredentials(){
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
    fun TestCorrectCredentials(){
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