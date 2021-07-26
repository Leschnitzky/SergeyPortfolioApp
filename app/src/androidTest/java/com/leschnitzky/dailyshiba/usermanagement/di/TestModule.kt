package com.leschnitzky.dailyshiba.usermanagement.di

import android.content.Context
import com.facebook.CallbackManager
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.leschnitzky.dailyshiba.R
import com.leschnitzky.dailyshiba.usermanagement.repository.Repository
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import org.mockito.Mockito.mock

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
    fun provideFacebookLoginManager() : LoginManager {
        return LoginManager.getInstance()
    }

    @Provides
    fun provideFacebookCallbackManager() : CallbackManager{
        return CallbackManager.Factory.create()
    }

    @Provides
    fun provideGoogleSigninClient(@ApplicationContext context: Context): GoogleSignInClient{
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(context, gso)

    }
}