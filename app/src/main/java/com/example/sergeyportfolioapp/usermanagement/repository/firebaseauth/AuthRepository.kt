package com.example.sergeyportfolioapp.usermanagement.repository.firebaseauth

import com.example.sergeyportfolioapp.usermanagement.repository.firebaseauth.model.UserForFirebase
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthCredential

interface AuthRepository {
    suspend fun createUser(userForFirebase: UserForFirebase)
    fun getCurrentUser(): FirebaseUser?
    suspend fun logToUser(userForFirebase: UserForFirebase)
    fun logOffFromCurrentUser()
    fun getUserDisplayName(): String
    suspend fun logToUser(tokenID: AuthCredential)
}