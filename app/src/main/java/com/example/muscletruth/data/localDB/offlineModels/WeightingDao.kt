package com.example.muscletruth.data.localDB.offlineModels

import androidx.room.*
import com.example.muscletruth.data.models.Weighting

@Dao
interface WeightingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(weighting: Weighting)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(weightings: List<Weighting>)

    @Update
    suspend fun update(weighting: Weighting)

    @Delete
    suspend fun delete(weighting: Weighting)

    @Query("SELECT * from weightings ORDER BY server_id DESC LIMIT(1)")
    suspend fun getLastWeighting(): Weighting

    @Query("SELECT * from weightings")
    suspend fun getWeightings(): List<Weighting>

    @Query("SELECT * from weightings WHERE substr(creation_date, 1, 10) BETWEEN :startDate AND :endDate")
    suspend fun getWeightingsByDate(startDate: String? = null, endDate: String? = null): List<Weighting>

    @Query("SELECT * FROM weightings WHERE substr(creation_date, 1, 10) >= :startDate")
    suspend fun getWeightingsFrom(startDate: String): List<Weighting>

    @Query("SELECT * from weightings WHERE server_id = -1")
    suspend fun getWeightingsForSync(): List<Weighting>

    @Query("SELECT * from weightings WHERE local_id = :id")
    suspend fun getWeighting(id: String): Weighting

    @Query("SELECT * from weightings WHERE server_id = :id")
    suspend fun getServerWeighting(id: Int): Weighting
}