package com.example.muscletruth.data.repository

import android.util.Log
import com.example.muscletruth.data.api.ApiClient
import com.example.muscletruth.data.models.Meal
import com.example.muscletruth.data.models.Serving
import com.example.muscletruth.data.serviceClasses.ServingItem
import com.example.muscletruth.utils.Utils.NetworkUtils.checkForInternetConnection
import com.example.muscletruth.data.repository.UserRepository.localDb
import java.util.UUID

object ServingRepository {
    private val apiService = ApiClient.apiService

    suspend fun addServing(meal: Meal, serving: Serving): Result<Serving> {
        var serverServing: Serving? = null
        return try {
            //ONLY IF SERVING DOESN'T EXIST ON THE SERVER ALREADY
            if(checkForInternetConnection() && serving.serverID === -1){
                serverServing = apiService.addServing(meal.serverID, serving).body()
            }
            Log.d("APP_DEBUG", "ADD SERVING $meal $serving")
//            if(meal.localID === null){
                if(serverServing !== null){
                    serverServing.localID = UUID.randomUUID().toString()
                    Log.d("APP_DEBUG", "ADDED SERVING $serverServing")
                    serverServing.localMealID = meal.localID
                    localDb.servingDao().insert(serverServing)
                    Result.success(serverServing)
                }
                else{
                    serving.localMealID = meal.localID
                    localDb.servingDao().insert(serving)
                    Log.d("APP_DEBUG", "ADDED SERVING $serving")
                    Result.success(serving)
//                Result.failure(Exception("Не удалось локально добавить порцию!"))
                }
//            } else {
//                Log.e("APP_DEBUG", "ADD SERVING ERROR!")
//                Result.failure(Exception("Не удалось локально добавить порцию!"))
//            }
        }
        catch(e: Exception){
            Log.e("APP_DEBUG", "ADD SERVING ERROR ${e.toString()}")
            Result.failure(Exception("${e.toString()}"))
        }
    }

    suspend fun getServings(serverID: Int, localID: String? = null): MutableList<ServingItem> {
        try{
            if(checkForInternetConnection()){
                return apiService.getServings(serverID).map {serving ->
                    ServingItem(
                        id = serving.serverID,
                        productID = serving.productID,
                        mealID = serving.mealID,
                        productAmount = serving.productAmount,
                    )
                }.toMutableList()
            }

            var servings: List<Serving>
            if(serverID !== -1){
                servings = localDb.servingDao().getServerMealServings(serverID)
            }
            else{
                servings = localDb.servingDao().getLocalMealServings(localID)
            }

            Log.d("APP_DEBUG", "SERVINGS - $servings")
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