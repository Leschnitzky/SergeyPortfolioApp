package com.example.sergeyportfolioapp.usermanagement.di

import android.content.Context
import androidx.room.Room
import com.example.sergeyportfolioapp.usermanagement.firebaseauth.FirebaseRepository
import com.example.sergeyportfolioapp.usermanagement.firebaseauth.FirebaseRepositoryImpl
import com.example.sergeyportfolioapp.usermanagement.repository.Repository
import com.example.sergeyportfolioapp.usermanagement.repository.RepositoryImpl

import com.example.sergeyportfolioapp.usermanagement.room.LocalUserDatabase
import com.example.sergeyportfolioapp.usermanagement.room.UserDao
import com.example.sergeyportfolioapp.usermanagement.ui.ResourcesProvider
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
internal object UserManagementModule{

    @Provides
    fun provideFirebaseDatabaseReference(): FirebaseAuth{
        return FirebaseAuth.getInstance()
    }

    @Provides
    fun provideFirebaseRepository() : FirebaseRepository{
        return FirebaseRepositoryImpl(provideFirebaseDatabaseReference())
    }

    @Provides
    fun provideRepository(@ApplicationContext context: Context) : Repository{
        return RepositoryImpl(provideUserDao(context), provideFirebaseRepository())
    }

    @Provides
    fun provideUserDataBase(@ApplicationContext context : Context) : LocalUserDatabase {
        return Room.databaseBuilder(
            context,
            LocalUserDatabase::class.java, "userDB"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideUserDao(@ApplicationContext context: Context): UserDao{
        return provideUserDataBase(context).usersDao()
    }
}