package com.example.sergeyportfolioapp.usermanagement.repository

import android.util.Log
import com.example.sergeyportfolioapp.usermanagement.repository.retrofit.RetrofitRepository
import com.example.sergeyportfolioapp.usermanagement.repository.firebaseauth.AuthRepository
import com.example.sergeyportfolioapp.usermanagement.repository.firebaseauth.model.UserForFirebase
import com.example.sergeyportfolioapp.usermanagement.repository.firestore.FirestoreRepository
import com.example.sergeyportfolioapp.usermanagement.repository.firestore.model.UserForFirestore
import com.example.sergeyportfolioapp.usermanagement.repository.room.UserDao
import com.example.sergeyportfolioapp.usermanagement.repository.room.model.User
import com.example.sergeyportfolioapp.usermanagement.repository.room.model.UserTypeConverter
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import com.kiwimob.firestore.coroutines.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RepositoryImpl @Inject constructor(
    var userDao: UserDao,
    var authRepository: AuthRepository,
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
        authRepository.createUser(UserForFirebase(email,password)).let {
            userDao.insertUser(User(email,name, arrayListOf(), mapOf()))
        }
        firestoreRepo.addNewUserToFirestore(email,name)
        return name;
    }

    override suspend fun createUserInFirestore(email: String, name: String) {
        firestoreRepo.addNewUserToFirestore(email,name).let {
            userDao.insertUser(User(email,name, arrayListOf(), mapOf()))
        }
    }

    override fun logoutUser() {
        Log.d(TAG, "logoutUser: ")
        authRepository.logOffFromCurrentUser()
    }

    override fun getCurrentUserEmail(): String? {
        return authRepository.getCurrentUser()?.email
    }

    override suspend fun getCurrentUserDisplayName(): String? {
        if(authRepository.getCurrentUser() == null){
            return null
        }
        return userDao
            .getDisplayNameByEmail(
                authRepository.getCurrentUser()!!.email!!
            ).first().displayName
    }

    override suspend fun getCurrentUserPhotos(): ArrayList<String> {
        Log.d(TAG, "getCurrentUserPhotos: ")
        val typeConverter = UserTypeConverter()
        var photosUrls = arrayListOf<String>()
        photosUrls.addAll(
            typeConverter.StringToList(
                userDao.getCurrentPhotosByEmail(
                getCurrentUserEmail()!!
            ).first()
            )
        )
        Log.d(TAG, "getCurrentUserPhotos SIZE: ${photosUrls.size}")

        if(photosUrls.isNullOrEmpty()){

            photosUrls = getPhotosFromServerDB()
            if(photosUrls.size == 1){
                photosUrls = getNewPhotosFromServer()
            }
        }

        Log.d(TAG, "Photo list: $photosUrls")

        return photosUrls
    }

    private suspend fun getPhotosFromServerDB(): ArrayList<String> {
        val photosUrls = arrayListOf<String>("SaveLocally")
        photosUrls.addAll( firestoreRepo.getUserFromFirestore(getCurrentUserEmail()!!).currentPhotosList as ArrayList<String>)
        Log.d(TAG, "getPhotosFromServerDB: $photosUrls")
        return photosUrls
    }

    override suspend fun getCurrentUserFavorites(): List<String> {
        return firestoreRepo.getUserFromFirestore(getCurrentUserEmail()!!).favoritesList
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
            firestoreRepo.getUserFromFirestore(email!!).also {
                it.profilePicURI = profilePic;
                firestoreRepo.updateUserFromFirestore(it)
            }
        }
    }

    override suspend fun getCurrentUserTitleState(): Pair<String, String> {
        firestoreRepo.getUserFromFirestore(getCurrentUserEmail()!!).also {

            Log.d(TAG, "getCurrentUserTitleState: $it")
            return Pair(it.displayName,it.profilePicURI)
        }
    }

    override suspend fun getCurrentUserURLMap(): Map<String, String> {
        val typeConverter = UserTypeConverter()
        return typeConverter.StringToMap(
            userDao.getCurrentURLMapByEmail(
                getCurrentUserEmail()!!
            ).first()
        )
    }

    override suspend fun getNewPhotosFromServer() : ArrayList<String> {
        val urlsFromServer = getPhotosFromServer()
        firestoreRepo.updateUserPhotos(getCurrentUserEmail()!!,urlsFromServer)
        val photosUrls = arrayListOf<String>()
        photosUrls.add("SaveLocally")
        photosUrls.addAll(urlsFromServer)
        return photosUrls
    }

    override suspend fun addPictureToFavorites(picture: String) {
        Log.d(TAG, "addPictureToFavorites: $picture")
        val user = firestoreRepo.getUserFromFirestore(getCurrentUserEmail()!!)
        val arrayList = arrayListOf<String>(picture)
        arrayList.addAll(user.favoritesList)
        user.favoritesList = arrayList.distinct()
        firestoreRepo.updateUserFromFirestore(user)
    }

    override suspend fun removePictureFromFavorites(picture: String) {
        val user = firestoreRepo.getUserFromFirestore(getCurrentUserEmail()!!)
        val arrayList = arrayListOf<String>()
        arrayList.addAll(user.favoritesList)
        arrayList.remove(picture)
        user.favoritesList = arrayList
        firestoreRepo.updateUserFromFirestore(user)
    }

    override suspend fun isPhotoInCurrentUserFavorites(picture: String) : Boolean {
        return firestoreRepo.getUserFromFirestore(getCurrentUserEmail()!!).favoritesList.contains(picture)
    }

    override suspend fun signInAccountWithGoogle(signedInAccountFromIntent: Task<GoogleSignInAccount>?) {
        signedInAccountFromIntent?.await().also {
            authRepository.logToUser(GoogleAuthProvider.getCredential(it?.idToken,null))
        }
    }

    override suspend fun doesUserExistInFirestore(currentUserEmail: String) : Boolean {
        return firestoreRepo.doesUserExist(currentUserEmail)
    }

    override fun getAuthDisplayName(): String {
       return authRepository.getUserDisplayName()
    }

    suspend fun getPhotosFromServer(): List<String> {
        Log.d(TAG, "getPhotosFromServer: ")
        return retrofitRepository.get10Photos()
    }


    private suspend fun logWithANewUserAndGetName(user: UserForFirebase): String {
        authRepository.logToUser(user).let {
                val userEmail = authRepository.getCurrentUser()?.email!!
                val userDisplayNameEntry = userDao.getDisplayNameByEmail(userEmail)
            return if(userDisplayNameEntry.isEmpty()) {
                val firebaseDisplayName = authRepository.getUserDisplayName()
                userDao.insertUser(User(userEmail,firebaseDisplayName, arrayListOf(), mapOf()))
                firebaseDisplayName
            } else {
                userDisplayNameEntry.first().displayName
            }
            }
        }


}


