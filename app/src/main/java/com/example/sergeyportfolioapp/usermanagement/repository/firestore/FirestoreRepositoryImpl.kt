package com.example.sergeyportfolioapp.usermanagement.repository.firestore

import android.util.Log
import com.example.sergeyportfolioapp.usermanagement.repository.firestore.model.UserForFirestore
import com.example.sergeyportfolioapp.usermanagement.repository.firestore.model.UserForFirestore.Companion.fromMap
import com.example.sergeyportfolioapp.usermanagement.repository.firestore.model.UserForFirestore.Companion.toMap
import com.google.firebase.firestore.FirebaseFirestore
import com.kiwimob.firestore.coroutines.await
import kotlinx.coroutines.flow.Flow
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
            .set(toMap(UserForFirestore(email, name, profilePic, listOf())))
            .await()
    }

    override suspend fun getUserFromFirestore(email: String): UserForFirestore {
        val user = firestore
            .collection(COLLECTION_NAME)
            .document(email).get().await()

        return fromMap(user?.data as Map<String, Any>)
    }


    override suspend fun updateUserFromFirestore(userForFirestore: UserForFirestore) {
        Log.d(TAG, "updateUserFromFirestore: $userForFirestore")
        firestore
            .collection(COLLECTION_NAME)
            .document(userForFirestore.email)
            .update(
                "profile_pic", userForFirestore.profilePicURI,
                "list_favorite", userForFirestore.favoritesList
            )
            .await()

    }
}