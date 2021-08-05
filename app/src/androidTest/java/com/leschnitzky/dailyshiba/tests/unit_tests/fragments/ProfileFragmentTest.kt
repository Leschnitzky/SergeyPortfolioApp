package com.leschnitzky.dailyshiba.tests.unit_tests.fragments

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.LargeTest
import com.leschnitzky.dailyshiba.R
import com.leschnitzky.dailyshiba.usermanagement.ui.profile.ProfileFragment
import com.leschnitzky.dailyshiba.util.launchFragmentInHiltContainer
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@LargeTest
@HiltAndroidTest
class ProfileFragmentTest {


    @get:Rule()
    val hiltRule = HiltAndroidRule(this)

    var fragment: ProfileFragment? = null

    @Before
    fun init() {

        hiltRule.inject()
        fragment = launchFragmentInHiltContainer<ProfileFragment>() as ProfileFragment

    }

    @Test
    fun profileFragment_isEveryViewDisplayed() {
        onView(withId(R.id.textView2)).check(matches(isDisplayed()))
        onView(withId(R.id.textView4)).check(matches(isDisplayed()))
        onView(withId(R.id.profile_shiba_toggle)).check(matches(isDisplayed()))
        onView(withId(R.id.profile_husky_toggle)).check(matches(isDisplayed()))
        onView(withId(R.id.profile_shiba)).check(matches(isDisplayed()))
        onView(withId(R.id.profile_shiba2)).check(matches(isDisplayed()))
        onView(withId(R.id.profile_shiba3)).check(matches(isDisplayed()))
        onView(withId(R.id.profile_shiba4)).check(matches(isDisplayed()))
        onView(withId(R.id.profile_beagle_toggle)).check(matches(isDisplayed()))
        onView(withId(R.id.profile_corgi_toggle)).check(matches(isDisplayed()))
    }
}