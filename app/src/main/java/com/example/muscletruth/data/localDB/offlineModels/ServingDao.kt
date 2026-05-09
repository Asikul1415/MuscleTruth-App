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
    suspend fun update(serving: Serving)

    @Delete
    suspend fun delete(serving: Serving)

    @Query("SELECT * from servings WHERE meal_id = :mealServerID")
    suspend fun getServerMealServings(mealServerID: Int): MutableList<Serving>

    @Query("SELECT * from servings WHERE local_meal_id = :localMealID")
    suspend fun getLocalMealServings(localMealID: String?): MutableList<Serving>

    @Query("SELECT * from servings")
    suspend fun getServings(): MutableList<Serving>

    @Query("SELECT * from servings WHERE server_id = :servingID")
    suspend fun getServing(servingID: Int): Serving

    @Query("SELECT * from servings WHERE server_id = -1")
    suspend fun getServingsForSync(): List<Serving>
}