package com.example.sergeyportfolioapp.usermanagement.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.sergeyportfolioapp.usermanagement.room.model.User

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE emailPass LIKE :emailAndPassword LIMIT 1")
    suspend fun getTokenByEmailAndPassword(emailAndPassword: String) : List<User>

    @Insert
    suspend fun insertUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    @Query("UPDATE users SET userToken = :token WHERE emailPass =:emailPass")
    fun updateTokenForUser(token: String, emailPass: String)

}