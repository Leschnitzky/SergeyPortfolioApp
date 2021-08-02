package com.leschnitzky.dailyshiba.tests.fragments

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId

import androidx.test.filters.LargeTest
import com.leschnitzky.dailyshiba.R
import com.leschnitzky.dailyshiba.usermanagement.ui.main.ShibaFragment
import com.leschnitzky.dailyshiba.usermanagement.ui.profile.ProfileFragment
import com.leschnitzky.dailyshiba.usermanagement.ui.register.RegisterFragment
import com.leschnitzky.dailyshiba.util.hasTextInputLayoutErrorText
import com.leschnitzky.dailyshiba.util.launchFragmentInHiltContainer
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
@LargeTest
class RegisterFragmentTest {

    @get:Rule()
    val hiltRule = HiltAndroidRule(this)

    var registerFragment : RegisterFragment? = null
    @ExperimentalCoroutinesApi
    @Before
    fun init() {

        hiltRule.inject()
        registerFragment = launchFragmentInHiltContainer<RegisterFragment>() as RegisterFragment

    }

    @Test
    fun registerFragment_areViewsDisplayed_ShouldReturnTrue(){
        onView(withId(R.id.register_page_email_layout)).check(matches(isDisplayed()))
        onView(withId(R.id.register_page_password_layout)).check(matches(isDisplayed()))
        onView(withId(R.id.register_page_name_layout)).check(matches(isDisplayed()))
        onView(withId(R.id.register_done)).check(matches(isDisplayed()))
        onView(withId(R.id.radioButton)).check(matches(isDisplayed()))
    }

    @Test
    fun registerFragment_EmptyFullName_ShouldDisplayError(){
        onView(withId(R.id.register_page_email_layout_input_text))
            .perform(typeText("email@gmail.com"))
            .perform(closeSoftKeyboard())
        onView(withId(R.id.register_page_password_layout_edit_text))
            .perform(typeText("123456"))
            .perform(closeSoftKeyboard())
        onView(withId(R.id.radioButton))
            .perform(click())

        onView(withId(R.id.register_done)).perform(click())

        onView(withId(R.id.register_page_name_layout))
            .check(
                matches(
                    hasTextInputLayoutErrorText("The following field is empty")
                )
            )
    }

    @Test
    fun registerFragment_EmptyEmail_ShouldDisplayError(){
        onView(withId(R.id.register_page_name_layout_edit_text))
            .perform(typeText("emailgmail.com"))
            .perform(closeSoftKeyboard())
        onView(withId(R.id.register_page_password_layout_edit_text))
            .perform(typeText("123456"))
            .perform(closeSoftKeyboard())
        onView(withId(R.id.radioButton))
            .perform(click())
            .perform(click())

        onView(withId(R.id.register_done)).perform(click())

        onView(withId(R.id.register_page_email_layout))
            .check(
                matches(
                    hasTextInputLayoutErrorText("The following field is empty")
                )
            )
    }

    @Test
    fun registerFragment_EmptyPassword_ShouldDisplayError(){
        onView(withId(R.id.register_page_name_layout_edit_text))
            .perform(typeText("test"))
            .perform(closeSoftKeyboard())
        onView(withId(R.id.register_page_email_layout_input_text))
            .perform(typeText("email@gmail.com"))
            .perform(closeSoftKeyboard())
        onView(withId(R.id.radioButton))
            .perform(click())
            .perform(click())

        onView(withId(R.id.register_done)).perform(click())

        onView(withId(R.id.register_page_password_layout))
            .check(
                matches(
                    hasTextInputLayoutErrorText("The following field is empty")
                )
            )
    }

    @Test
    fun registerFragment_DidNotCheckTerms_ShouldDisplayError(){
        onView(withId(R.id.register_page_email_layout_input_text))
            .perform(typeText("email@gmail.com"))
            .perform(closeSoftKeyboard())
        onView(withId(R.id.register_page_name_layout_edit_text))
            .perform(typeText("test"))
            .perform(closeSoftKeyboard())
        onView(withId(R.id.register_page_password_layout_edit_text))
            .perform(typeText("123456"))
            .perform(closeSoftKeyboard())


        onView(withId(R.id.register_done)).perform(click())

        onView(withId(R.id.register_page_name_layout))
            .check(
                matches(
                    hasTextInputLayoutErrorText("Terms and conditions are not checked")
                )
            )
    }


    @Test
    fun registerFragment_EmailNotValid_ShouldDisplayError(){
        onView(withId(R.id.register_page_email_layout_input_text))
            .perform(typeText("emailgmail.com"))
            .perform(closeSoftKeyboard())
        onView(withId(R.id.register_page_name_layout_edit_text))
            .perform(typeText("test"))
            .perform(closeSoftKeyboard())
        onView(withId(R.id.register_page_password_layout_edit_text))
            .perform(typeText("123456"))
            .perform(closeSoftKeyboard())
        onView(withId(R.id.radioButton))
            .perform(click())
            .perform(click())


        onView(withId(R.id.register_done)).perform(click())

        onView(withId(R.id.register_page_email_layout))
            .check(
                matches(
                    hasTextInputLayoutErrorText("Email is invalid")
                )
            )
    }


    @Test
    fun registerFragment_AllDetailsValid_ShouldNotDisplayError(){
        onView(withId(R.id.register_page_email_layout_input_text))
            .perform(typeText("email2@gmail.com"))
            .perform(closeSoftKeyboard())
        onView(withId(R.id.register_page_name_layout_edit_text))
            .perform(typeText("test"))
            .perform(closeSoftKeyboard())
        onView(withId(R.id.register_page_password_layout_edit_text))
            .perform(typeText("123456"))
            .perform(closeSoftKeyboard())
        onView(withId(R.id.radioButton))
            .perform(click())
            .perform(click())


        onView(withId(R.id.register_done)).perform(click())

        onView(withId(R.id.register_page_email_layout))
            .check(
                    matches(
                        not(
                            hasTextInputLayoutErrorText("Email is invalid")

                        )
                    )
            )
    }
}