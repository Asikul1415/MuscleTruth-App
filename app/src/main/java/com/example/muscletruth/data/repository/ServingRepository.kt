package com.example.muscletruth.data.repository

import android.util.Log
import com.example.muscletruth.data.api.ApiClient
import com.example.muscletruth.data.models.Meal
import com.example.muscletruth.data.models.Serving
import com.example.muscletruth.utils.Utils.NetworkUtils.checkForInternetConnection
import com.example.muscletruth.data.repository.UserRepository.localDb
import java.util.UUID

object ServingRepository {
    private val apiService = ApiClient.apiService

    suspend fun addServing(meal: Meal, serving: Serving): Result<Serving> {
        var serverServing: Serving? = null
        return try {
            //SERVER
            //ONLY IF SERVING DOESN'T EXIST ON THE SERVER ALREADY
            if(checkForInternetConnection() && serving.serverID === -1){
                //In case serving product was added offline
                if(serving.productID === -1 && serving.localProductID !== null){
                    val productLocalID = serving.localProductID!!
                    val product = localDb.productDao().getLocalProduct(productLocalID)
                    if(product !== null){
                        serving.productID = product.serverID
                    }
                }

                serverServing = apiService.addServing(meal.serverID, serving).body()
                Log.d("APP_DEBUG", "ADD SERVING: ADDED SERVING $serverServing TO THE SERVER")
            }



            //LOCAL
            if(serverServing !== null){
                if(serving.localID !== null){
                    serverServing.localID = serving.localID
                }
                else{
                    serverServing.localID = UUID.randomUUID().toString()
                }
                serverServing.localMealID = meal.localID
                serverServing.localProductID = serving.localProductID
                localDb.servingDao().insert(serverServing)

                Log.d("APP_DEBUG", "ADD SERVING: ADDED SERVING $serverServing")
                Result.success(serverServing)
            }
            else{
                serving.localMealID = meal.localID
                localDb.servingDao().insert(serving)

                Log.d("APP_DEBUG", "ADD SERVING: ADDED SERVING $serving")
                Result.success(serving)
            }
        }
        catch(e: Exception){
            Log.e("APP_DEBUG", "ADD SERVING ERROR: ${e.toString()}")
            Result.failure(Exception("${e.toString()}"))
        }
    }

    suspend fun getServings(mealServerID: Int, mealLocalID: String? = null): MutableList<Serving> {
        try{
            if(checkForInternetConnection()){
                return apiService.getServings(mealServerID).map{ serving -> serving.copy(
                    localID=UUID.randomUUID().toString(),
                    localMealID = localDb.mealDao().getServerMeal(serving.mealID!!)!!.localID,
                    localProductID = localDb.productDao().getServerProduct(serving.productID)!!.localID,
                )}.toMutableList()
            }

            var servings: List<Serving>
            if(mealServerID != -1){
                servings = localDb.servingDao().getServerMealServings(mealServerID)
            }
            else{
                servings = localDb.servingDao().getLocalMealServings(mealLocalID)
            }

            Log.d("APP_DEBUG", "SERVINGS - $servings")
            return servings.toMutableList()
        }
        catch(e: Exception){
            Log.e("APP_DEBUG", "getServings() ERROR: ${e.toString()}")
            return mutableListOf()
        }
    }

    suspend fun updateServing(serving: Serving) {
        try{
            if(checkForInternetConnection()){
                val mealID = serving.mealID
                if(mealID !== null && serving.serverID != -1){
                    apiService.updateServing(mealID, serving.serverID, serving)
                    Log.d("APP_DEBUG", "SERVING UPDATE: $serving WAS UPDATED ON THE SERVER")
                }
            }

            if(serving.serverID != -1){
                val localServing = localDb.servingDao().getServerServing(serving.serverID)
                localServing.productAmount = serving.productAmount
                localDb.servingDao().update(localServing)
            }
            else{
                localDb.servingDao().update(serving)
            }
        } catch(e: Exception) {
            Log.d("APP_DEBUG", "SERVING UPDATE ERROR: ${e.toString()}")

        }
    }

    suspend fun deleteServing(serving: Serving){
        try{
            if(checkForInternetConnection()){
                val mealID = serving.mealID
                if(mealID !== null){
                    apiService.deleteServing(mealID, serving.serverID)
                }
            }

            val localServing = localDb.servingDao().getLocalServing(serving.localID)
            localDb.servingDao().delete(localServing)
            Log.d("APP_DEBUG", "DELETE SERVING: SERVING $localServing WAS DELETED")
        }
        catch(e: Exception){
            Log.e("APP_DEBUG", "DELETE SERVING ERROR: ${e.toString()}")
        }
    }
}