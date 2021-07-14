package com.leschnitzky.dailyshiba.usermanagement.repository.room.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @ColumnInfo(name = "email") val email : String,
    @ColumnInfo(name = "display_name") val displayName: String,
    @ColumnInfo(name = "current_photo_path") val currPhotos: ArrayList<String>?,
    @ColumnInfo(name = "original_url_map") val originalURLMap : Map<String,String>
    ){

    @PrimaryKey(autoGenerate = true) var id: Long = 0

    @Ignore constructor() : this("","", arrayListOf(), mapOf())
}

