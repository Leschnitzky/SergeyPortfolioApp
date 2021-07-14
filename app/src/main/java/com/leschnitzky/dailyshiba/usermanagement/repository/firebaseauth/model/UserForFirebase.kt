package com.leschnitzky.dailyshiba.usermanagement.repository.firebaseauth.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class UserForFirebase(val email: String, val password: String) {
    // Null default values create a no-argument default constructor, which is needed
    // for deserialization from a DataSnapshot.
}