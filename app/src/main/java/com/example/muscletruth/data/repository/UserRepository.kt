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

    suspend fun getWeightings(): List<Weighting.WeightingBase> {
        try{
            val response = apiService.getUserWeightings()
            return response
        }
        catch(e: Exception){
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
}