package com.example.sergeyportfolioapp.usermanagement.firebaseauth

import com.example.sergeyportfolioapp.usermanagement.firebaseauth.model.UserForFirebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser

interface FirebaseRepository {
    suspend fun createUser(userForFirebase: UserForFirebase)
    fun getCurrentUser(): FirebaseUser?
    suspend fun logToUser(userForFirebase: UserForFirebase)
    fun logOffFromCurrentUser()
    suspend fun logToUser(tokenID: String)
    fun getUserDisplayName(): String
}