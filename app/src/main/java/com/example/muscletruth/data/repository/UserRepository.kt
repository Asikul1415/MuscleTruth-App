package com.example.muscletruth.data.repository
import com.example.muscletruth.data.api.ApiClient
import com.example.muscletruth.data.api.models.*
import android.util.Log
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.LocalDate

class UserRepository {
    private val apiService = ApiClient.apiService

    suspend fun registerUser(user: User.UserCreate, image: MultipartBody.Part? = null): Result<User.LoginResponse> {
        return try {
            val userJson = Gson().toJson(user)
            val userBody = userJson.toRequestBody("application/json".toMediaTypeOrNull())

            val response = apiService.createUser(userBody, image)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Registration failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<User.LoginResponse> {
        return try {
            val response = apiService.authorizeUser(User.UserLogin(email, password))
            if (response.isSuccessful) {
                response.body()?.let { authResponse ->
                    ApiClient.setAuthToken(authResponse.accessToken)
                    Result.success(authResponse)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Login failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("APP_DEBUG", "${e.toString()}")
            Result.failure(e)
        }
    }

    suspend fun checkEmail(email: String): Boolean{
        val response = apiService.checkEmail(User.Email(email))

        return response.response
    }

    suspend fun getUser(): Result<User.UserResponse> {
        return try {
            val response = apiService.getUser()
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Ошибка получения профиля!"))
            }
        } catch (e: Exception) {
            Log.e("APP_DEBUG", "${e.toString()}")
            Result.failure(e)
        }
    }

    suspend fun checkUserPassword(password: String): User.CheckPasswordResponse{
        val response = apiService.checkUserPassword(User.Password(password))
        return User.CheckPasswordResponse(response = response.response)
    }

    suspend fun updateUser(user: User.UserCreate, image: MultipartBody.Part? = null): Result<User.UserUpdateResponse> {
        return try {
            val userJson = Gson().toJson(user)
            val userBody = userJson.toRequestBody("application/json".toMediaTypeOrNull())

            val response = apiService.updateUser(userBody, image)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Ошибка изменения профиля!"))
            }
        } catch (e: Exception) {
            Log.e("APP_DEBUG", "${e.toString()}")
            Result.failure(e)
        }
    }

    suspend fun getWeightings(request: Weighting.WeightingRequest): List<Weighting.WeightingBase> {
        try{
            val response = apiService.getUserWeightings(startDate = request.startDate, endDate = request.endDate)
            return response
        }
        catch(e: Exception){
            Log.d("APP_DEBUG", "${e.toString()}")
            return emptyList()
        }
    }

    suspend fun getLastWeighting(): Weighting.WeightingBase? {
        try{
            val response = apiService.getUserLastWeighting()
            return response
        }
        catch(e: Exception){
            Log.d("APP_DEBUG", "${e.toString()}")
            return null
        }
    }

    suspend fun getWeightingsMonthChartData(): List<Weighting.MonthChartData>{
        try{
            val response = apiService.getWeightingsMonthChartData()
            return response
        }
        catch(e: Exception){
            Log.d("APP_DEBUG", "${e.toString()}")
            return emptyList()
        }
    }

    suspend fun getWeightingsYearChartData(): List<Weighting.YearChartData>{
        try{
            val response = apiService.getWeightingsYearChartData()
            return response
        }
        catch(e: Exception){
            Log.d("APP_DEBUG", "${e.toString()}")
            return emptyList()
        }
    }

    suspend fun addWeighting(weighting: Weighting.WeightingBase, image: MultipartBody.Part? = null): Result<Weighting.WeightingResponse> {
        return try{
            val weightingJson = Gson().toJson(weighting)
            val weightingBody = weightingJson.toRequestBody("application/json".toMediaTypeOrNull())

            val response = apiService.addWeighting(weightingBody, image)
            if(response.isSuccessful) {
                val body = response.body()
                if(body !== null){
                    Result.success(body)
                }
                else {
                    Result.failure(Exception("Не удалось добавить взвешивание"))
                }
            } else{
                Result.failure(Exception("Не удалось добавить взвешивание"))
            }
        } catch(e: Exception){
            Result.failure(e)
        }
    }

    suspend fun updateWeighting(weightingID: Int, weighting: Weighting.WeightingBase, image: MultipartBody.Part? = null): Result<User.UserUpdateResponse>{
        return try {
            val weightingJson = Gson().toJson(weighting)
            val weightingBody = weightingJson.toRequestBody("application/json".toMediaTypeOrNull())

            val response = apiService.updateWeighting(weighting_id = weightingID, weighting = weightingBody, image = image )
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Ошибка изменения взвешивания!"))
            }
        } catch (e: Exception) {
            Log.e("APP_DEBUG", "${e.toString()}")
            Result.failure(e)
        }
    }

    suspend fun getProducts(serarchQuery: String? = null): MutableList<Product.ProductBase> {
        try{
            val response = apiService.getProducts(serarchQuery)
            return response
        }
        catch(e: Exception){
            Log.d("APP_DEBUG", "${e.toString()}")
            return mutableListOf()
        }
    }

    suspend fun getProduct(productID: Int): Product.ProductBase? {
        try{
            val response = apiService.getProduct(productID)
            return response
        }
        catch(e: Exception){
            Log.d("APP_DEBUG", "${e.toString()}")
            return null
        }
    }

    suspend fun addProduct(product: Product.ProductBase, imagePart: MultipartBody.Part?): Result<Product.ProductResponse>{
        return try {
            val productJson = Gson().toJson(product)
            val productBody = productJson.toRequestBody("application/json".toMediaTypeOrNull())

            val response = apiService.addProduct(productBody, imagePart)
            if(response.isSuccessful){
                val body = response.body()
                if(body !== null){
                    Result.success(body)
                }
                else {
                    Result.failure(Exception("Не удалось добавить продукт"))
                }
            }
            else {
                Result.failure(Exception("Не удалось добавить продукт"))
            }
        }
        catch(e: Exception) {
            Result.failure(Exception("${e.toString()}"))
        }
    }

    suspend fun addMeal(meal: Meal.MealBase, image: MultipartBody.Part? = null): Result<Meal.MealResponse> {
        return try {
            val mealJson = Gson().toJson(meal)
            val mealBody = mealJson.toRequestBody("application/json".toMediaTypeOrNull())

            val response = apiService.addMeal(mealBody, image)
            if(response.isSuccessful){
                val body = response.body()
                if(body !== null){
                    Result.success(body)
                }
                else {
                    Result.failure(Exception("Не удалось добавить приём пищи"))
                }
            }
            else {
                Result.failure(Exception("Не удалось добавить приём пищи"))
            }
        }
        catch(e: Exception){
            Result.failure(Exception("${e.toString()}"))
        }
    }

    suspend fun updateMeal(mealID: Int, meal: Meal.MealBase, image: MultipartBody.Part?): Result<User.UserUpdateResponse>{
        return try {
            val mealJson = Gson().toJson(meal)
            val mealBody = mealJson.toRequestBody("application/json".toMediaTypeOrNull())

            val response = apiService.updateMeal(mealID,mealBody, image)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Ошибка изменения приёма пищи!"))
            }
        } catch (e: Exception) {
            Log.e("APP_DEBUG", "${e.toString()}")
            Result.failure(e)
        }
    }

