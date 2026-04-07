package com.example.muscletruth.data.localDB.offlineModels

import androidx.room.*
import com.example.muscletruth.data.models.Meal

@Dao
interface MealDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(meal: Meal)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(meals: List<Meal>)

    @Update
    suspend fun update(meal: Meal)

    @Delete
    suspend fun delete(meal: Meal)

    @Query("SELECT * FROM meals WHERE substr(creation_date, 1, 10) = :today")
    suspend fun getTodayMeals(today: String): List<Meal>

    @Query("SELECT * from meals WHERE server_id = :mealID")
    suspend fun getMeal(mealID: Int): Meal?

    @Query("SELECT * from meals WHERE meal_type_id = :mealTypeID AND substr(creation_date, 1, 10) = :today")
    suspend fun getMealTypeMeals(mealTypeID: Int, today: String): MutableList<Meal>

    @Query("SELECT * FROM meals WHERE server_id = -1")
    suspend fun getMealsForSync(): List<Meal>
}