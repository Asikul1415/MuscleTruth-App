package com.example.muscletruth.data.repository

import android.util.Log
import com.example.muscletruth.data.api.ApiClient
import com.example.muscletruth.data.models.Serving
import com.example.muscletruth.data.serviceClasses.ServingItem
import com.example.muscletruth.utils.Utils.NetworkUtils.checkForInternetConnection
import com.example.muscletruth.data.repository.UserRepository.localDb
import java.util.UUID

object ServingRepository {
    private val apiService = ApiClient.apiService

    suspend fun addServing(mealID: Int, serving: Serving): Result<Serving> {
        var serverServing: Serving? = null
        return try {
            if(checkForInternetConnection()){
                serverServing = apiService.addServing(mealID, serving).body()
            }

            if(serverServing !== null){
                serverServing.localID = UUID.randomUUID().toString()
                localDb.servingDao().insert(serverServing)
                Result.success(serverServing)
            }
            else{
                Result.failure(Exception("Не удалось локально добавить порцию!"))
            }
        }
        catch(e: Exception){
            Result.failure(Exception("${e.toString()}"))
        }
    }

    suspend fun getServings(mealID: Int): MutableList<ServingItem> {
        try{
            if(checkForInternetConnection()){
                return apiService.getServings(mealID)
            }
            val servings = localDb.servingDao().getServings(mealID)
            return servings.map {serving ->
                ServingItem(
                    id = serving.serverID,
                    productID = serving.productID,
                    mealID = serving.mealID,
                    productAmount = serving.productAmount,
                )
            }.toMutableList()
        }
        catch(e: Exception){
            Log.e("APP_DEBUG", "ERROR: ${e.toString()}")
            return mutableListOf()
        }
    }

    suspend fun deleteServing(serving: ServingItem){
        try{
            if(serving.id != null && serving.mealID != null){
                if(checkForInternetConnection()){
                    apiService.deleteServing(serving.mealID, serving.id)
                }

                val localServing = localDb.servingDao().getServing(serving.id)
                localDb.servingDao().delete(localServing)
                Log.d("APP_DEBUG", "DELETE: SERVING ${localServing} WAS DELETED")
            }
            else{
                Log.e("APP_DEBUG", "ERROR: serving wasn't deleted. MealID: ${serving.mealID} ServingID: ${serving.id}")
            }
        }
        catch(e: Exception){
            Log.e("APP_DEBUG", "ERROR: ${e.toString()}")
        }
    }
}