package com.example.muscletruth.data.localDB.offlineModels

import androidx.room.*
import com.example.muscletruth.data.models.RecentServing
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

    @Delete
    suspend fun delete(serving: RecentServing)

    @Query("SELECT * from servings WHERE meal_id = :mealServerID")
    suspend fun getServerMealServings(mealServerID: Int): MutableList<Serving>

    @Query("SELECT * from servings WHERE local_meal_id = :localMealID")
    suspend fun getLocalMealServings(localMealID: String?): MutableList<Serving>

    @Query("SELECT * from servings")
    suspend fun getServings(): MutableList<Serving>

    @Query("SELECT * from servings WHERE server_id = :servingID")
    suspend fun getServerServing(servingID: Int): Serving

    @Query("SELECT * from servings WHERE local_id = :servingLocalID")
    suspend fun getLocalServing(servingLocalID: String): Serving

    @Query("SELECT * from servings WHERE server_id = -1")
    suspend fun getServingsForSync(): List<Serving>

    @Query("SELECT * FROM recent_servings WHERE serving_server_id = :servingServerID OR serving_local_id = :localServingID")
    suspend fun getRecentServing(servingServerID: Int?, localServingID: String?): RecentServing?

    @Query("SELECT * FROM recent_servings ORDER BY use_date DESC LIMIT 50")
    suspend fun getRecentServings(): List<RecentServing>
}