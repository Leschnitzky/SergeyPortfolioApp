package com.leschnitzky.dailyshiba.usermanagement.repository.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.leschnitzky.dailyshiba.usermanagement.repository.room.model.UserTypeConverter
import com.leschnitzky.dailyshiba.usermanagement.repository.room.model.User

@Database(entities = [User::class], version = 9)
@TypeConverters(UserTypeConverter::class)
abstract class LocalUserDatabase : RoomDatabase() {
    abstract fun usersDao() : UserDao
}