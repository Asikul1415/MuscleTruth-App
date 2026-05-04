package com.example.muscletruth.data.repository

import android.content.Context
import android.net.Uri
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
import com.example.muscletruth.data.repository.UserRepository.localDb
import com.example.muscletruth.data.serviceClasses.CaloriesChartData
import com.example.muscletruth.utils.Utils
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

object MealRepository {
    private val apiService = ApiClient.apiService

    suspend fun addMeal(meal: Meal, image: MultipartBody.Part? = null, localImage: Uri? = null, context: Context): Result<Meal> {
        var serverMeal: Meal? = null
        return try {
            if(checkForInternetConnection()){
                val mealJson = Gson().toJson(meal)
                val mealBody = mealJson.toRequestBody("application/json".toMediaTypeOrNull())

                serverMeal = apiService.addMeal(mealBody, image).body()
            }

            if(serverMeal !== null){
                if(meal.localID !== null){
                    serverMeal.localID = meal.localID
                }
                else{
                    serverMeal.localID = UUID.randomUUID().toString()
                }

                if(meal.localPicture !== null){
                    serverMeal.localPicture = meal.localPicture
                }
                else{
                    serverMeal.localPicture = Utils.ImageUtils.saveImageFromServer(context, Utils.ImageUtils.getImagePath(serverMeal.serverPicture!!))
                }

                localDb.mealDao().insert(serverMeal)
                Log.d("APP_DEBUG", "ADDED MEAL $serverMeal")
                Result.success(serverMeal)
            }
            else{
                if(localImage !== null){
                    meal.localPicture = Utils.ImageUtils.copyImageToLocalStorage(context, localImage)
                }
                meal.creationDate = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                localDb.mealDao().insert(meal)

                Log.d("APP_DEBUG", "ADDED MEAL $meal")
                Result.success(meal)
            }
        }
        catch(e: Exception){
            Log.e("APP_DEBUG", "addMeal() ERROR: ${e.toString()}")
            Result.failure(Exception("addMeal() ERROR: ${e.toString()}"))
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
            Log.e("APP_DEBUG", "updateMeal() ERROR: ${e.toString()}")
            return false
        }
    }

    suspend fun getTodayMeals(): List<Meal>{
        try{
            if(checkForInternetConnection()){
                val meals = apiService.getTodayMeals()
                Log.d("APP_DEBUG", "TODAY_MEAL: $meals")
                return meals.map{meal ->
                    val localMeal = localDb.mealDao().getServerMeal(meal.serverID)
                    if(localMeal !== null){
                        meal.copy(localID = localMeal.localID)
                    }
                    else meal
                }
            }

            val meals = localDb.mealDao().getTodayMeals(LocalDate.now().toString())
            Log.d("APP_DEBUG", "TODAY_MEAL: $meals")
            return meals
        }
        catch(e: Exception){
            Log.e("APP_DEBUG", "getTodayMeals() ERROR: ${e.toString()}")
            return emptyList()
        }
    }

    suspend fun getMeal(mealID: Int, localMealID: String? = null): Meal?{
        try{
            if(checkForInternetConnection()){
                return apiService.getMeal(mealID)
            }
            else{
                if(localMealID !== null && mealID === -1){
                    return localDb.mealDao().getLocalMeal(localMealID)
                }
                else{
                    return localDb.mealDao().getServerMeal(mealID)
                }
            }
        }
        catch(e: Exception){
            Log.e("APP_DEBUG", "ERROR: ${e.toString()}")
            return null
        }
    }

    suspend fun deleteMeal(meal: Meal? = null, mealID: Int? = null){
        try{
            if(checkForInternetConnection()){
                if(meal !== null && meal.serverID !== -1){
                    apiService.deleteMeal(meal.serverID)
                }
                else if(mealID !== null && mealID !== -1){
                    apiService.deleteMeal(mealID)
                }
                else{
                    Log.e("APP_DEBUG", "DELETE MEAL: MEAL WASN'T DELETED")
                }
            }

            if(meal !== null){
                val localMeal = localDb.mealDao().getLocalMeal(meal.localID)
                localDb.mealDao().delete(localMeal!!)
                Log.d("APP_DEBUG", "DELETE: MEAL $meal WAS DELETED")
            }
            else if(mealID !== null && mealID !== -1){
                val localMeal = localDb.mealDao().getServerMeal(mealID)
                localDb.mealDao().delete(localMeal!!)
                Log.d("APP_DEBUG", "DELETE: MEAL $meal WAS DELETED")
            }
            else{
                Log.e("APP_DEBUG", "DELETE MEAL: MEAL WASN'T DELETED")
            }
        }
        catch(e: Exception){
            Log.e("APP_DEBUG", "DELETE MEAL ERROR: ${e.toString()}")
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
                localDb.servingDao().getServerMealServings(meal.serverID!!).forEach { serving ->
                    val product = localDb.productDao().getServerProduct(serving.productID)!!
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
            Log.e("APP_DEBUG", "getMealTypeTotal() ERROR: ${e.toString()}")
            return null
        }
    }

    suspend fun getAverageCaloriesYearChartData(): List<CaloriesChartData.ChartDataByWeek>{
        try{
            val data = apiService.getAverageCaloriesYearChartData()

            Log.d("APP_DEBUG", "GET CALORIES DATA FOR A YEAR: $data")
            return data
        }
        catch(e: Exception){
            Log.e("APP_DEBUG", "GET CALORIES DATA FOR A YEAR ERROR: ${e.toString()}")
            return emptyList()
        }
    }

    suspend fun getAverageCaloriesMonthChartData(): List<CaloriesChartData.ChartDataByDay>{
        try{
            val data = apiService.getAverageCaloriesMonthChartData()

            Log.d("APP_DEBUG", "GET CALORIES DATA FOR A MONTH: $data")
            return data
        }
        catch(e: Exception){
            Log.e("APP_DEBUG", "GET CALORIES DATA FOR A MONTH ERROR: ${e.toString()}")
            return emptyList()
        }
    }

    suspend fun getAverageCaloriesWeekChartData(): List<CaloriesChartData.ChartDataByDay>{
        try{
            val data = apiService.getAverageCaloriesWeekChartData()

            Log.d("APP_DEBUG", "GET CALORIES DATA FOR A WEEK: $data")
            return data
        }
        catch(e: Exception){
            Log.e("APP_DEBUG", "GET CALORIES DATA FOR A WEEK ERROR: ${e.toString()}")
            return emptyList()
        }
    }
}