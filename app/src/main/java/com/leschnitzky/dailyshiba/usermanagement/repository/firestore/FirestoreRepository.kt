package com.leschnitzky.dailyshiba.usermanagement.repository.firestore

import com.leschnitzky.dailyshiba.usermanagement.repository.firestore.model.UserForFirestore

interface FirestoreRepository {
    suspend fun addNewUserToFirestore(email: String)
    suspend fun addNewUserToFirestore(email: String,name: String)
    suspend fun addNewUserToFirestore(email: String, name : String, profilePic : String)
    suspend fun getUserFromFirestore(email: String): UserForFirestore
    suspend fun updateUserFromFirestore(userForFirestore: UserForFirestore)
    suspend fun updateUserPhotos(email: String, urlsFromServer: List<String>)
    suspend fun doesUserExist(currentUserEmail: String): Boolean
}