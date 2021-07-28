package com.leschnitzky.dailyshiba.usermanagement.repository

import android.util.Log
import com.leschnitzky.dailyshiba.usermanagement.repository.retrofit.RetrofitRepository
import com.leschnitzky.dailyshiba.usermanagement.repository.firebaseauth.AuthRepository
import com.leschnitzky.dailyshiba.usermanagement.repository.firebaseauth.model.UserForFirebase
import com.leschnitzky.dailyshiba.usermanagement.repository.firestore.FirestoreRepository
import com.leschnitzky.dailyshiba.usermanagement.repository.firestore.model.UserForFirestore
import com.leschnitzky.dailyshiba.usermanagement.repository.room.UserDao
import com.leschnitzky.dailyshiba.usermanagement.repository.room.model.User
import com.leschnitzky.dailyshiba.usermanagement.repository.room.model.UserTypeConverter
import com.facebook.AccessToken
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import com.kiwimob.firestore.coroutines.await
import kotlinx.coroutines.*
import timber.log.Timber
import java.util.regex.Pattern
import javax.inject.Inject

class RepositoryImpl @Inject constructor(
    var userDao: UserDao,
    var authRepository: AuthRepository,
    var firestoreRepo : FirestoreRepository,
    val retrofitRepository: RetrofitRepository,
) : Repository {
    private val TAG = "RepositoryImpl"
    override suspend fun loginUserAndReturnName(email: String, password: String): String {
        val user = UserForFirebase(email,password)
        Timber.d( "loginUserAndReturnName: $user")
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
        Timber.d( "logoutUser: ")
        authRepository.logOffFromCurrentUser()
    }

    override fun getCurrentUserEmail(): String? {
        Timber.d( "getCurrentUserEmail: ${authRepository.getCurrentUser()?.email}")
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
        Timber.d( "getCurrentUserPhotos: ")
        val typeConverter = UserTypeConverter()
        var photosUrls = arrayListOf<String>()
        val localDBQuery = userDao.getCurrentPhotosByEmail(
            getCurrentUserEmail()!!
        )
        Timber.d( "getCurrentUserPhotos: $localDBQuery")

        if(localDBQuery.isEmpty() || localDBQuery.first().isEmpty()){
            photosUrls = getPhotosFromServerDB()
            if(photosUrls.size == 1){
                photosUrls = getNewPhotosFromServer()
            }
        } else {
            photosUrls.addAll(
                typeConverter.StringToList(
                    localDBQuery.first()
                )
            )
        }


        Timber.d( "getCurrentUserPhotos SIZE: ${photosUrls.size}")


        Timber.d( "Photo list: $photosUrls")

        return photosUrls
    }

    private suspend fun getPhotosFromServerDB(): ArrayList<String> {
        val photosUrls = arrayListOf<String>("SaveLocally")
        photosUrls.addAll( firestoreRepo.getUserFromFirestore(getCurrentUserEmail()!!).currentPhotosList as ArrayList<String>)
        Timber.d( "getPhotosFromServerDB: $photosUrls")
        return photosUrls
    }

    override suspend fun getCurrentUserFavorites(): List<String> {
        return firestoreRepo.getUserFromFirestore(getCurrentUserEmail()!!).favoritesList
    }

    override suspend fun updateCurrentUserPhotos(
        list: ArrayList<String>,
        originalUrlList: List<String>
    ) {
        val converter = UserTypeConverter()

        Timber.d( "updateCurrentUserPhotos: Email: ${getCurrentUserEmail()!!} ,List: $list, Original Url List: $originalUrlList")
        userDao.updateCurrentPhotosByMail(list,getCurrentUserEmail()!!)
        val map = createURLMap(list,originalUrlList)
        Timber.d("MAP : $map")
        userDao.updateCurrentPhotoURLMapByMail(getCurrentUserEmail()!!,converter.MapToString(map)).also{
            Timber.d("USER: ${userDao.getDisplayNameByEmail(getCurrentUserEmail()!!)}")
        }
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

            Timber.d( "getCurrentUserTitleState: $it")
            return Pair(it.displayName,it.profilePicURI)
        }
    }

    override suspend fun getCurrentUserURLMap(): Map<String, String> {
        val typeConverter = UserTypeConverter()
        Timber.d(getCurrentUserEmail()!!)
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
        Timber.d( "addPictureToFavorites: $picture")
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

    override suspend fun signInAccountWithFacebook(token: AccessToken?) {
        Timber.d( "signInAccountWithFacebook: ${token?.token}")
        authRepository.logToUser(FacebookAuthProvider.getCredential(token?.token!!))
    }

    override suspend fun getCurrentUserData(): UserForFirestore {
        return firestoreRepo.getUserFromFirestore(getCurrentUserEmail()!!)
    }

    override suspend fun getDBUserData(): User? {
        val listQuery = userDao.getDisplayNameByEmail(getCurrentUserEmail()!!)
        return if(listQuery.isNotEmpty()){
            listQuery.first()
        } else {
            null
        }
    }

    override suspend fun updateCurrentUserDisplayName(displayName: String) {
        var user = firestoreRepo.getUserFromFirestore(getCurrentUserEmail()!!)
        Timber.d( "updateCurrentUserDisplayName: Current User $user")
        user.displayName = displayName
        Timber.d( "updateCurrentUserDisplayName: $displayName")
        Timber.d( "updateCurrentUserDisplayName: Updating $user")

        firestoreRepo.updateUserFromFirestore(user)
    }

    override suspend fun createUserInDB(currentUserEmail: String, authDisplayName: String) {
        userDao.insertUser(User(currentUserEmail,authDisplayName, arrayListOf(), mapOf()))
    }

    override suspend fun sendResetEmail(email: String) {
        authRepository.sendEmailResetMail(email)
    }

    override suspend fun updateCurrentUserSettings(setting: String, field: String, value: String): String {
        val user = firestoreRepo.getUserFromFirestore(getCurrentUserEmail()!!)

        Timber.d("$user")
        val currentSettingsMap = mutableMapOf<String,String>()
        currentSettingsMap.putAll(user.userSettings.settings)

        val currentSetting = user.userSettings.settings[setting]?.toCharArray()
        currentSetting!![currentSetting.indexOf(field[0]) + 1] = value[0]

        val newSettingValue = String(currentSetting)
        val matcher = Pattern.compile("1").matcher(newSettingValue)

        var count = 0
        while (matcher.find()) {
            count++
        }
        if(count == 0){
            return "Must have at least one dog selected"
        }

        currentSettingsMap[setting] = newSettingValue

        user.userSettings = UserForFirestore.UserSettingsForFirestore(currentSettingsMap)
        firestoreRepo.updateUserFromFirestore(user)
        return ""
    }

    suspend fun getPhotosFromServer(): List<String> {
        Timber.d( "getPhotosFromServer: ")
        firestoreRepo.getUserFromFirestore(getCurrentUserEmail()!!).also {
            val dogSetting = it.userSettings.settings["dogs"]
            val dogList = dogSetting?.chunked(2)
                ?.filter {
                    it.indexOf('1') != -1
                }?.map {
                    it.substring(0,1)
                }?.toList()
            return getPhotosByDogList(dogList)
        }
    }

    private suspend fun getPhotosByDogList(dogList: List<String>?): List<String> {
        val dogsPerType = 9 / dogList!!.size
        val extraDogPhotos = 9 % dogList.size
        val returnList = arrayListOf<String>()
        withContext(Dispatchers.IO){
            if(extraDogPhotos > 0){
                async(Dispatchers.IO) {
                    when(dogList.first()){
                        "s" -> returnList.addAll( retrofitRepository.getShibaPhotos(extraDogPhotos))
                        "c" -> returnList.addAll( retrofitRepository.getCorgiPhotos(extraDogPhotos))
                        "h" -> returnList.addAll( retrofitRepository.getHuskyPhotos(extraDogPhotos))
                        "b" -> returnList.addAll( retrofitRepository.getBeaglePhotos(extraDogPhotos))
                        else -> {}
                    }
                }.await()
            }
            dogList.map {
                async(Dispatchers.IO) {
                    when(it){
                        "s" -> returnList.addAll( retrofitRepository.getShibaPhotos(dogsPerType))
                        "c" -> returnList.addAll( retrofitRepository.getCorgiPhotos(dogsPerType))
                        "h" -> returnList.addAll( retrofitRepository.getHuskyPhotos(dogsPerType))
                        "b" -> returnList.addAll( retrofitRepository.getBeaglePhotos(dogsPerType))
                        else -> {}
                    }
                }
            }.awaitAll()

        }
        return returnList
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


