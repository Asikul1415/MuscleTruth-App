package com.example.muscletruth.data.api

import com.example.muscletruth.data.api.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @Multipart
    @POST("api/auth/register")
    suspend fun createUser(@Part("user") user: RequestBody, @Part image: MultipartBody.Part? = null): Response<User.LoginResponse>

    @POST("api/auth/login")
    suspend fun authorizeUser(@Body user: User.UserLogin): Response<User.LoginResponse>

    @GET("api/users/me")
    suspend fun getUser(): Response<User.UserResponse>

    @POST("api/users/check-email")
    suspend fun checkEmail(@Body email: User.Email): User.CheckPasswordResponse

    @Multipart
    @PUT("api/users/me")
    suspend fun updateUser(@Part("user") user: RequestBody, @Part image: MultipartBody.Part? = null): Response<User.UserUpdateResponse>

    @POST("api/users/me/check-password")
    suspend fun checkUserPassword(@Body password: User.Password): User.CheckPasswordResponse

    @GET("api/weightings")
    suspend fun getUserWeightings(@Query("start_date") startDate: String?, @Query("end_date") endDate: String?): List<Weighting.WeightingBase>

    @GET("api/weightings/last")
    suspend fun getUserLastWeighting(): Weighting.WeightingBase

    @Multipart
    @POST("api/weightings")
    suspend fun addWeighting(@Part("weighting") weighting: RequestBody, @Part image: MultipartBody.Part? = null): Response<Weighting.WeightingResponse>

    @Multipart
    @PUT("api/weightings/{weighting_id}")
    suspend fun updateWeighting(
        @Path("weighting_id") weighting_id: Int,
        @Part("weighting") weighting: RequestBody,
        @Part image: MultipartBody.Part? = null): Response<User.UserUpdateResponse>

    @GET("api/weightings/chart/year")
    suspend fun getWeightingsYearChartData(): List<Weighting.YearChartData>

    @GET("api/weightings/chart/month")
    suspend fun getWeightingsMonthChartData(): List<Weighting.MonthChartData>

    @GET("api/products")
    suspend fun getProducts(@Query("search_query") searchQuery: String?): MutableList<Product.ProductBase>

    @GET("api/products/{product_id}")
    suspend fun getProduct(@Path("product_id") product_id: Int): Product.ProductBase

    @Multipart
    @POST("api/products")
    suspend fun addProduct(@Part("product") product: RequestBody, @Part image: MultipartBody.Part? = null): Response<Product.ProductResponse>

    @Multipart
    @POST("api/meals")
    suspend fun addMeal(@Part("meal") meal: RequestBody, @Part image: MultipartBody.Part? = null): Response<Meal.MealResponse>

    @Multipart
    @PUT("api/meals/{meal_id}")
    suspend fun updateMeal(@Path("meal_id") mealID: Int, @Part("meal") meal: RequestBody, @Part image: MultipartBody.Part? = null): Response<User.UserUpdateResponse>

    @POST("/api/meals/{meal_id}/servings")
    suspend fun addServing(@Path("meal_id") mealID: Int, @Body serving: Serving.ServingBase): Response<Serving.ServingResponse>

    @GET("/api/meals/{meal_id}/servings")
    suspend fun getServings(@Path("meal_id") mealID: Int): MutableList<Serving.ServingItem>

    @DELETE("/api/meals/{meal_id}/servings/{serving_id}")
    suspend fun deleteServing(@Path("meal_id") mealID: Int, @Path("serving_id") servingID: Int)

    @GET("/api/meals/{meal_id}")
    suspend fun getMeal(@Path("meal_id") mealID: Int): Meal.MealBase

    @GET("/api/meals")
    suspend fun getMeals(@Query("start_date") startDate: String?, @Query("end_date") endDate: String?): List<Meal.MealBase>

    @GET("/api/meals/today")
    suspend fun getTodayMeals(): List<Meal.MealBase>

    @DELETE("/api/meals/{meal_id}")
    suspend fun deleteMeal(@Path("meal_id") mealID: Int)

    @GET("/api/meals/{meal_type_id}/total")
    suspend fun getMealTypeTotal(@Path("meal_type_id") mealTypeID: Int, @Query("start_date") startDate: String, @Query("end_date") endDate: String): MealType.MealTypeBase

}