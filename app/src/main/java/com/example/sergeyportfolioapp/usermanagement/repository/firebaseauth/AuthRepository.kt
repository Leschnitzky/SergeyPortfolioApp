package com.example.sergeyportfolioapp.usermanagement.repository.firebaseauth

import com.example.sergeyportfolioapp.usermanagement.repository.firebaseauth.model.UserForFirebase
import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
    suspend fun createUser(userForFirebase: UserForFirebase)
    fun getCurrentUser(): FirebaseUser?
    suspend fun logToUser(userForFirebase: UserForFirebase)
    fun logOffFromCurrentUser()
    suspend fun logToUser(tokenID: String)
    fun getUserDisplayName(): String
}