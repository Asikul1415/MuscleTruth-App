package com.example.muscletruth.data.api

import com.example.muscletruth.data.models.Meal
import com.example.muscletruth.data.models.Product
import com.example.muscletruth.data.serviceClasses.ServingItem
import com.example.muscletruth.data.models.Serving
import com.example.muscletruth.data.serviceClasses.Token
import com.example.muscletruth.data.models.Weighting
import com.example.muscletruth.data.serviceClasses.WeightingsChartData
import com.example.muscletruth.data.models.User
import com.example.muscletruth.data.serviceClasses.MealType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    //========================================AUTH========================================\\
    @Multipart
    @POST("api/auth/register")
    suspend fun createUser(@Part("user") user: RequestBody, @Part image: MultipartBody.Part? = null): Response<Token>

    @POST("api/auth/login")
    suspend fun authorizeUser(@Body user: Map<String, String>): Response<Token>



    //========================================USERS========================================\\
    @GET("api/users/me")
    suspend fun getUser(): Response<User>

    @POST("api/users/check-email")
    suspend fun checkEmail(@Field("email") email: String): Boolean

    @Multipart
    @PUT("api/users/me")
    suspend fun updateUser(@Part("user") user: RequestBody, @Part image: MultipartBody.Part? = null): Boolean

    @POST("api/users/me/check-password")
    suspend fun checkUserPassword(@Field("password") password: String): Boolean



    //========================================WEIGHTINGS========================================\\
    @GET("api/weightings")
    suspend fun getUserWeightings(@Query("start_date") startDate: String?, @Query("end_date") endDate: String?): List<Weighting>

    @GET("api/weightings/last")
    suspend fun getUserLastWeighting(): Weighting

    @Multipart
    @POST("api/weightings")
    suspend fun addWeighting(@Part("weighting") weighting: RequestBody, @Part image: MultipartBody.Part? = null): Result<Weighting>

    @Multipart
    @PUT("api/weightings/{weighting_id}")
    suspend fun updateWeighting(
        @Path("weighting_id") weighting_id: Int,
        @Part("weighting") weighting: RequestBody,
        @Part image: MultipartBody.Part? = null): Boolean

    @GET("api/weightings/chart/year")
    suspend fun getWeightingsYearChartData(): List<WeightingsChartData.YearChartData>

    @GET("api/weightings/chart/month")
    suspend fun getWeightingsMonthChartData(): List<WeightingsChartData.MonthChartData>



    //========================================PRODUCTS========================================\\
    @GET("api/products")
    suspend fun getProducts(@Query("search_query") searchQuery: String?): MutableList<Product>

    @GET("api/products/{product_id}")
    suspend fun getProduct(@Path("product_id") product_id: Int): Product

    @Multipart
    @POST("api/products")
    suspend fun addProduct(@Part("product") product: RequestBody, @Part image: MultipartBody.Part? = null): Response<Product>



    //========================================MEALS========================================\\
    @Multipart
    @POST("api/meals")
    suspend fun addMeal(@Part("meal") meal: RequestBody, @Part image: MultipartBody.Part? = null): Response<Meal>

    @Multipart
    @PUT("api/meals/{meal_id}")
    suspend fun updateMeal(@Path("meal_id") mealID: Int, @Part("meal") meal: RequestBody, @Part image: MultipartBody.Part? = null): Boolean
    @POST("/api/meals/{meal_id}/servings")
    suspend fun addServing(@Path("meal_id") mealID: Int, @Body serving: Serving): Response<Serving>

    @GET("/api/meals/{meal_id}/servings")
    suspend fun getServings(@Path("meal_id") mealID: Int): MutableList<Serving>

    @DELETE("/api/meals/{meal_id}/servings/{serving_id}")
    suspend fun deleteServing(@Path("meal_id") mealID: Int, @Path("serving_id") servingID: Int)

    @GET("/api/meals/{meal_id}")
    suspend fun getMeal(@Path("meal_id") mealID: Int): Meal
//
//    @GET("/api/meals")
//    suspend fun getMeals(@Query("start_date") startDate: String?, @Query("end_date") endDate: String?): List<Meal.MealBase>

    @GET("/api/meals/today")
    suspend fun getTodayMeals(): List<Meal>

    @DELETE("/api/meals/{meal_id}")
    suspend fun deleteMeal(@Path("meal_id") mealID: Int)

    @GET("/api/meals/{meal_type_id}/total")
    suspend fun getMealTypeTotal(@Path("meal_type_id") mealTypeID: Int, @Query("start_date") startDate: String, @Query("end_date") endDate: String): MealType
}