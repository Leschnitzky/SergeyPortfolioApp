package com.leschnitzky.dailyshiba.usermanagement.repository.firestore

import android.util.Log
import com.leschnitzky.dailyshiba.usermanagement.repository.firestore.model.UserForFirestore
import com.leschnitzky.dailyshiba.usermanagement.repository.firestore.model.UserForFirestore.Companion.fromMap
import com.leschnitzky.dailyshiba.usermanagement.repository.firestore.model.UserForFirestore.Companion.toMap
import com.google.firebase.firestore.FirebaseFirestore
import com.kiwimob.firestore.coroutines.await
import com.leschnitzky.dailyshiba.usermanagement.repository.firestore.model.FireStoreConverter
import timber.log.Timber
import javax.inject.Inject

class FirestoreRepositoryImpl @Inject constructor(
    var firestore : FirebaseFirestore
) : FirestoreRepository {
    private val TAG = "FirestoreRepository"
    private val COLLECTION_NAME = "users"
    
    override suspend fun addNewUserToFirestore(email: String) {
        firestore.collection(COLLECTION_NAME)
            .document(email)
            .set(toMap(UserForFirestore(email)))
    }

    override suspend fun addNewUserToFirestore(email: String, name: String) {
        firestore.collection(COLLECTION_NAME)
            .document(email)
            .set(toMap(UserForFirestore(email,name))).await()    }


    override suspend fun addNewUserToFirestore(email: String, name: String, profilePic: String) {
        firestore.collection(COLLECTION_NAME)
            .document(email)
            .set(toMap(UserForFirestore(email, name, profilePic, listOf(), listOf(), UserForFirestore.UserSettingsForFirestore.DEFAULT_USER_SETTINGS)))
            .await()
    }

    override suspend fun getUserFromFirestore(email: String): UserForFirestore {
        val user = firestore
            .collection(COLLECTION_NAME)
            .document(email).get().await()

        return fromMap(user?.data as Map<String, Any>)
    }


    override suspend fun updateUserFromFirestore(userForFirestore: UserForFirestore) {
        Timber.d( "updateUserFromFirestore: $userForFirestore")
        firestore
            .collection(COLLECTION_NAME)
            .document(userForFirestore.email)
            .update(
                "display_name", userForFirestore.displayName,
                "profile_pic", userForFirestore.profilePicURI,
                "list_favorite", userForFirestore.favoritesList,
                    "current_photos", userForFirestore.currentPhotosList,
                "user_settings", FireStoreConverter.settingsToString(userForFirestore.userSettings)
            )
            .await()

    }

    override suspend fun updateUserPhotos(email: String, urlsFromServer: List<String>) {
        Timber.d( "updateUserPhotos: $urlsFromServer")
        val user = getUserFromFirestore(email)
        user.currentPhotosList = urlsFromServer
        updateUserFromFirestore(user)
    }

    override suspend fun updateUserSettings(
        email: String,
        userSettingsForFirestore: UserForFirestore.UserSettingsForFirestore
    ) {
        val user = getUserFromFirestore(email)
        user.userSettings = userSettingsForFirestore
        updateUserFromFirestore(user)
    }

    override suspend fun doesUserExist(currentUserEmail: String): Boolean {
        firestore
            .collection(COLLECTION_NAME)
            .document(currentUserEmail)
            .get().await().let {
                return it.exists()
            }
    }
}