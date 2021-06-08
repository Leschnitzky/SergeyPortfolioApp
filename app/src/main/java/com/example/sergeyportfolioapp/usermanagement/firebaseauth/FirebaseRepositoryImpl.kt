package com.example.sergeyportfolioapp.usermanagement.firebaseauth

import android.util.Log
import com.example.sergeyportfolioapp.usermanagement.firebaseauth.model.UserForFirebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.math.log

class FirebaseRepositoryImpl @Inject constructor(private val firebaseAuth: FirebaseAuth)
    : FirebaseRepository {
    private val TAG = "FirebaseRepositoryImpl"
    override suspend fun createUser(userForFirebase: UserForFirebase): AuthResult? {
        return firebaseAuth.createUserWithEmailAndPassword(
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

    override suspend fun logToUser(tokenID: String) {
        firebaseAuth
            .signInWithCustomToken(tokenID)
            .await()
    }

    override fun logOffFromCurrentUser() {
        firebaseAuth.signOut()
    }

    override fun getUserDisplayName(): String {
        if(firebaseAuth.currentUser?.displayName == null){
            return firebaseAuth.currentUser?.email!!;
        }
        return firebaseAuth.currentUser?.displayName!!
    }


}