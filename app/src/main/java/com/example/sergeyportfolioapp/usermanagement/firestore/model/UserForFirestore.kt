package com.example.sergeyportfolioapp.usermanagement.firestore.model

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot

data class UserForFirestore(val email: String,
                            var profilePicURI : String,
                            var favoritesList : List<String>){

    constructor(email: String) : this(email,"", listOf())

    companion object{
        fun fromMap(map : MutableMap<String,Any>) : UserForFirestore {
            return UserForFirestore(
                map["email"] as String,
                map["profile_pic"] as String,
                map["favoritesList"] as List<String>
            )
        }

        fun toMap(userForFirestore: UserForFirestore) : MutableMap<String, Any> {
            return mutableMapOf<String,Any>(
                "email" to userForFirestore.email,
                "profile_pic" to userForFirestore.profilePicURI,
                "list_favorite" to userForFirestore.favoritesList
            )
        }
    }
}