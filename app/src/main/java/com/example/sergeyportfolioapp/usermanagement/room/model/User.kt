package com.example.sergeyportfolioapp.usermanagement.room.model

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @ColumnInfo(name = "emailPass") val email_pass : String,
    @ColumnInfo(name = "userToken") val token: String){

    @PrimaryKey(autoGenerate = true) var id: Long = 0

    @Ignore constructor() : this("","")
}

