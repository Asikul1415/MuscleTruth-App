package com.example.muscletruth.data.repository

import android.util.Log
import com.example.muscletruth.data.api.ApiClient
import com.example.muscletruth.data.models.Product
import com.example.muscletruth.utils.Utils.NetworkUtils.checkForInternetConnection
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import com.example.muscletruth.data.repository.UserRepository.localDb
import java.util.UUID

object ProductRepository {
    private val apiService = ApiClient.apiService

    suspend fun getProducts(searchQuery: String? = null): MutableList<Product> {
        try{
            if(checkForInternetConnection()){
                return apiService.getProducts(searchQuery)
            }
            val products = localDb.productDao().getProducts(searchQuery)
            Log.d("APP_DEBUG", "LOCAL_GET: PRODUCTS ${products}")
            return products.toMutableList()
        }
        catch(e: Exception){
            Log.d("APP_DEBUG", "${e.toString()}")
            return mutableListOf()
        }
    }

    suspend fun getProduct(productID: Int): Product? {
        try{
            if(checkForInternetConnection()){
                return apiService.getProduct(productID)
            }
            return localDb.productDao().getProduct(productID)
        }
        catch(e: Exception){
            Log.d("APP_DEBUG", "${e.toString()}")
            return null
        }
    }

    suspend fun addProduct(product: Product, imagePart: MultipartBody.Part?): Result<Product>{
        var serverProduct: Product? = null
        return try {
            if(checkForInternetConnection()){
                val productJson = Gson().toJson(product)
                val productBody = productJson.toRequestBody("application/json".toMediaTypeOrNull())

                serverProduct = apiService.addProduct(productBody, imagePart).body()
            }
            if(serverProduct !== null){
                serverProduct.localID = UUID.randomUUID().toString()
                localDb.productDao().insert(serverProduct)
                Result.success(serverProduct)
            }
            else{
                Result.failure(Exception("Не удалось локально сохранить продукт"))
            }
        }
        catch(e: Exception) {
            Result.failure(Exception("${e.toString()}"))
        }
    }
}