package com.leschnitzky.dailyshiba.usermanagement.di

import com.facebook.CallbackManager
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.leschnitzky.dailyshiba.usermanagement.repository.Repository
import dagger.Module
import dagger.Provides
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
        return FakeRepositoryImpl()
    }

    @Provides
    fun provideGoogleSignInClient() : GoogleSignInClient{
        return mockk<GoogleSignInClient>()
    }

    @Provides
    fun provideFacebookCallbackManager() : CallbackManager{
        return mockk()
    }

    @Provides
    fun provideFacebookLoginManager() : LoginManager{
        return mockk()
    }
}