package com.example.sergeyportfolioapp.usermanagement.firestore

import com.example.sergeyportfolioapp.usermanagement.firestore.model.UserForFirestore
import com.example.sergeyportfolioapp.usermanagement.firestore.model.UserForFirestore.Companion.fromMap
import com.example.sergeyportfolioapp.usermanagement.firestore.model.UserForFirestore.Companion.toMap
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FirestoreRepositoryImpl @Inject constructor(
    var firestore : FirebaseFirestore
) : FirestoreRepository {
    private val COLLECTION_NAME = "users"
    
    override suspend fun addNewUserToFirestore(email: String): Task<DocumentReference> {
        return firestore.collection(COLLECTION_NAME)
                .add(toMap(UserForFirestore(email)))
    }

    override suspend fun getUserFromFirestore(email: String): UserForFirestore {
        return fromMap(
            firestore
                .collection(COLLECTION_NAME)
                .document(email)
                .get()
                .await()
                .data as MutableMap<String, Any>
        )
    }

    override suspend fun updateUserFromFirestore(userForFirestore: UserForFirestore) {
        firestore
            .collection(COLLECTION_NAME)
            .document(userForFirestore.email)
            .update(
                "profile_pic", userForFirestore.profilePicURI,
                "favoritesList", userForFirestore.favoritesList
            )
            .await()
    }
}