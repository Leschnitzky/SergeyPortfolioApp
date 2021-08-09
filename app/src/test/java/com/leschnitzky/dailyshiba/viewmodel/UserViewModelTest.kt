package com.leschnitzky.dailyshiba.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.test.runner.AndroidJUnitRunner
import app.cash.turbine.test
import com.google.firebase.firestore.auth.User
import com.leschnitzky.dailyshiba.MainCoroutineRule
import com.leschnitzky.dailyshiba.UserIntent
import com.leschnitzky.dailyshiba.usermanagement.repository.Repository
import com.leschnitzky.dailyshiba.usermanagement.repository.RepositoryImpl
import com.leschnitzky.dailyshiba.usermanagement.ui.UserViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import javax.inject.Inject
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime

@RunWith(JUnit4::class)
class UserViewModelTest {


    @ExperimentalTime
    @ExperimentalCoroutinesApi
    @Test
    fun userViewModel_GetLoginIntentWithOKValues_ShouldRunRepoMethod() {
    }


    @Test
    fun userViewModel_GetDisplayNameWhenLoggedOff_ReturnsUnsigned(){
        assertEquals("test", "test")
    }
}