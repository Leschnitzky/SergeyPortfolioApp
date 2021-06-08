package com.example.sergeyportfolioapp.usermanagement.repository

import android.util.Log
import com.example.sergeyportfolioapp.usermanagement.firebaseauth.FirebaseRepository
import com.example.sergeyportfolioapp.usermanagement.firebaseauth.model.UserForFirebase
import com.example.sergeyportfolioapp.usermanagement.room.UserDao
import com.example.sergeyportfolioapp.usermanagement.room.model.User
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
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
            val userTokenList = userDao.getTokenByEmailAndPassword(email + password)
            return if (userTokenList.isNotEmpty()){
                logUserWithTokenAndGetName(userTokenList.first().token)
            } else {
                logWithANewUserAndGetName(user);
            }
        } catch (e: FirebaseAuthException){

            Log.e(TAG, "loginUserAndReturnName: caughtError: ${e.errorCode}")
            Log.e(TAG, "loginUserAndReturnName: caughtError: ${e.message}")

            if(e.errorCode.equals("ERROR_INVALID_CUSTOM_TOKEN")){
                return updateTokenAndGetName(user);
            } else {
                throw e
            }
        }
    }


    private suspend fun logUserWithTokenAndGetName(token: String): String {
        Log.d(TAG, "logUserWithTokenAndGetName: $token")
        database.logToUser(token).let {
            return database.getUserDisplayName()
        }
    }

    private suspend fun logWithANewUserAndGetName(user: UserForFirebase): String {
        database.logToUser(user).let {
            database.getCurrentUser()?.getIdToken(false)?.await().let {
                Log.d(TAG, "loginUserAndReturnName: insert UserPass: ${user.email}$${user.password}" +
                        " and Token: ${it?.token}")
                userDao.insertUser(User("${user.email}\$${user.password}",it?.token!!))
                return database.getUserDisplayName()
            }
        }
    }

    private suspend fun updateTokenAndGetName(user: UserForFirebase): String {
        database.logToUser(user).let {
            database.getCurrentUser()?.getIdToken(false)?.await().let {
                Log.d(TAG, "loginUserAndReturnName: insert UserPass: ${user.email}$${user.password}" +
                        " and Token: ${it?.token}")
                withContext(Dispatchers.IO){
                    userDao.updateTokenForUser(it?.token!!,"${user.email}\$${user.password}")
                }
                return database.getUserDisplayName()
            }
        }
    }

}
