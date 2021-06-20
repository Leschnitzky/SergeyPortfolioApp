package com.example.sergeyportfolioapp.usermanagement.room.model

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @ColumnInfo(name = "email") val email : String,
    @ColumnInfo(name = "displayName") val displayName: String,
    @ColumnInfo(name = "favoritePhotos") val photos: ArrayList<String>?,
    @ColumnInfo(name = "availablePhotos") val availablePhotos: Int,
    @ColumnInfo(name = "currentPhotoPath") val currPhotos: ArrayList<String>?
    ){

    @PrimaryKey(autoGenerate = true) var id: Long = 0

    @Ignore constructor() : this("","", arrayListOf("get"),10, arrayListOf())
}

