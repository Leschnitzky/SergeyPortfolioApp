package com.example.sergeyportfolioapp.usermanagement.firestore

import com.example.sergeyportfolioapp.usermanagement.firestore.model.UserForFirestore
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference

interface FirestoreRepository {
    suspend fun addNewUserToFirestore(email: String): Task<DocumentReference>
    suspend fun getUserFromFirestore(email: String): UserForFirestore
    suspend fun updateUserFromFirestore(userForFirestore: UserForFirestore)
}