package com.example.sergeyportfolioapp.usermanagement.repository

import android.util.Log
import com.example.sergeyportfolioapp.usermanagement.repository.retrofit.RetrofitRepository
import com.example.sergeyportfolioapp.usermanagement.repository.firebaseauth.FirebaseRepository
import com.example.sergeyportfolioapp.usermanagement.repository.firebaseauth.model.UserForFirebase
import com.example.sergeyportfolioapp.usermanagement.repository.firestore.FirestoreRepository
import com.example.sergeyportfolioapp.usermanagement.repository.firestore.model.UserForFirestore
import com.example.sergeyportfolioapp.usermanagement.repository.room.UserDao
import com.example.sergeyportfolioapp.usermanagement.repository.room.model.User
import com.example.sergeyportfolioapp.usermanagement.repository.room.model.UserTypeConverter
import com.example.sergeyportfolioapp.utils.GlobalTags.Companion.TAG_PROFILE_PIC
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RepositoryImpl @Inject constructor(
    var userDao: UserDao,
    var database: FirebaseRepository,
    var firestoreRepo : FirestoreRepository,
    val retrofitRepository: RetrofitRepository,
    val applicationScope : CoroutineScope
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
            userDao.insertUser(User(email,name, arrayListOf(), mapOf()))
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
        val typeConverter = UserTypeConverter()
        val photosUrls = arrayListOf<String>()
        photosUrls.addAll(
            typeConverter.StringToList(
                userDao.getCurrentPhotosByEmail(
                getCurrentUserEmail()!!
            ).first()
            )
        )

        if(photosUrls.isEmpty()){
            photosUrls.add("SaveLocally")
            photosUrls.addAll(getPhotosFromServer())
        }

        return photosUrls
    }

    override suspend fun getCurrentUserFavorites(): kotlinx.coroutines.flow.Flow<UserForFirestore> {
        return firestoreRepo.getUserFromFirestore(getCurrentUserEmail()!!)
    }

    override suspend fun updateCurrentUserPhotos(
        list: ArrayList<String>,
        originalUrlList: List<String>
    ) {
        Log.d(TAG, "updateCurrentUserPhotos: Email: ${getCurrentUserEmail()!!} ,List: $list, Original Url List: $originalUrlList")
        userDao.updateCurrentPhotosByMail(list,getCurrentUserEmail()!!)
        val map = createURLMap(list,originalUrlList)
        userDao.updateCurrentPhotoURLMapByMail(getCurrentUserEmail()!!,map)
    }

    private fun createURLMap(list: ArrayList<String>, originalUrlList: List<String>): Map<String,String> {
        val map = mutableMapOf<String,String>()
        (list zip originalUrlList).forEach {
            map[it.first] = it.second
        }
        return map
    }

    override suspend fun updateCurrentUserProfilePicture(profilePic: String) {
        val email = getCurrentUserEmail()
        withContext(Dispatchers.IO){
            firestoreRepo.getUserFromFirestore(email!!).collect {
                it.profilePicURI = profilePic;
                firestoreRepo.updateUserFromFirestore(it)
            }
        }
    }

    override suspend fun getCurrentUserProfilePic(): kotlinx.coroutines.flow.Flow<UserForFirestore> {
        Log.d("$TAG.$TAG_PROFILE_PIC", "getCurrentUserProfilePic: start currentProfile")
        return firestoreRepo.getUserFromFirestore(getCurrentUserEmail()!!)



    }

    override suspend fun getCurrentUserURLMap(): Map<String, String> {
        val typeConverter = UserTypeConverter()
        return typeConverter.StringToMap(userDao.getCurrentURLMapByEmail(getCurrentUserEmail()!!).first())
    }

    suspend fun getPhotosFromServer(): List<String> {
        return retrofitRepository.get10Photos()
    }


    private suspend fun logWithANewUserAndGetName(user: UserForFirebase): String {
        database.logToUser(user).let {
                val userEmail = database.getCurrentUser()?.email!!
                val userDisplayNameEntry = userDao.getDisplayNameByEmail(userEmail)
            return if(userDisplayNameEntry.isEmpty()) {
                val firebaseDisplayName = database.getUserDisplayName()
                userDao.insertUser(User(userEmail,firebaseDisplayName, arrayListOf(), mapOf()))
                firebaseDisplayName
            } else {
                userDisplayNameEntry.first().displayName
            }
            }
        }


}


