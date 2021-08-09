package com.leschnitzky.dailyshiba.di

import android.content.Context
import com.facebook.CallbackManager
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.leschnitzky.dailyshiba.di.UserManagementModule
import com.leschnitzky.dailyshiba.usermanagement.repository.Repository
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.mockk

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [UserManagementModule::class]
)
internal object TestModule {
    @Provides
    fun provideRepo(): Repository{
        return mockk()
    }

    @Provides
    fun provideFacebookLoginManager() : LoginManager {
        return mockk(relaxed = true)
    }

    @Provides
    fun provideFacebookCallbackManager() : CallbackManager{
        return mockk(relaxed = true)
    }

    @Provides
    fun provideGoogleSigninClient(@ApplicationContext context: Context): GoogleSignInClient{
        return mockk(relaxed = true)

    }

}