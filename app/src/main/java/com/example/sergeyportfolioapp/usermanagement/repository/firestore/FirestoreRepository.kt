package com.example.sergeyportfolioapp.usermanagement.repository.firestore

import com.example.sergeyportfolioapp.usermanagement.repository.firestore.model.UserForFirestore
import kotlinx.coroutines.flow.Flow

interface FirestoreRepository {
    suspend fun addNewUserToFirestore(email: String)
    suspend fun addNewUserToFirestore(email: String, profilePic : String)
    fun getUserFromFirestore(email: String): Flow<UserForFirestore>
    suspend fun updateUserFromFirestore(userForFirestore: UserForFirestore)
}