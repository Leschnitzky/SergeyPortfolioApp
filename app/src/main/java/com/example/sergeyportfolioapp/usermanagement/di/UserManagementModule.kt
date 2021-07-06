package com.example.sergeyportfolioapp.usermanagement.di

import android.content.Context
import androidx.room.Room
import com.example.sergeyportfolioapp.MyApplication
import com.example.sergeyportfolioapp.R
import com.example.sergeyportfolioapp.usermanagement.repository.retrofit.RetrofitRepository
import com.example.sergeyportfolioapp.usermanagement.repository.retrofit.RetrofitRepositoryImpl
import com.example.sergeyportfolioapp.usermanagement.repository.retrofit.ShibaRetrofit
import com.example.sergeyportfolioapp.usermanagement.repository.firebaseauth.AuthRepository
import com.example.sergeyportfolioapp.usermanagement.repository.firebaseauth.AuthRepositoryImpl
import com.example.sergeyportfolioapp.usermanagement.repository.firestore.FirestoreRepository
import com.example.sergeyportfolioapp.usermanagement.repository.firestore.FirestoreRepositoryImpl
import com.example.sergeyportfolioapp.usermanagement.repository.Repository
import com.example.sergeyportfolioapp.usermanagement.repository.RepositoryImpl

import com.example.sergeyportfolioapp.usermanagement.repository.room.LocalUserDatabase
import com.example.sergeyportfolioapp.usermanagement.repository.room.UserDao
import com.example.sergeyportfolioapp.usermanagement.repository.room.model.UserTypeConverter
import com.facebook.CallbackManager
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


@Module
@InstallIn(SingletonComponent::class)
internal object UserManagementModule{

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
            provideUserDao(context),
            provideFirebaseRepository(),
            provideFirestoreRepository(),
            provideRetrofitRepository(),
        provideApplicationScope(context as MyApplication))
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
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build()

        return retrofit.create(ShibaRetrofit::class.java)
    }

    @Provides
    fun provideRetrofitRepository() : RetrofitRepository {
        return RetrofitRepositoryImpl(provideShibaRetrofit())
    }

}