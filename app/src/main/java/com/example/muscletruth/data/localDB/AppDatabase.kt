package com.example.muscletruth.data.localDB

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.muscletruth.data.localDB.offlineModels.MealDao
import com.example.muscletruth.data.localDB.offlineModels.ProductDao
import com.example.muscletruth.data.localDB.offlineModels.ServingDao
import com.example.muscletruth.data.localDB.offlineModels.UserDao
import com.example.muscletruth.data.localDB.offlineModels.WeightingDao
import com.example.muscletruth.data.models.FavouriteProduct
import com.example.muscletruth.data.models.Meal
import com.example.muscletruth.data.models.Product
import com.example.muscletruth.data.models.RecentServing
import com.example.muscletruth.data.models.Serving
import com.example.muscletruth.data.models.User
import com.example.muscletruth.data.models.Weighting

@Database(entities = [
    User::class,
    Serving::class,
    Meal::class,
    Weighting::class,
    Product::class,
    FavouriteProduct::class,
    RecentServing::class,
    SavedMeal::class,
    ],
    version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun servingDao(): ServingDao
    abstract  fun mealDao(): MealDao
    abstract fun weightingDao(): WeightingDao
    abstract fun productDao(): ProductDao
}