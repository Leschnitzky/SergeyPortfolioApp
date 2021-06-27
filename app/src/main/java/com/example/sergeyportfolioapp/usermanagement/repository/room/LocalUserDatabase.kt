package com.example.sergeyportfolioapp.usermanagement.repository.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.sergeyportfolioapp.usermanagement.repository.room.model.UserTypeConverter
import com.example.sergeyportfolioapp.usermanagement.repository.room.model.User

@Database(entities = [User::class], version = 8)
@TypeConverters(UserTypeConverter::class)
abstract class LocalUserDatabase : RoomDatabase() {
    abstract fun usersDao() : UserDao
}