package com.example.muscletruth.data.repository
import com.example.muscletruth.data.api.ApiClient
import com.example.muscletruth.data.api.models.*
import android.util.Log
import okhttp3.Response

class UserRepository {
    private val apiService = ApiClient.apiService

    suspend fun registerUser(user: User.UserCreate): Result<User.UserResponse> {
        return try {
            val response = apiService.createUser(user)
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
                    // Сохраняем токен
                    ApiClient.setAuthToken(authResponse.accessToken)
                    Result.success(authResponse)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Login failed: ${response.code()}"))
            }
        } catch (e: Exception) {
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

    suspend fun addWeighting(weighting: Weighting.WeightingBase): Result<Weighting.WeightingResponse> {
        return try{
            val response = apiService.addWeighting(weighting)
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

    suspend fun getProducts(serarchQuery: String? = null): List<Product.ProductBase> {
        try{
            val response = apiService.getProducts(serarchQuery)
            return response
        }
        catch(e: Exception){
            Log.d("APP_DEBUG", "${e.toString()}")
            return emptyList()
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

    suspend fun addProduct(product: Product.ProductBase): Result<Product.ProductResponse>{
        return try {
            val response = apiService.addProduct(product)
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

    suspend fun addMeal(meal: Meal.MealBase): Result<Meal.MealResponse> {
        return try {
            val response = apiService.addMeal(meal)
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

    suspend fun getServings(mealID: Int): List<Serving.ServingItem> {
        try{
            val response = apiService.getServings(mealID)
            return response
        }
        catch(e: Exception){
            Log.d("APP_DEBUG", "ERROR: ${e.toString()}")
            return emptyList()
        }
    }

    suspend fun getMeals(startDate: String? = null, endDate: String? = null, mealTypeID: Int? = null): List<Meal.MealBase>{
        try{
            val response = apiService.getMeals(startDate, endDate, mealTypeID)
            return response
        }
        catch(e: Exception){
            Log.d("APP_DEBUG", "ERROR: ${e.toString()}")
            return emptyList()
        }
    }
}