package com.leschnitzky.dailyshiba.di

import android.content.Context
import androidx.room.Room
import com.leschnitzky.dailyshiba.MyApplication
import com.leschnitzky.dailyshiba.R
import com.leschnitzky.dailyshiba.usermanagement.repository.retrofit.RetrofitRepository
import com.leschnitzky.dailyshiba.usermanagement.repository.retrofit.RetrofitRepositoryImpl
import com.leschnitzky.dailyshiba.usermanagement.repository.retrofit.ShibaRetrofit
import com.leschnitzky.dailyshiba.usermanagement.repository.firebaseauth.AuthRepository
import com.leschnitzky.dailyshiba.usermanagement.repository.firebaseauth.AuthRepositoryImpl
import com.leschnitzky.dailyshiba.usermanagement.repository.firestore.FirestoreRepository
import com.leschnitzky.dailyshiba.usermanagement.repository.firestore.FirestoreRepositoryImpl
import com.leschnitzky.dailyshiba.usermanagement.repository.Repository
import com.leschnitzky.dailyshiba.usermanagement.repository.RepositoryImpl

import com.leschnitzky.dailyshiba.usermanagement.repository.room.LocalUserDatabase
import com.leschnitzky.dailyshiba.usermanagement.repository.room.UserDao
import com.leschnitzky.dailyshiba.usermanagement.repository.room.model.UserTypeConverter
import com.facebook.CallbackManager
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.leschnitzky.dailyshiba.usermanagement.repository.retrofit.BreedsRetrofit
import com.leschnitzky.dailyshiba.utils.CoroutineContextProvider
import com.leschnitzky.dailyshiba.utils.CoroutineContextProviderImpl
import com.leschnitzky.dailyshiba.utils.CoroutineScopeProvider
import com.leschnitzky.dailyshiba.utils.CoroutineScopeProviderImpl
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


@Module
@InstallIn(SingletonComponent::class)
internal object UserManagementModule{

    @Provides
    fun provideViewModelScope() : CoroutineScopeProvider {
        return CoroutineScopeProviderImpl(null)
    }

    @Provides
    fun provideCoroutineContextProvider() : CoroutineContextProvider {
        return CoroutineContextProviderImpl()
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


    @Provides
    fun provideApplicationScope(@ApplicationContext application: MyApplication): CoroutineScope{
        return application.applicationScope
    }

    @Provides
    fun provideFirebaseDatabaseReference(): FirebaseAuth{
        return FirebaseAuth.getInstance()
    }

    @Provides
    fun provideFirebaseRepository() : AuthRepository{
        return AuthRepositoryImpl(provideFirebaseDatabaseReference())
    }

    @Provides
    fun provideFirestoreRepository(): FirestoreRepository{
        return FirestoreRepositoryImpl(provideFirestoreInstance())
    }

    @Provides
    fun provideFirestoreInstance(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    fun provideRepository(@ApplicationContext context: Context) : Repository{
        return RepositoryImpl(
            provideCoroutineContextProvider(),
            provideUserDao(context),
            provideFirebaseRepository(),
            provideFirestoreRepository(),
            provideRetrofitRepository()
        )
    }

    @Provides
    fun provideUserDataBase(@ApplicationContext context : Context) : LocalUserDatabase {
        return Room.databaseBuilder(
            context,
            LocalUserDatabase::class.java, "userDB"
        )
            .addTypeConverter(UserTypeConverter())
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideUserDao(@ApplicationContext context: Context): UserDao{
        return provideUserDataBase(context).usersDao()
    }

    @Provides
    fun provideShibaRetrofit() : ShibaRetrofit {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://shibe.online/api/")
            .addConverterFactory(
                MoshiConverterFactory.create(
                    Moshi.Builder()
                        .build()
                ))
            .build()

        return retrofit.create(ShibaRetrofit::class.java)
    }

    @Provides
    fun provideBreedsOtherRetrofit() : BreedsRetrofit {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.woofbot.io/v1/")
            .addConverterFactory(
                MoshiConverterFactory.create(
                    Moshi.Builder()
                        .build()
                )
            )
            .build()

        return retrofit.create(BreedsRetrofit::class.java)
    }

    @Provides
    fun provideRetrofitRepository() : RetrofitRepository {
        return RetrofitRepositoryImpl(
            provideShibaRetrofit(),
            provideBreedsOtherRetrofit()
        )
    }

}