    suspend fun addServing(mealID: Int, serving: Serving.ServingBase): Result<Serving.ServingResponse> {
        return try {
            val response = apiService.addServing(mealID, serving)
            if(response.isSuccessful){
                val body = response.body()
                if(body !== null){
                    Result.success(body)
                }
                else {
                    Result.failure(Exception("Не удалось добавить порцию"))
                }
            }
            else {
                Result.failure(Exception("Не удалось добавить порцию"))
            }
        }
        catch(e: Exception){
            Result.failure(Exception("${e.toString()}"))
        }
    }

    suspend fun getServings(mealID: Int): MutableList<Serving.ServingItem> {
        try{
            val response = apiService.getServings(mealID)
            return response
        }
        catch(e: Exception){
            Log.e("APP_DEBUG", "ERROR: ${e.toString()}")
            return mutableListOf()
        }
    }

    suspend fun deleteServing(serving: Serving.ServingItem){
        try{
            if(serving.id != null && serving.mealID != null){
                apiService.deleteServing(serving.mealID, serving.id)
            }
            else{
                Log.e("APP_DEBUG", "ERROR: serving wasn't deleted. MealID: ${serving.mealID} ServingID: ${serving.id}")
            }
        }
        catch(e: Exception){
            Log.e("APP_DEBUG", "ERROR: ${e.toString()}")
        }
    }

    suspend fun getMeals(startDate: String? = null, endDate: String? = null): List<Meal.MealBase>{
        try{
            val response = apiService.getMeals(startDate, endDate)
            return response
        }
        catch(e: Exception){
            Log.e("APP_DEBUG", "ERROR: ${e.toString()}")
            return emptyList()
        }
    }

    suspend fun getTodayMeals(): List<Meal.MealBase>{
        try{
            val response = apiService.getTodayMeals()
            return response
        }
        catch(e: Exception){
            Log.e("APP_DEBUG", "ERROR: ${e.toString()}")
            return emptyList()
        }
    }

    suspend fun getMeal(mealID: Int): Meal.MealBase?{
        try{
            val response = apiService.getMeal(mealID)
            return response
        }
        catch(e: Exception){
            Log.e("APP_DEBUG", "ERROR: ${e.toString()}")
            return null
        }
    }

    suspend fun deleteMeal(mealID: Int){
        try{
            apiService.deleteMeal(mealID)
        }
        catch(e: Exception){
            Log.e("APP_DEBUG", "ERROR: ${e.toString()}")
        }
    }

    suspend fun getMealTypeTotal(mealTypeID: Int): MealType.MealTypeBase? {
        try{
            val todayDate = LocalDate.now().toString()
            val response = apiService.getMealTypeTotal(mealTypeID,todayDate, todayDate)
            return response
        }
        catch(e: Exception){
            Log.e("APP_DEBUG", "ERROR: ${e.toString()}")
            return null
        }
    }
}