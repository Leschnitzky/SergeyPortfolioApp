package com.example.sergeyportfolioapp.usermanagement.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.sergeyportfolioapp.usermanagement.room.model.User

@Database(entities = [User::class], version = 4)
abstract class LocalUserDatabase : RoomDatabase() {
    abstract fun usersDao() : UserDao
}