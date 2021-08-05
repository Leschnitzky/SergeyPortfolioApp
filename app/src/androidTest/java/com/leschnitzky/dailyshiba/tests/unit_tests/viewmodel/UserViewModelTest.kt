package com.leschnitzky.dailyshiba.tests.unit_tests.viewmodel

import androidx.test.filters.LargeTest
import com.leschnitzky.dailyshiba.usermanagement.ui.UserViewModel
import com.leschnitzky.dailyshiba.usermanagement.ui.login.LoginFragment
import com.leschnitzky.dailyshiba.util.launchFragmentInHiltContainer
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@LargeTest
@HiltAndroidTest
class UserViewModelTest {


    @get:Rule()
    val hiltRule = HiltAndroidRule(this)

    var fragment: LoginFragment? = null
    var userViewModel : UserViewModel? = null

    @ExperimentalCoroutinesApi
    @Before
    fun init() {

        hiltRule.inject()
        fragment = (launchFragmentInHiltContainer<LoginFragment>() as LoginFragment)
        userViewModel = fragment!!.userViewModel!!

    }


    @Test
    fun testHistory(){
        every {

        }

    }


}