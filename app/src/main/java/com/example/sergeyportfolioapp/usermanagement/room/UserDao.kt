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

    @Query("SELECT currentPhotoPath FROM users WHERE email LIKE :email LIMIT 1")
    suspend fun getCurrentPhotosByEmail(email: String) : List<String>

    @Query("SELECT favoritePhotos FROM users WHERE email LIKE :email LIMIT 1")
    suspend fun getFavoritePhotosByEmail(email: String) : List<String>

    @Query("SELECT availablePhotos FROM users WHERE email LIKE :email LIMIT 1")
    suspend fun getNumOfAvailableNewPhotosByEmail(email: String) : List<Int>

    @Query("UPDATE users SET availablePhotos=:num WHERE email LIKE :email")
    suspend fun updateNumOfAvailablePhotos(num : Int, email: String)

    @Query("UPDATE users SET currentPhotoPath=:photos WHERE email LIKE :email")
    suspend fun updateCurrentPhotosByMail(photos : ArrayList<String>, email: String)

    @Query("UPDATE users SET favoritePhotos=:favoritePhotos WHERE email LIKE :email")
    suspend fun updateFavoritePhotosByMail(favoritePhotos : ArrayList<String>, email: String)

    @Insert
    suspend fun insertUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

}