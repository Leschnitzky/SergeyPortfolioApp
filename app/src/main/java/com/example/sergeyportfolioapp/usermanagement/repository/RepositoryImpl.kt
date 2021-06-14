package com.example.sergeyportfolioapp.usermanagement.repository

import android.util.Log
import com.example.sergeyportfolioapp.usermanagement.firebaseauth.FirebaseRepository
import com.example.sergeyportfolioapp.usermanagement.firebaseauth.model.UserForFirebase
import com.example.sergeyportfolioapp.usermanagement.room.UserDao
import com.example.sergeyportfolioapp.usermanagement.room.model.User
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RepositoryImpl @Inject constructor(
    var userDao: UserDao,
    var database: FirebaseRepository
) : Repository {
    private val TAG = "RepositoryImpl"
    override suspend fun loginUserAndReturnName(email: String, password: String): String {
        val user = UserForFirebase(email,password)
        Log.d(TAG, "loginUserAndReturnName: $user")
        try {
            return logWithANewUserAndGetName(user);
        } catch (e: FirebaseAuthException){

            Log.e(TAG, "loginUserAndReturnName: caughtError: ${e.errorCode}")
            Log.e(TAG, "loginUserAndReturnName: caughtError: ${e.message}")

        }
        return "BADSTRING"
    }

    override suspend fun createUser(email: String, password: String, name: String): String {
        database.createUser(UserForFirebase(email,password)).let {
            userDao.insertUser(User(email,name))
        }
        return name;
    }

    override fun logoutUser() {
        Log.d(TAG, "logoutUser: ")
        database.logOffFromCurrentUser()
    }

    override fun getCurrentUserEmail(): String? {
        return database.getCurrentUser()?.email
    }

    override suspend fun getCurrentUserDisplayName(): String? {
        if(database.getCurrentUser() == null){
            return null
        }
        return userDao.getDisplayNameByEmail(database.getCurrentUser()!!.email!!).first().displayName
    }


    private suspend fun logWithANewUserAndGetName(user: UserForFirebase): String {
        database.logToUser(user).let {
                val userEmail = database.getCurrentUser()?.email!!
                val userDisplayNameEntry = userDao.getDisplayNameByEmail(userEmail)
            return if(userDisplayNameEntry.isEmpty()) {
                val firebaseDisplayName = database.getUserDisplayName()
                userDao.insertUser(User(userEmail,firebaseDisplayName))
                firebaseDisplayName
            } else {
                userDisplayNameEntry.first().displayName
            }
            }
        }
}


