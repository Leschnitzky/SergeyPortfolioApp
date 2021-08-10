package com.leschnitzky.dailyshiba.di

import android.content.Context
import androidx.room.Room
import com.facebook.CallbackManager
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.leschnitzky.dailyshiba.util.TestCoroutineRule
import com.leschnitzky.dailyshiba.utils.CoroutineContextProvider
import com.leschnitzky.dailyshiba.utils.CoroutineScopeProvider
import com.leschnitzky.dailyshiba.utils.CoroutineScopeProviderImpl
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.rules.TestRule
import com.leschnitzky.dailyshiba.util.TestCoroutineContextProvider

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [UserManagementModule::class]
    )
object TestModule {

    val testRule = TestCoroutineRule()

    @Provides
    fun provideRule() : TestRule{
        return testRule
    }


    @Provides
    fun provideViewModelScope() : CoroutineScopeProvider {
        return CoroutineScopeProviderImpl(TestCoroutineScope(testRule.testDispatcher))
    }

    @Provides
    fun provideCoroutineContextProvider() : CoroutineContextProvider {
        return TestCoroutineContextProvider(testRule.testDispatcher)
    }

    @Provides
    fun provideFacebookLoginManager() : LoginManager {
        return mockk(relaxed = true)
    }

    @Provides
    fun provideFacebookCallbackManager() : CallbackManager {
        return mockk(relaxed = true)
    }

    @Provides
    fun provideGoogleSigninClient(@ApplicationContext context: Context): GoogleSignInClient {
        return mockk(relaxed = true)

    }


}