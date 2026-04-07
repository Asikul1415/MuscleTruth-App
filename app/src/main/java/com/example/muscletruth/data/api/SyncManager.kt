package com.example.muscletruth.data.api

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.room.util.copy
import com.example.muscletruth.data.api.models.*
import com.example.muscletruth.data.models.User
import com.example.muscletruth.data.models.Meal
import com.example.muscletruth.data.models.Product
import com.example.muscletruth.data.serviceClasses.ServingItem
import com.example.muscletruth.data.models.Serving
import com.example.muscletruth.data.models.Weighting
import com.example.muscletruth.data.repository.MealRepository
import com.example.muscletruth.data.repository.ProductRepository
import com.example.muscletruth.data.repository.ServingRepository
import com.example.muscletruth.data.repository.UserRepository
import com.example.muscletruth.data.repository.WeightingRepository
import com.example.muscletruth.utils.Utils.NetworkUtils.checkForInternetConnection
import com.example.muscletruth.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import okhttp3.MultipartBody
import java.io.File
import java.util.UUID

object SyncManager {
    private val localDb = UserRepository.localDb

    suspend fun syncDb(context: Context){
        if(checkForInternetConnection()){
            syncWeightings(context)
            syncTodayMeals()
            syncServings()
            syncProducts()
            syncUser(context)
            Log.d("APP_DEBUG", "DB SYNCED")
        }
        else{
            Log.d("APP_DEBUG", "DB SYNC IS NOT POSSIBLE. NETWORK IS DOWN.")
        }
    }

     private suspend fun syncWeightings(context: Context){
        var weightings: List<Weighting>
        coroutineScope {
            with(Dispatchers.IO){
                weightings = WeightingRepository.getWeightings().map { weighting ->
                    var localWeighting = UserRepository.localDb.weightingDao().getServerWeighting(weighting.serverID)
                    val localID = localWeighting?.localID ?: UUID.randomUUID().toString()
                    weighting.copy(localID = localID)
                }
            }
        }

        localDb.weightingDao().insertAll(weightings)
        Log.d("APP_DEBUG", "SYNC: WEIGHTINGS INSERTED ${weightings}")

        val localWeightings = localDb.weightingDao().getWeightingsForSync()
        coroutineScope {
             with(Dispatchers.IO){
                 localWeightings.forEach { weighting ->
                     var imagePart: MultipartBody.Part? = null;
                     if(weighting.localPicture !== "" && weighting.localPicture !== null){
                         imagePart = Utils.ImageUtils.createImagePart(context, Uri.fromFile(File(weighting.localPicture)));
                     }

                     WeightingRepository.addWeighting(weighting, context = context, image = imagePart)
                 }
             }
        }
        Log.d("APP_DEBUG", "SYNC: LOCAL WEIGHTINGS WERE SENT ${localWeightings}")
     }

    private suspend fun syncTodayMeals(){
        var meals: List<Meal>
        coroutineScope {
            with(Dispatchers.IO){
                meals = MealRepository.getTodayMeals().map {meal ->
                    meal.copy(localID = UUID.randomUUID().toString())
                }
            }
        }

        localDb.mealDao().insertAll(meals)
        Log.d("APP_DEBUG", "SYNC: MEALS INSERTED ${meals}")

        val localMeals = localDb.mealDao().getMealsForSync()
        coroutineScope {
            with(Dispatchers.IO){
                localMeals.forEach { meal ->
                    MealRepository.addMeal(Meal(
                        userID = meal.userID,
                        mealTypeID = meal.mealTypeID!!,
                        creationDate = meal.creationDate
                    ))
                }
            }
        }
        Log.d("APP_DEBUG", "SYNC: LOCAL MEALS WERE SENT ${localMeals}")
    }

    private suspend fun syncServings(){
        val servings: MutableList<ServingItem> = mutableListOf()
        coroutineScope {
            with(Dispatchers.IO){
                val meals = MealRepository.getTodayMeals()
                meals.forEach { meal ->
                    servings.addAll(ServingRepository.getServings(meal.serverID!!))
                }
            }
        }

        val convertedServings = servings.map { serving ->
            Serving(
                serverID = serving.id!!,
                mealID = serving.mealID,
                productID = serving.productID,
                productAmount = serving.productAmount
            )
        }

        localDb.servingDao().insertAll(convertedServings)
        Log.d("APP_DEBUG", "SYNC: SERVINGS INSERTED: ${convertedServings}")

        val localServings = localDb.servingDao().getServingsForSync()
        coroutineScope {
            with(Dispatchers.IO){
                localServings.forEach { serving ->
                    ServingRepository.addServing(serving.mealID!! ,serving)
                }
            }
        }
        Log.d("APP_DEBUG", "SYNC: LOCAL SERVINGS WERE SENT ${localServings}")
    }

    private suspend fun syncProducts(){
        var products: List<Product>
        coroutineScope {
            with(Dispatchers.IO){
                products = ProductRepository.getProducts().map{product ->
                    product.copy(localID = UUID.randomUUID().toString())
                }
            }
        }

        localDb.productDao().insertAll(products)
        Log.d("APP_DEBUG", "SYNC: PRODUCTS INSERTED ${products}")

        val localProducts = localDb.productDao().getProductsForSync()
        coroutineScope {
            with(Dispatchers.IO){
                localProducts.forEach { product ->
                    ProductRepository.addProduct(product, null)
                }
            }
        }
        Log.d("APP_DEBUG", "SYNC: LOCAL PRODUCTS WERE SENT ${localProducts}")
    }

    private suspend fun syncUser(context: Context){
        var user: User? = null
        coroutineScope {
            with(Dispatchers.IO){
                UserRepository.getUser(context).onSuccess {it -> {
                    user = it
                    user.localID = UUID.randomUUID().toString()
                }}
            }
        }
        Log.d("APP_DEBUG", "GET: USER ${user}")
        if(user === null) return

        localDb.userDao().insert(user)
        Log.d("APP_DEBUG", "SYNC: USER INSERTED")
    }
}