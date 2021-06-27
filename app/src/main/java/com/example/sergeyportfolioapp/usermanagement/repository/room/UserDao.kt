package com.example.sergeyportfolioapp.usermanagement.repository.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.sergeyportfolioapp.usermanagement.repository.room.model.User

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE email LIKE :email LIMIT 1")
    suspend fun getDisplayNameByEmail(email: String) : List<User>

    @Query("SELECT current_photo_path FROM users WHERE email LIKE :email LIMIT 1")
    suspend fun getCurrentPhotosByEmail(email: String) : List<String>

    @Query("SELECT original_url_map FROM users WHERE email LIKE :email LIMIT 1")
    suspend fun getCurrentURLMapByEmail(email: String) : List<String>

    @Query("UPDATE users SET current_photo_path=:photos WHERE email LIKE :email")
    suspend fun updateCurrentPhotosByMail(photos : ArrayList<String>, email: String)

    @Query("UPDATE users set original_url_map=:map WHERE email LIKE :email")
    suspend fun updateCurrentPhotoURLMapByMail(email: String, map: Map<String,String>)


    @Insert
    suspend fun insertUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

}