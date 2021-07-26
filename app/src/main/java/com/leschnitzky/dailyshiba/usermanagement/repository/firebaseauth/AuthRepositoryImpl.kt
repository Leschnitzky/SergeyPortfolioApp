package com.leschnitzky.dailyshiba.usermanagement.repository.firebaseauth

import android.util.Log
import com.google.firebase.auth.ActionCodeSettings
import com.leschnitzky.dailyshiba.usermanagement.repository.firebaseauth.model.UserForFirebase
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(private val firebaseAuth: FirebaseAuth)
    : AuthRepository {
    private val TAG = "FirebaseRepositoryImpl"
    override suspend fun createUser(userForFirebase: UserForFirebase) {
        firebaseAuth.createUserWithEmailAndPassword(
                userForFirebase.email,
                userForFirebase.password
        ).await()
    }

    override fun getCurrentUser(): FirebaseUser? {
       return firebaseAuth.currentUser
    }

    override suspend fun logToUser(userForFirebase: UserForFirebase) {
        Timber.d( "logToUser: start")
        firebaseAuth.signInWithEmailAndPassword(
            userForFirebase.email,
            userForFirebase.password
        ).await()
        Timber.d( "logToUser: end")
    }

    override suspend fun logToUser(tokenID: AuthCredential) {
        firebaseAuth
            .signInWithCredential(tokenID)
            .await()
    }

    override fun logOffFromCurrentUser() {
        Timber.d( "logOffFromCurrentUser: ")
        firebaseAuth.signOut()
    }

    override fun getUserDisplayName(): String {
        if(firebaseAuth.currentUser?.displayName == null){
            return firebaseAuth.currentUser?.email!!;
        }
        return firebaseAuth.currentUser?.displayName!!
    }

    override suspend fun sendEmailResetMail(email: String) {
        firebaseAuth.sendPasswordResetEmail(email)
    }


}