package com.example.muscletruth.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.muscletruth.data.api.ApiClient
import com.example.muscletruth.data.models.FavouriteProduct
import com.example.muscletruth.data.models.Product
import com.example.muscletruth.data.models.ProductsHistory
import com.example.muscletruth.utils.Utils.NetworkUtils.checkForInternetConnection
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import com.example.muscletruth.data.repository.UserRepository.localDb
import com.example.muscletruth.utils.Utils
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
            Log.e("APP_DEBUG", "${e.toString()}")
            return mutableListOf()
        }
    }

    suspend fun getFavouriteProducts(): MutableList<FavouriteProduct> {
        try{
            if(checkForInternetConnection()){
                val products = apiService.getFavouriteProducts()
                Log.d("APP_DEBUG", "GET FAVOURITE PRODUCTS: $products")
                return products
            }

            val products = localDb.productDao().getFavouriteProducts()
            Log.d("APP_DEBUG", "GET FAVOURITE PRODUCTS: LOCAL $products")
            return products.toMutableList()
        }
        catch(e: Exception){
            Log.e("APP_DEBUG", "GET FAVOURITE PRODUCTS ERROR: ${e.toString()}")
            return mutableListOf()
        }
    }

    suspend fun getRecentProducts(): MutableList<ProductsHistory> {
        try{
            if(checkForInternetConnection()){
                val products = apiService.getRecentProducts()
                Log.d("APP_DEBUG", "GET RECENT PRODUCTS: $products")
                return products
            }

            val products = localDb.productDao().getRecentProducts()
            Log.d("APP_DEBUG", "GET RECENT PRODUCTS: LOCAL $products")
            return products.toMutableList()
        }
        catch(e: Exception){
            Log.e("APP_DEBUG", "GET RECENT PRODUCTS ERROR: ${e.toString()}")
            return mutableListOf()
        }
    }

    suspend fun getProduct(productID: Int, localProductID: String? = null): Product? {
        try{
            if(checkForInternetConnection()){
                return apiService.getProduct(productID)
            }

            if(localProductID !== null){
                return localDb.productDao().getLocalProduct(localProductID)
            }
            else{
                return localDb.productDao().getServerProduct(productID)
            }
        }
        catch(e: Exception){
            Log.d("APP_DEBUG", "${e.toString()}")
            return null
        }
    }

    suspend fun addProduct(product: Product, imagePart: MultipartBody.Part?, localImage: Uri?, context: Context): Result<Product>{
        return try {
            if(checkForInternetConnection()){
                val productJson = Gson().toJson(product)
                val productBody = productJson.toRequestBody("application/json".toMediaTypeOrNull())

                var serverProduct = apiService.addProduct(productBody, imagePart).body()

                if(serverProduct !== null){
                    if(product.localID !== null){
                        serverProduct.localID = product.localID
                    }
                    else{
                        serverProduct.localID = UUID.randomUUID().toString()
                    }

                    if(serverProduct.serverPicture !== null){
                        if(product.localPicture !== null){
                            serverProduct.localPicture = product.localPicture
                        }
                        else{
                            val url = serverProduct.serverPicture!!
                            serverProduct.localPicture = Utils.ImageUtils.saveImageFromServer(context,Utils.ImageUtils.getImagePath(url))
                        }
                    }

                    localDb.productDao().insert(serverProduct)
                    Result.success(serverProduct)
                }
                else{
                    Log.e("APP_DEBUG", "ADD PRODUCT: FAILURE")
                    Result.failure(Exception("ADD PRODUCT FAILURE"))
                }
            }
            else{
                if(localImage !== null){
                    product.localPicture = Utils.ImageUtils.copyImageToLocalStorage(context, localImage)
                }

                localDb.productDao().insert(product)
                Result.success(product)
            }
        }
        catch(e: Exception) {
            Log.e("APP_DEBUG", "PRODUCT ADD ERROR!!!")
            Result.failure(Exception("${e.toString()}"))
        }
    }
}