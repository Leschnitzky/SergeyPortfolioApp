package com.example.sergeyportfolioapp.usermanagement.repository.firestore

import com.example.sergeyportfolioapp.usermanagement.repository.firestore.model.UserForFirestore
import kotlinx.coroutines.flow.Flow

interface FirestoreRepository {
    suspend fun addNewUserToFirestore(email: String)
    suspend fun addNewUserToFirestore(email: String,name: String)
    suspend fun addNewUserToFirestore(email: String, name : String, profilePic : String)
    suspend fun getUserFromFirestore(email: String): UserForFirestore
    suspend fun updateUserFromFirestore(userForFirestore: UserForFirestore)
}