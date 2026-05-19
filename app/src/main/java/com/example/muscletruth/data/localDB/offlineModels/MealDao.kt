package com.example.muscletruth.data.localDB.offlineModels

import androidx.room.*
import com.example.muscletruth.data.models.Meal
import com.example.muscletruth.data.models.SavedMeal

@Dao
interface MealDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(meal: Meal)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(meals: List<Meal>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedMeal(meal: SavedMeal)

    @Update
    suspend fun update(meal: Meal)

    @Delete
    suspend fun delete(meal: Meal)

    @Delete
    suspend fun deleteSavedMeal(savedMeal: SavedMeal)

    @Query("SELECT * FROM meals WHERE substr(creation_date, 1, 10) = :today")
    suspend fun getTodayMeals(today: String): List<Meal>

    @Query("SELECT * from meals WHERE server_id = :mealID")
    suspend fun getServerMeal(mealID: Int): Meal?

    @Query("SELECT * from meals WHERE local_id = :mealID")
    suspend fun getLocalMeal(mealID: String): Meal?

    @Query("SELECT * from meals WHERE meal_type_id = :mealTypeID AND substr(creation_date, 1, 10) = Fяsubstr(:date, 1, 10)")
    suspend fun getMealTypeMeals(mealTypeID: Int, date: String): MutableList<Meal>

    @Query("SELECT * FROM meals WHERE creation_date LIKE '%' || substr(:date, 1, 10) || '%'")
    suspend fun getMeals(date: String? = null): List<Meal>

    @Query("SELECT * FROM meals WHERE server_id = -1")
    suspend fun getMealsForSync(): List<Meal>

    @Query("SELECT * FROM meals WHERE was_updated = 1")
    suspend fun getMealsForUpdate(): List<Meal>

    @Query("SELECT * FROM saved_meals")
    suspend fun getSavedMeals(): List<SavedMeal>

    @Query("SELECT * FROM saved_meals WHERE meal_local_id = :mealLocalID OR meal_server_id = :mealServerID")
    suspend fun getSavedMeal(mealServerID: Int?, mealLocalID: String?): SavedMeal?
}