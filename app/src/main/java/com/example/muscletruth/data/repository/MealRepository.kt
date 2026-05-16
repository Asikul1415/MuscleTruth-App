package com.example.muscletruth.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.muscletruth.data.api.ApiClient
import com.example.muscletruth.data.serviceClasses.MealType
import com.example.muscletruth.data.models.Meal
import com.example.muscletruth.data.models.Serving
import com.example.muscletruth.utils.Utils.NetworkUtils.checkForInternetConnection
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.LocalDate
import com.example.muscletruth.data.repository.UserRepository.localDb
import com.example.muscletruth.data.serviceClasses.CaloriesChartData
import com.example.muscletruth.utils.Utils
import java.time.DayOfWeek
import java.time.OffsetDateTime
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
                    if(serverMeal.serverPicture !== null){
                        serverMeal.localPicture = Utils.ImageUtils.saveImageFromServer(context, Utils.ImageUtils.getImagePath(serverMeal.serverPicture!!))
                    }
                }

                localDb.mealDao().insert(serverMeal)
                Log.d("APP_DEBUG", "MEAL ADD: ADDED MEAL $serverMeal")
                Result.success(serverMeal)
            }
            else{
                if(localImage !== null){
                    meal.localPicture = Utils.ImageUtils.copyImageToLocalStorage(context, localImage)
                }
                meal.creationDate = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                localDb.mealDao().insert(meal)

                Log.d("APP_DEBUG", "MEAL ADD: ADDED MEAL $meal")
                Result.success(meal)
            }
        }
        catch(e: Exception){
            Log.e("APP_DEBUG", "addMeal() ERROR: ${e.toString()}")
            Result.failure(Exception("addMeal() ERROR: ${e.toString()}"))
        }
    }

    suspend fun updateMeal(meal: Meal, localImage: Uri? = null, image: MultipartBody.Part? = null, context: Context): Boolean{
        try {
            if(checkForInternetConnection()){
                val mealJson = Gson().toJson(meal)
                val mealBody = mealJson.toRequestBody("application/json".toMediaTypeOrNull())

                Log.d("APP_DEBUG", "MEAL UPDATE: SUCCESS")
                return apiService.updateMeal(meal.serverID,mealBody, image)
            }
            else{
                if(localImage !== null){
                    meal.localPicture = Utils.ImageUtils.copyImageToLocalStorage(context, localImage)
                }
                localDb.mealDao().update(meal)
                Log.d("APP_DEBUG", "MEAL UPDATE: SUCCESS")
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

    suspend fun getMeals(): List<Meal>{
        try{
            if(checkForInternetConnection()){
                val meals = apiService.getMeals(null, null)

                return meals.map{meal ->
                    val localMeal = localDb.mealDao().getServerMeal(meal.serverID)
                    if(localMeal !== null){
                        meal.copy(localID = localMeal.localID)
                    }
                    else meal
                }
            }

            val meals = localDb.mealDao().getMeals()
            return meals
        }
        catch(e: Exception){
            Log.e("APP_DEBUG", "getMeals() ERROR: ${e.toString()}")
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

    suspend fun deleteMeal(mealServerID: Int? = null, mealLocalID: String? = null){
        try{
            if(checkForInternetConnection()){
                if(mealServerID !== null && mealServerID != -1){
                    apiService.deleteMeal(mealServerID)
                }
                else{
                    Log.e("APP_DEBUG", "DELETE MEAL: MEAL WASN'T DELETED")
                }
            }

            if((mealServerID !== null && mealServerID != -1) || mealLocalID !== null){
                var servings: MutableList<Serving>
                var localMeal: Meal?
                 if(mealServerID !== null && mealServerID != -1) {
                    localMeal = localDb.mealDao().getServerMeal(mealServerID)
                    servings = localDb.servingDao().getServerMealServings(mealServerID)
                }
                else{
                     localMeal = localDb.mealDao().getLocalMeal(mealLocalID!!)
                     servings = localDb.servingDao().getLocalMealServings(mealLocalID)
                }

                localDb.mealDao().delete(localMeal!!)
                servings.forEach {serving ->
                    val recentServing = localDb.servingDao().getRecentServing(serving.serverID, serving.localID)
                    if(recentServing !== null){
                        localDb.servingDao().delete(recentServing)
                    }
                    localDb.servingDao().delete(serving)
                }
                Log.d("APP_DEBUG", "DELETE MEAL: MEAL $localMeal WAS DELETED")
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
            if(checkForInternetConnection()){
                val data = apiService.getAverageCaloriesYearChartData()

                Log.d("APP_DEBUG", "CALORIES CHART YEAR DATA: DATA - $data")
                return data
            }
            else{
                val end = LocalDate.now()
                val start = end.minusYears(1)

                val meals = localDb.mealDao().getMeals().filter{meal ->
                    val creationDate = OffsetDateTime.parse(meal.creationDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDate()

                    (creationDate.isAfter(start) || creationDate.isEqual(start) && (creationDate.isBefore(end) || creationDate.isEqual(end)))
                }

                //day, list of calories per meal
                val mealsTotalPerDay = mutableMapOf<LocalDate, MutableList<Double>>();
                meals.forEach {meal ->
                    var caloriesInMeal = 0.00

                    val mealServings = localDb.servingDao().getLocalMealServings(meal.localID)
                    mealServings.forEach {serving ->
                        if(serving.localProductID !== null){
                            val product = localDb.productDao().getLocalProduct(serving.localProductID!!)
                            if(product !== null){
                                caloriesInMeal += (4 * product.proteins + 4 * product.carbs + 9 * product.fats) * (serving.productAmount / 100)
                            }
                        }
                    }

                    val date = OffsetDateTime.parse(meal.creationDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDate()
                    if(mealsTotalPerDay.keys.contains(date) === false){
                        mealsTotalPerDay[date] = mutableListOf<Double>()
                    }
                    mealsTotalPerDay[date]?.add(caloriesInMeal)
                }

                val caloriesPerDay = mutableListOf<CaloriesChartData.ChartDataByDay>()
                mealsTotalPerDay.entries.forEach {group ->
                    caloriesPerDay.add(CaloriesChartData.ChartDataByDay(
                        day = group.key.toString(),
                        total_calories = group.value.sum()
                    ))
                }

                // week start date, list of calories per day
                val caloriesPerWeek = mutableMapOf<LocalDate, MutableList<Double>>()
                caloriesPerDay.forEach {data ->
                    val weekStartDate = LocalDate.parse(data.day).with(DayOfWeek.MONDAY)
                    if(caloriesPerWeek.keys.contains(weekStartDate) === false){
                        caloriesPerWeek[weekStartDate] = mutableListOf<Double>()
                    }
                    caloriesPerWeek[weekStartDate]?.add(data.total_calories)
                }

                val yearChartData = mutableListOf<CaloriesChartData.ChartDataByWeek>()
                caloriesPerWeek.entries.forEach {data ->
                    yearChartData.add(CaloriesChartData.ChartDataByWeek(
                        week_start = data.key.toString(),
                        average_calories = data.value.sum() / data.value.count()
                    ))
                }

                Log.d("APP_DEBUG", "CALORIES CHART YEAR DATA: DATA - $yearChartData")
                return yearChartData
            }
        }
        catch(e: Exception){
            Log.e("APP_DEBUG", "GET CALORIES DATA FOR A YEAR ERROR: ${e.toString()}")
            return emptyList()
        }
    }

    suspend fun getAverageCaloriesMonthChartData(): List<CaloriesChartData.ChartDataByDay>{
        try{
            if(checkForInternetConnection()){
                val data = apiService.getAverageCaloriesMonthChartData()

                Log.d("APP_DEBUG", "GET CALORIES DATA FOR A MONTH: $data")
                return data
            }
            else{
                val end = LocalDate.now()
                val start = end.minusMonths(1)

                val meals = localDb.mealDao().getMeals().filter{meal ->
                    val creationDate = OffsetDateTime.parse(meal.creationDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDate()

                    (creationDate.isAfter(start) || creationDate.isEqual(start) && (creationDate.isBefore(end) || creationDate.isEqual(end)))
                }

                val mealsTotalPerDay = mutableMapOf<LocalDate, MutableList<Double>>();
                meals.forEach {meal ->
                    var caloriesInMeal = 0.00

                    val mealServings = localDb.servingDao().getLocalMealServings(meal.localID)
                    mealServings.forEach {serving ->
                        if(serving.localProductID !== null){
                            val product = localDb.productDao().getLocalProduct(serving.localProductID!!)
                            if(product !== null){
                                caloriesInMeal += (4 * product.proteins + 4 * product.carbs + 9 * product.fats) * (serving.productAmount / 100)
                            }
                        }
                    }

                    val date = OffsetDateTime.parse(meal.creationDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDate()
                    if(mealsTotalPerDay.keys.contains(date) === false){
                        mealsTotalPerDay[date] = mutableListOf<Double>()
                    }
                    mealsTotalPerDay[date]?.add(caloriesInMeal)
                }

                val caloriesPerDay = mutableListOf<CaloriesChartData.ChartDataByDay>()
                mealsTotalPerDay.entries.forEach {group ->
                    caloriesPerDay.add(CaloriesChartData.ChartDataByDay(
                        day = group.key.toString(),
                        total_calories = group.value.sum()
                    ))
                }

                Log.d("APP_DEBUG", "CALORIES CHART MONTH DATA: DATA - $caloriesPerDay")
                return caloriesPerDay
            }
        }
        catch(e: Exception){
            Log.e("APP_DEBUG", "CALORIES CHART MONTH DATA ERROR: ${e.toString()}")
            return emptyList()
        }
    }

    suspend fun getAverageCaloriesWeekChartData(): List<CaloriesChartData.ChartDataByDay>{
        try{
            if(checkForInternetConnection()){
                val data = apiService.getAverageCaloriesWeekChartData()

                Log.d("APP_DEBUG", "CALORIES CHART WEEK DATA: DATA: $data")
                return data
            }
            else{
                val end = LocalDate.now()
                val start = end.minusWeeks(1)

                val meals = localDb.mealDao().getMeals().filter{meal ->
                    val creationDate = OffsetDateTime.parse(meal.creationDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDate()

                    (creationDate.isAfter(start) || creationDate.isEqual(start) && (creationDate.isBefore(end) || creationDate.isEqual(end)))
                }

                val mealsTotalPerDay = mutableMapOf<LocalDate, MutableList<Double>>();
                meals.forEach {meal ->
                    var caloriesInMeal = 0.00

                    val mealServings = localDb.servingDao().getLocalMealServings(meal.localID)
                    mealServings.forEach {serving ->
                        if(serving.localProductID !== null){
                            val product = localDb.productDao().getLocalProduct(serving.localProductID!!)
                            if(product !== null){
                                caloriesInMeal += (4 * product.proteins + 4 * product.carbs + 9 * product.fats) * (serving.productAmount / 100)
                            }
                        }
                    }

                    val date = OffsetDateTime.parse(meal.creationDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDate()
                    if(mealsTotalPerDay.keys.contains(date) === false){
                        mealsTotalPerDay[date] = mutableListOf<Double>()
                    }
                    mealsTotalPerDay[date]?.add(caloriesInMeal)
                }

                val caloriesPerDay = mutableListOf<CaloriesChartData.ChartDataByDay>()
                mealsTotalPerDay.entries.forEach {group ->
                    caloriesPerDay.add(CaloriesChartData.ChartDataByDay(
                        day = group.key.toString(),
                        total_calories = group.value.sum()
                    ))
                }

                Log.d("APP_DEBUG", "CALORIES CHART WEEK DATA: DATA: $caloriesPerDay")
                return caloriesPerDay
            }
        }
        catch(e: Exception){
            Log.e("APP_DEBUG", "CALORIES CHART WEEK DATA ERROR: ${e.toString()}")
            return emptyList()
        }
    }
}