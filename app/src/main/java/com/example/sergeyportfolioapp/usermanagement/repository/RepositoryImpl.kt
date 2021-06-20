package com.example.sergeyportfolioapp.usermanagement.repository

import android.util.Log
import androidx.room.TypeConverter
import com.example.sergeyportfolioapp.usermanagement.firebaseauth.FirebaseRepository
import com.example.sergeyportfolioapp.usermanagement.firebaseauth.model.UserForFirebase
import com.example.sergeyportfolioapp.usermanagement.firestore.FirestoreRepository
import com.example.sergeyportfolioapp.usermanagement.room.UserDao
import com.example.sergeyportfolioapp.usermanagement.room.model.User
import com.example.sergeyportfolioapp.usermanagement.room.model.UserTypeConverter
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.log

class RepositoryImpl @Inject constructor(
    var userDao: UserDao,
    var database: FirebaseRepository,
    var firestoreRepo : FirestoreRepository,
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
            throw e

        }
        return "BADSTRING"
    }

    override suspend fun createUser(email: String, password: String, name: String): String {
        database.createUser(UserForFirebase(email,password)).let {
            userDao.insertUser(User(email,name, arrayListOf("get"),10, arrayListOf()))
        }
        firestoreRepo.addNewUserToFirestore(email)
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

    override suspend fun getCurrentUserPhotos(): ArrayList<String> {
        Log.d(TAG, "getCurrentUserPhotos: Here")
        val typeConverter = UserTypeConverter()
        return typeConverter.StringToList(
            userDao.getCurrentPhotosByEmail(
                getCurrentUserEmail()!!
            ).first())
    }

    override suspend fun getCurrentUserFavorites(): ArrayList<String> {
        val typeConverter = UserTypeConverter()
        return typeConverter.StringToList(
            userDao.getFavoritePhotosByEmail(
                getCurrentUserEmail()!!).first()
        )
    }

    override suspend fun updateCurrentUserPhotos(list: ArrayList<String>) {

        Log.d(TAG, "updateCurrentUserPhotos: Email: ${getCurrentUserEmail()!!} ,List: $list")
        userDao.updateCurrentPhotosByMail(list,getCurrentUserEmail()!!)
    }

    override suspend fun updateCurrentUserProfilePicture(profilePic: String) {
        val email = getCurrentUserEmail()
        withContext(Dispatchers.IO){
            val user = firestoreRepo.getUserFromFirestore(email!!)
            user.profilePicURI = profilePic;
            firestoreRepo.updateUserFromFirestore(user)
        }
    }

    override suspend fun getCurrentUserProfilePic(): String {
        return firestoreRepo.getUserFromFirestore(getCurrentUserEmail()!!).profilePicURI
    }


    private suspend fun logWithANewUserAndGetName(user: UserForFirebase): String {
        database.logToUser(user).let {
                val userEmail = database.getCurrentUser()?.email!!
                val userDisplayNameEntry = userDao.getDisplayNameByEmail(userEmail)
            return if(userDisplayNameEntry.isEmpty()) {
                val firebaseDisplayName = database.getUserDisplayName()
                userDao.insertUser(User(userEmail,firebaseDisplayName, arrayListOf(),10, arrayListOf()))
                firebaseDisplayName
            } else {
                userDisplayNameEntry.first().displayName
            }
            }
        }
}


