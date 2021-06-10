package com.example.sergeyportfolioapp.usermanagement.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.sergeyportfolioapp.usermanagement.room.model.User

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE email LIKE :email LIMIT 1")
    suspend fun getDisplayNameByEmail(email: String) : List<User>

    @Insert
    suspend fun insertUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

}