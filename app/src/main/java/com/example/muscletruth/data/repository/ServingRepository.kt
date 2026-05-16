package com.example.muscletruth.data.repository

import android.util.Log
import com.example.muscletruth.data.api.ApiClient
import com.example.muscletruth.data.models.Meal
import com.example.muscletruth.data.models.RecentServing
import com.example.muscletruth.data.models.Serving
import com.example.muscletruth.utils.Utils.NetworkUtils.checkForInternetConnection
import com.example.muscletruth.data.repository.UserRepository.localDb
import java.time.ZonedDateTime
import java.util.UUID

object ServingRepository {
    private val apiService = ApiClient.apiService

    suspend fun addServing(meal: Meal, serving: Serving): Result<Serving> {
        var serverServing: Serving? = null
        return try {
            //SERVER
            //ONLY IF SERVING DOESN'T EXIST ON THE SERVER ALREADY
            if(checkForInternetConnection() && serving.serverID == -1){
                //In case serving product was added offline
                if(serving.productID == -1 && serving.localProductID !== null){
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

    suspend fun getServings(): MutableList<Serving> {
        try{
            if(checkForInternetConnection()){
                val servings = apiService.getServings()
                return servings.map{ serving -> serving.copy(
                    localID=UUID.randomUUID().toString(),
                    localMealID = localDb.mealDao().getServerMeal(serving.mealID!!)!!.localID,
                    localProductID = localDb.productDao().getServerProduct(serving.productID)!!.localID,
                )}.toMutableList()
            }

            val servings = localDb.servingDao().getServings()

            Log.d("APP_DEBUG", "SERVINGS - $servings")
            return servings.toMutableList()
        }
        catch(e: Exception){
            Log.e("APP_DEBUG", "getMealServings() ERROR: ${e.toString()}")
            throw e
            return mutableListOf()
        }
    }

    suspend fun getMealServings(mealServerID: Int, mealLocalID: String? = null): MutableList<Serving> {
        try{
            if(checkForInternetConnection()){
                val servings = apiService.getMealServings(mealServerID)
                return servings.map{ serving -> serving.copy(
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
            Log.e("APP_DEBUG", "getMealServings() ERROR: ${e.toString()}")
            throw e
            return mutableListOf()
        }
    }

    suspend fun getServing(servingServerID: Int, servingLocalID: String? = null): Serving? {
        try{
            if(checkForInternetConnection() && servingServerID != -1){
                val serverServing = apiService.getServing(servingServerID)
                return serverServing.copy(
                    localID=UUID.randomUUID().toString(),
                    localMealID = localDb.mealDao().getServerMeal(serverServing.mealID!!)!!.localID,
                    localProductID = localDb.productDao().getServerProduct(serverServing.productID)!!.localID,
                )
            }

            var serving: Serving? = null
            if(servingServerID != -1){
                serving = localDb.servingDao().getServerServing(servingServerID)
            }
            else{
                if(servingLocalID !== null){
                    serving = localDb.servingDao().getLocalServing(servingLocalID)
                }
            }

            Log.d("APP_DEBUG", "SERVING - $serving")
            return serving
        }
        catch(e: Exception){
            Log.e("APP_DEBUG", "getServing() ERROR: ${e.toString()}")

            return null
        }
    }

    suspend fun updateServing(serving: Serving) {
        try{
            if(checkForInternetConnection()){
                val mealID = serving.mealID
                if(mealID !== null && serving.serverID != -1){
                    apiService.updateServing(serving.serverID, serving)
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
                if(serving.serverID != -1){
                    apiService.deleteServing(serving.serverID)
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

    suspend fun getRecentServings(): MutableList<RecentServing> {
        try{
            if(checkForInternetConnection()){
                val recentServings = apiService.getRecentServings()
                Log.d("APP_DEBUG", "GET RECENT SERVINGS: $recentServings")
                return recentServings
            }

            val localRecentServings = localDb.servingDao().getRecentServings()
            Log.d("APP_DEBUG", "GET RECENT SERVINGS: LOCAL $localRecentServings")
            return localRecentServings.toMutableList()
        }
        catch(e: Exception){
            Log.e("APP_DEBUG", "GET RECENT SERVINGS ERROR: ${e.toString()}")
            return mutableListOf()
        }
    }

    suspend fun addRecentServing(servingServerID: Int, servingLocalID: String? = null) {
        try{
            var recentServing: RecentServing? = null
            if(checkForInternetConnection() && servingServerID != -1){
                recentServing = apiService.addRecentServing(servingServerID)
                Log.d("APP_DEBUG", "ADDED RECENT SERVING ON SERVER: $recentServing")
            }

            if(recentServing !== null){
                val localRecentServing = localDb.servingDao().getServerServing(recentServing.servingServerID)
                if(localRecentServing !== null){
                    recentServing.servingLocalID = localRecentServing.localID
                }

            }
            else{
                var localServingID: String? = null
                if(servingServerID != -1){
                    localServingID = localDb.servingDao().getServerServing(servingServerID)!!.localID
                }
                else{
                    localServingID = servingLocalID
                }

                recentServing = RecentServing(
                    servingLocalID = localServingID!!,
                    servingServerID = servingServerID,
                    useDate = ZonedDateTime.now().toOffsetDateTime().toString()
                )
            }
            localDb.productDao().addRecentProduct(recentServing)
            Log.d("APP_DEBUG", "ADDED RECENT SERVING LOCAL: $recentServing")
        }
        catch(e: Exception){
            Log.e("APP_DEBUG", "ADDED RECENT SERVING ERROR: ${e.toString()}")
        }
    }

    suspend fun deleteRecentServing(servingServerID: Int, servingLocalID: String? = null){
        try{
            if(checkForInternetConnection()){
                val isDeleteSuccessful = apiService.deleteRecentServing(servingServerID)
                Log.d("APP_DEBUG", "DELETE RECENT SERVING ON SERVER: $isDeleteSuccessful")
            }


            var localID: String? = null
            if(servingServerID != -1){
                localID = localDb.productDao().getServerProduct(servingServerID)!!.localID
            }
            else{
                localID = servingLocalID
            }

            val recentProduct = RecentServing(
                servingLocalID = localID!!,
                servingServerID = servingServerID)
            localDb.productDao().deleteRecentProduct(recentProduct)
            Log.d("APP_DEBUG", "DELETE RECENT SERVING LOCAL: $recentProduct")
        }
        catch(e: Exception){
            Log.e("APP_DEBUG", "DELETE RECENT SERVING ERROR: ${e.toString()}")
        }
    }
}