package com.leschnitzky.dailyshiba.usermanagement.repository.firebaseauth

import com.leschnitzky.dailyshiba.usermanagement.repository.firebaseauth.model.UserForFirebase
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
    suspend fun createUser(userForFirebase: UserForFirebase)
    fun getCurrentUser(): FirebaseUser?
    suspend fun logToUser(userForFirebase: UserForFirebase)
    fun logOffFromCurrentUser()
    fun getUserDisplayName(): String
    suspend fun logToUser(tokenID: AuthCredential)
}