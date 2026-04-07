package com.example.muscletruth.data.localDB.offlineModels

import androidx.room.*
import com.example.muscletruth.data.models.Serving

@Dao
interface ServingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(servingEntity: Serving)

    @Insert(onConflict = OnConflictStrategy.NONE)
    suspend fun insertAll(servings: List<Serving>)

    @Update
    suspend fun update(servingEntity: Serving)

    @Delete
    suspend fun delete(servingEntity: Serving)

    @Query("SELECT * from servings WHERE meal_id = :mealID")
    suspend fun getServings(mealID: Int): MutableList<Serving>

    @Query("SELECT * from servings WHERE server_id = :servingID")
    suspend fun getServing(servingID: Int): Serving

    @Query("SELECT * from servings WHERE server_id = -1")
    suspend fun getServingsForSync(): List<Serving>
}