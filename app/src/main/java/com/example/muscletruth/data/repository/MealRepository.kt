package com.example.muscletruth.data.repository

import android.util.Log
import com.example.muscletruth.data.api.ApiClient
import com.example.muscletruth.data.serviceClasses.MealType
import com.example.muscletruth.data.models.Meal
import com.example.muscletruth.utils.Utils.NetworkUtils.checkForInternetConnection
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.LocalDate
import java.time.ZonedDateTime
import com.example.muscletruth.data.repository.UserRepository.localDb
import java.util.UUID

object MealRepository {
    private val apiService = ApiClient.apiService

    suspend fun addMeal(meal: Meal, image: MultipartBody.Part? = null): Result<Meal> {
        var serverMeal: Meal? = null
        return try {
            if(checkForInternetConnection()){
                val mealJson = Gson().toJson(meal)
                val mealBody = mealJson.toRequestBody("application/json".toMediaTypeOrNull())

                serverMeal = apiService.addMeal(mealBody, image).body()
            }

            if(serverMeal !== null){
                serverMeal.localID = UUID.randomUUID().toString()
                localDb.mealDao().insert(serverMeal)
                Log.d("APP_DEBUG", "ADDED MEAL $serverMeal")
                Result.success(serverMeal)
            }
            else{
                Result.failure(Exception("Не удалось сохранить локально прием пищи."))
            }
        }
        catch(e: Exception){
            Result.failure(Exception("${e.toString()}"))
        }
    }

    suspend fun updateMeal(mealID: Int, meal: Meal, image: MultipartBody.Part?): Boolean{
        try {
            if(checkForInternetConnection()){
                val mealJson = Gson().toJson(meal)
                val mealBody = mealJson.toRequestBody("application/json".toMediaTypeOrNull())

                return apiService.updateMeal(mealID,mealBody, image)
            }
            else{

                localDb.mealDao().update(meal)
                return true
            }
        } catch (e: Exception) {
            Log.e("APP_DEBUG", "${e.toString()}")
            return false
        }
    }

    suspend fun getTodayMeals(): List<Meal>{
        try{
            if(checkForInternetConnection()){
                return apiService.getTodayMeals()
            }
            return localDb.mealDao().getTodayMeals(LocalDate.now().toString())
        }
        catch(e: Exception){
            Log.e("APP_DEBUG", "ERROR: ${e.toString()}")
            return emptyList()
        }
    }

    suspend fun getMeal(mealID: Int): Meal?{
        try{
            if(checkForInternetConnection()){
                return apiService.getMeal(mealID)
            }
            else{
                return localDb.mealDao().getMeal(mealID)
            }
        }
        catch(e: Exception){
            Log.e("APP_DEBUG", "ERROR: ${e.toString()}")
            return null
        }
    }

    suspend fun deleteMeal(mealID: Int){
        try{
            if(checkForInternetConnection()){
                apiService.deleteMeal(mealID)
            }
            val meal = localDb.mealDao().getMeal(mealID)
            localDb.mealDao().delete(meal!!)
            Log.d("APP_DEBUG", "DELETE: MEAL ${meal} WAS DELETED")
        }
        catch(e: Exception){
            Log.e("APP_DEBUG", "ERROR: ${e.toString()}")
        }
    }

    suspend fun getMealTypeTotal(mealTypeID: Int): MealType? {
        try{
            val todayDate = LocalDate.now().toString()
            if(checkForInternetConnection()){
                val response = apiService.getMealTypeTotal(mealTypeID,todayDate, todayDate)
                return response
            }
            val meals = localDb.mealDao().getMealTypeMeals(mealTypeID, todayDate)
            var totalProteins = 0.00f;
            var totalFats = 0.00f;
            var totalCarbs = 0.00f;

            meals.forEach { meal ->
                localDb.servingDao().getServings(meal.serverID!!).forEach { serving ->
                    val product = localDb.productDao().getProduct(serving.productID)!!
                    totalProteins += (product.proteins / 100.00f * serving.productAmount)
                    totalFats += (product.fats / 100.00f * serving.productAmount)
                    totalCarbs += (product.carbs / 100.00f * serving.productAmount)
                }
            }
            val mealTypeTotal = MealType(
                id = mealTypeID,
                title = null,
                proteins = totalProteins,
                fats = totalFats,
                carbs = totalCarbs,
                totalCalories = totalCarbs * 4 + totalProteins * 4 + totalFats * 9
            )
            Log.d("APP_DEBUG", "LOCAL_GET: MealType TOTAL: ${mealTypeTotal}")
            return mealTypeTotal

        }
        catch(e: Exception){
            Log.e("APP_DEBUG", "ERROR: ${e.toString()}")
            return null
        }
    }
}