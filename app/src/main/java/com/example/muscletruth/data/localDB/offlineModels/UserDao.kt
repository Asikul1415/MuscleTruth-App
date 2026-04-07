package com.example.muscletruth.data.localDB.offlineModels

import androidx.room.*
import com.example.muscletruth.data.models.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    @Query("SELECT * from users WHERE server_id = :userID")
    suspend fun getUser(userID: Int): User
}