package com.example.muscletruth.data.api

import android.content.Context
import android.net.Uri
import android.util.Log
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
            syncTodayMeals(context)
            syncProducts(context)
            syncServings()
            syncUser(context)
            Log.d("APP_DEBUG", "DB SYNCED")
        }
        else{
            Log.d("APP_DEBUG", "DB SYNC IS NOT POSSIBLE. NETWORK IS DOWN.")
        }
    }

     private suspend fun syncWeightings(context: Context){
         //Insert in localDB new weightings from the server
        var weightingsToInsert: List<Weighting>
        coroutineScope {
            with(Dispatchers.IO){
                val localWeightings = localDb.weightingDao().getWeightings()

                //Find all weightings from server that missing in localDB
                weightingsToInsert = WeightingRepository.getWeightings().map { weighting ->
                    var localWeighting = UserRepository.localDb.weightingDao().getServerWeighting(weighting.serverID)

                    var localPicture: String? = null
                    if(checkForInternetConnection() && weighting.serverPicture?.isNullOrEmpty() === false){
                        localPicture = Utils.ImageUtils.saveImageFromServer(context, Utils.ImageUtils.getImagePath(weighting.serverPicture!!))
                    }

                    weighting.copy(
                        localID = localWeighting?.localID ?: UUID.randomUUID().toString(),
                        localPicture = localPicture
                    )
                }.filter { weighting ->
                    localWeightings.find{ local -> local.serverID === weighting.serverID} === null
                }
            }
        }
        localDb.weightingDao().insertAll(weightingsToInsert)
        Log.d("APP_DEBUG", "SYNC: WEIGHTINGS INSERTED ${weightingsToInsert}")


        //Send to the server new weightings that were added in offline mode
        val weightingsForSync = localDb.weightingDao().getWeightingsForSync()
        coroutineScope {
             with(Dispatchers.IO){
                 weightingsForSync.forEach { weighting ->
                     var imagePart: MultipartBody.Part? = null;
                     if(weighting.localPicture !== "" && weighting.localPicture !== null){
                         imagePart = Utils.ImageUtils.createImagePart(context, Uri.fromFile(File(weighting.localPicture)));
                     }

                     WeightingRepository.addWeighting(weighting, context = context, image = imagePart)
                 }
             }
        }
        Log.d("APP_DEBUG", "SYNC: LOCAL WEIGHTINGS WERE SENT ${weightingsForSync}")
     }

    private suspend fun syncTodayMeals(context: Context){
        var meals: List<Meal>

        coroutineScope {
            with(Dispatchers.IO){
                val localMeals = localDb.mealDao().getMeals()

                meals = MealRepository.getMeals().map {meal ->
                    if(meal.serverPicture !== null){
                        meal.copy(
                            localID = UUID.randomUUID().toString(),
                            localPicture = Utils.ImageUtils.saveImageFromServer(context, Utils.ImageUtils.getImagePath(meal.serverPicture!!)))
                    }
                    else{
                        meal.copy(localID = UUID.randomUUID().toString())
                    }

                }.filter { meal ->
                    localMeals.find{ local -> local.serverID === meal.serverID} === null
                }
            }
        }

        localDb.mealDao().insertAll(meals)
        Log.d("APP_DEBUG", "SYNC MEALS: INSERTED $meals")

        val mealsForSync = localDb.mealDao().getMealsForSync()
        coroutineScope {
            with(Dispatchers.IO){
                mealsForSync.forEach { meal ->
                    var imagePart: MultipartBody.Part? = null
                    if(meal.localPicture !== null){
                        val uri = Uri.fromFile(File(meal.localPicture))
                        imagePart = Utils.ImageUtils.createImagePart(context, uri)
                    }


                    MealRepository.addMeal(meal, image = imagePart, context = context).onSuccess { serverMeal ->
                        updateMealServings(serverMeal)
                    }
                }
            }
        }
        Log.d("APP_DEBUG", "SYNC MEALS: LOCALS WERE SENT $mealsForSync")

        val mealsForUpdate = localDb.mealDao().getMealsForUpdate()
        coroutineScope {
            with(Dispatchers.IO){
                mealsForUpdate.forEach { meal ->
                    var imagePart: MultipartBody.Part? = null
                    if(meal.localPicture !== null){
                        val uri = Uri.fromFile(File(meal.localPicture))
                         imagePart = Utils.ImageUtils.createImagePart(context, uri)
                    }


                    MealRepository.updateMeal(meal,  image = imagePart)
                    val updatedMeal = MealRepository.getMeal(meal.serverID)
                    if(updatedMeal !== null){
                        updateMealServings(updatedMeal.copy(localID = meal.localID))
                    }

                    //setting flag to zero
                    localDb.mealDao().update(meal.copy(wasUpdated = 0))
                }
            }
        }
        Log.d("APP_DEBUG", "SYNC MEALS: LOCALS WERE UPDATED $mealsForUpdate")
    }

    private suspend fun updateMealServings(meal: Meal){
        localDb.servingDao().getLocalMealServings(meal.localID).map{serving ->
            val updatedServing = serving.copy(mealID = meal.serverID)
            localDb.servingDao().update(updatedServing)
        }
    }

    private suspend fun syncServings(){
        val servings: MutableList<ServingItem> = mutableListOf()

        coroutineScope {
            with(Dispatchers.IO){
                val meals = MealRepository.getMeals()
                meals.forEach { meal ->
                    servings.addAll(ServingRepository.getServings(meal.serverID!!))
                }
            }
        }

        val localServings = localDb.servingDao().getServings()
        val convertedServings = servings
            .filter{serving ->
                localServings.find {local -> local.serverID === serving.id} === null}
            .map {serving ->
                Serving(
                    serverID = serving.id!!,
                    mealID = serving.mealID,
                    localMealID = localDb.mealDao().getServerMeal(serving.mealID!!)?.localID,
                    productID = serving.productID,
                    localProductID = localDb.productDao().getServerProduct(serving.productID)?.localID,
                    productAmount = serving.productAmount
                )
            }

        localDb.servingDao().insertAll(convertedServings)
        Log.d("APP_DEBUG", "SYNC SERVINGS: INSERTED: $convertedServings")


        val servingsForSync = localDb.servingDao().getServingsForSync()
        coroutineScope {
            with(Dispatchers.IO){
                servingsForSync.forEach { serving ->
                    val meal = MealRepository.getMeal(serving.mealID!!, serving.localID)
                    if(meal !== null){
                         ServingRepository.addServing(meal ,serving).onSuccess { serverServing ->
                             val updatedServing = serving.copy(
                                 serverID = serverServing.serverID,
                                 productID = serverServing.productID,
                                 userID = serverServing.userID
                             )
                             Log.d("APP_DEBUG", "upd. SERVING - $updatedServing")
                             localDb.servingDao().update(updatedServing)
                         }
                    }
                }
            }
        }
        Log.d("APP_DEBUG", "SYNC SERVINGS: LOCALS WERE SENT $servingsForSync")
    }

    private suspend fun syncProducts(context: Context){
        var productsToInsert: List<Product>

        coroutineScope {
            with(Dispatchers.IO){
                val localProducts = localDb.productDao().getProducts()
                productsToInsert = ProductRepository.getProducts().map{ product ->
                    if(product.serverPicture !== null){
                        product.copy(
                            localID = UUID.randomUUID().toString(),
                            localPicture = Utils.ImageUtils.saveImageFromServer(context, Utils.ImageUtils.getImagePath(product.serverPicture!!)))
                    }
                    else{
                        product.copy(localID = UUID.randomUUID().toString())
                    }
                }.filter { product ->
                    localProducts.find{local -> local.serverID === product.serverID} === null
                }
            }
        }

        localDb.productDao().insertAll(productsToInsert)
        Log.d("APP_DEBUG", "SYNC PRODUCTS: INSERTED $productsToInsert")

        val productsForSync = localDb.productDao().getProductsForSync()
        coroutineScope {
            with(Dispatchers.IO){
                productsForSync.forEach { product ->
                    val uri = Uri.fromFile(File(product.localPicture))
                    ProductRepository.addProduct(
                        product,
                        imagePart = Utils.ImageUtils.createImagePart(context, uri),
                        localImage = null,
                        context = context)
                }
            }
        }
        Log.d("APP_DEBUG", "SYNC PRODUCTS: LOCALS WERE SENT $productsForSync")
    }

    private suspend fun syncUser(context: Context){
        var user: User? = null
        coroutineScope {
            with(Dispatchers.IO){
                user = UserRepository.getUser(context)
                if(user?.localID === null){
                    user?.localID = UUID.randomUUID().toString()
                }
            }
        }
        Log.d("APP_DEBUG", "GET USER: GOT USER $user")
        if(user === null) return

        localDb.userDao().insert(user)
        Log.d("APP_DEBUG", "SYNC USER: INSERTED")
    }
}