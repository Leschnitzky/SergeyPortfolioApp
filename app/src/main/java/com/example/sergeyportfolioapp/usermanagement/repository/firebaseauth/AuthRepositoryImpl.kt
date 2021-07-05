package com.example.sergeyportfolioapp.usermanagement.repository.firebaseauth

import android.util.Log
import com.example.sergeyportfolioapp.usermanagement.repository.firebaseauth.model.UserForFirebase
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthCredential
import kotlinx.coroutines.tasks.await
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
        Log.d(TAG, "logToUser: start")
        firebaseAuth.signInWithEmailAndPassword(
            userForFirebase.email,
            userForFirebase.password
        ).await()
        Log.d(TAG, "logToUser: end")
    }

    override suspend fun logToUser(tokenID: AuthCredential) {
        firebaseAuth
            .signInWithCredential(tokenID)
            .await()
    }

    override fun logOffFromCurrentUser() {
        Log.d(TAG, "logOffFromCurrentUser: ")
        firebaseAuth.signOut()
    }

    override fun getUserDisplayName(): String {
        if(firebaseAuth.currentUser?.displayName == null){
            return firebaseAuth.currentUser?.email!!;
        }
        return firebaseAuth.currentUser?.displayName!!
    }


}