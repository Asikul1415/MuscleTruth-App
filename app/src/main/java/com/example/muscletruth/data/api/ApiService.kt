package com.example.muscletruth.data.api

import com.example.muscletruth.data.api.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("api/auth/register")
    suspend fun createUser(@Body user: User.UserCreate): Response<User.UserResponse>

    @POST("api/auth/login")
    suspend fun authorizeUser(@Body user: User.UserLogin): Response<User.LoginResponse>

    @GET("api/weightings")
    suspend fun getUserWeightings(@Query("start_date") startDate: String?, @Query("end_date") endDate: String?): List<Weighting.WeightingBase>

    @POST("api/weightings")
    suspend fun addWeighting(@Body weighting: Weighting.WeightingBase): Response<Weighting.WeightingResponse>

    @GET("api/products")
    suspend fun getProducts(@Query("search_query") searchQuery: String?): List<Product.ProductBase>

    @GET("api/products/{product_id}")
    suspend fun getProduct(@Path("product_id") product_id: Int): Product.ProductBase

    @POST("api/products")
    suspend fun addProduct(@Body product: Product.ProductBase): Response<Product.ProductResponse>

    @POST("api/meals")
    suspend fun addMeal(@Body meal: Meal.MealBase): Response<Meal.MealResponse>

    @POST("/api/meals/{meal_id}/servings")
    suspend fun addServing(@Path("meal_id") mealID: Int, @Body serving: Serving.ServingBase): Response<Serving.ServingResponse>

    @GET("/api/meals/{meal_id}/servings")
    suspend fun getServings(@Path("meal_id") mealID: Int): List<Serving.ServingItem>

    @GET("/api/meals")
    suspend fun getMeals(@Query("start_date") startDate: String?, @Query("end_date") endDate: String?): List<Meal.MealBase>

    @GET("/api/meals/{meal_type_id}/total")
    suspend fun getMealTypeTotal(@Path("meal_type_id") mealTypeID: Int, @Query("start_date") startDate: String, @Query("end_date") endDate: String): MealType.MealTypeBase

}