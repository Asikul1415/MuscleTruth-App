package com.example.muscletruth.data.repository
import android.content.Context
import android.net.ConnectivityManager
import com.example.muscletruth.data.api.ApiClient
import com.example.muscletruth.data.models.User
import android.util.Log
import androidx.room.Room
import com.example.muscletruth.utils.PreferencesManager
import com.example.muscletruth.data.localDB.AppDatabase
import com.example.muscletruth.data.serviceClasses.Token
import com.example.muscletruth.utils.Utils.NetworkUtils.checkForInternetConnection
import com.example.muscletruth.utils.Utils.NetworkUtils.connectivityManager
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

object UserRepository {
    private val apiService = ApiClient.apiService
    lateinit var localDb: AppDatabase

    fun init(context: Context){
        localDb = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "app_database"
        ).build()
        connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    suspend fun registerUser(user: User, image: MultipartBody.Part? = null): Result<Token> {
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

    suspend fun login(email: String, password: String): Result<Token> {
        return try {
            val response = apiService.authorizeUser(mapOf("email" to email, "password" to password))
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
        val response = apiService.checkEmail(email = email)

        return response
    }

    suspend fun getUser(context: Context): Result<User> {
        return try {
            if(checkForInternetConnection()){
                val response = apiService.getUser()
                if (response.isSuccessful) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Ошибка получения профиля!"))
                }
            }
            else{
                val user = localDb.userDao().getUser(PreferencesManager(context).getUserId())
                Result.success(User(
                    serverID = user.serverID!!,
                    name = user.name,
                    email = user.email,
                    password = user.password,
                    age = user.age,
                    serverPicture = user.serverPicture
                ))

            }
        } catch (e: Exception) {
            Log.e("APP_DEBUG", "${e.toString()}")
            Result.failure(e)
        }
    }

    suspend fun checkUserPassword(password: String): Boolean{
        val response = apiService.checkUserPassword(password = password)
        return response
    }

    suspend fun updateUser(user: User, image: MultipartBody.Part? = null): Boolean {
        try {
            val userJson = Gson().toJson(user)
            val userBody = userJson.toRequestBody("application/json".toMediaTypeOrNull())

            return apiService.updateUser(userBody, image)
        } catch (e: Exception) {
            Log.e("APP_DEBUG", "${e.toString()}")

            return false;
        }
    }
}