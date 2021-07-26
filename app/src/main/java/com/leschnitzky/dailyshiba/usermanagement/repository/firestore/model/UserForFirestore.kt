package com.leschnitzky.dailyshiba.usermanagement.repository.firestore.model

import com.leschnitzky.dailyshiba.utils.DEFAULT_PROFILE_PIC

data class UserForFirestore(val email: String,
                            var displayName : String,
                            var profilePicURI : String,
                            var favoritesList : List<String>,
                            var currentPhotosList : List<String>){

    constructor(email: String) : this(email,"", DEFAULT_PROFILE_PIC, listOf(), listOf())
    constructor(email: String, name: String) : this(email,name, DEFAULT_PROFILE_PIC, listOf(), listOf())

    companion object{
        fun fromMap(map: Map<String,Any>) : UserForFirestore {
            return UserForFirestore(
                map["email"] as String,
                map["display_name"] as String,
                map["profile_pic"] as String,
                map["list_favorite"] as List<String>,
                map["current_photos"] as List<String>
            )
        }

        fun toMap(userForFirestore: UserForFirestore) : Map<String, Any> {
            return mutableMapOf<String,Any>(
                "email" to userForFirestore.email,
                "profile_pic" to userForFirestore.profilePicURI,
                "list_favorite" to userForFirestore.favoritesList,
                "display_name" to userForFirestore.displayName,
                "current_photos" to userForFirestore.currentPhotosList
            )
        }
    }
}