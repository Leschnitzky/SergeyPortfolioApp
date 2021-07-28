package com.leschnitzky.dailyshiba.usermanagement.repository.firestore.model

import com.leschnitzky.dailyshiba.usermanagement.repository.firestore.model.UserForFirestore.UserSettingsForFirestore.Companion.DEFAULT_USER_SETTINGS
import com.leschnitzky.dailyshiba.utils.DEFAULT_PROFILE_PIC

data class UserForFirestore(val email: String,
                            var displayName : String,
                            var profilePicURI : String,
                            var favoritesList : List<String>,
                            var currentPhotosList : List<String>,
                            var userSettings : UserSettingsForFirestore
                            ){

    data class UserSettingsForFirestore(val settings : Map<String, String  >) {

        companion object {
            val DEFAULT_USER_SETTINGS : UserSettingsForFirestore = UserSettingsForFirestore(mutableMapOf(
                "dogs" to "s1c0h0b0"
                )
            )
        }
    }

    constructor(email: String) : this(email,"", DEFAULT_PROFILE_PIC, listOf(), listOf(), DEFAULT_USER_SETTINGS)
    constructor(email: String, name: String) : this(email,name, DEFAULT_PROFILE_PIC, listOf(), listOf(), DEFAULT_USER_SETTINGS)

    companion object{
        fun fromMap(map: Map<String,Any>) : UserForFirestore {
            return UserForFirestore(
                map["email"] as String,
                map["display_name"] as String,
                map["profile_pic"] as String,
                map["list_favorite"] as List<String>,
                map["current_photos"] as List<String>,
                if(map["user_settings"] == null){
                    DEFAULT_USER_SETTINGS
                } else {
                    FireStoreConverter.stringToSettings(map["user_settings"] as String)
                }
            )
        }

        fun toMap(userForFirestore: UserForFirestore) : Map<String, Any> {
            return mutableMapOf<String,Any>(
                "email" to userForFirestore.email,
                "profile_pic" to userForFirestore.profilePicURI,
                "list_favorite" to userForFirestore.favoritesList,
                "display_name" to userForFirestore.displayName,
                "current_photos" to userForFirestore.currentPhotosList,
                "user_settings" to FireStoreConverter.settingsToString(userForFirestore.userSettings)
            )
        }
    }
}