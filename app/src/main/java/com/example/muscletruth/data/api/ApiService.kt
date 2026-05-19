package com.example.muscletruth.data.api

import com.example.muscletruth.data.models.FavouriteProduct
import com.example.muscletruth.data.models.Meal
import com.example.muscletruth.data.models.Product
import com.example.muscletruth.data.models.RecentServing
import com.example.muscletruth.data.models.SavedMeal
import com.example.muscletruth.data.models.Serving
import com.example.muscletruth.data.serviceClasses.Token
import com.example.muscletruth.data.models.Weighting
import com.example.muscletruth.data.serviceClasses.WeightingsChartData
import com.example.muscletruth.data.models.User
import com.example.muscletruth.data.serviceClasses.CaloriesChartData
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
    suspend fun getUser(): User

    @FormUrlEncoded
    @POST("api/users/check-email")
    suspend fun checkIfEmailRegistered(@Field("email") email: String): Boolean

    @Multipart
    @PUT("api/users/me")
    suspend fun updateUser(@Part("user") user: RequestBody, @Part image: MultipartBody.Part? = null): Boolean

    @FormUrlEncoded
    @POST("api/users/me/check-password")
    suspend fun checkUserPassword(@Field("password") password: String): Boolean



    //========================================WEIGHTINGS========================================\\
    @GET("api/weightings")
    suspend fun getUserWeightings(@Query("start_date") startDate: String?, @Query("end_date") endDate: String?): List<Weighting>

    @GET("api/weightings/last")
    suspend fun getUserLastWeighting(): Weighting

    @Multipart
    @POST("api/weightings")
    suspend fun addWeighting(@Part("weighting") weighting: RequestBody, @Part image: MultipartBody.Part? = null): Response<Weighting>

    @Multipart
    @PUT("api/weightings/{weighting_id}")
    suspend fun updateWeighting(
        @Path("weighting_id") weighting_id: Int,
        @Part("weighting") weighting: RequestBody,
        @Part image: MultipartBody.Part? = null): Boolean

    @DELETE("api/weightings/{weighting_id}")
    suspend fun deleteWeighting(@Path("weighting_id") weightingServerID: Int)

    @GET("api/weightings/chart/year")
    suspend fun getWeightingsYearChartData(): List<WeightingsChartData.YearChartData>

    @GET("api/weightings/chart/month")
    suspend fun getWeightingsMonthChartData(): List<WeightingsChartData.MonthChartData>



    //========================================PRODUCTS========================================\\
    @GET("api/products")
    suspend fun getProducts(@Query("search_query") searchQuery: String?): MutableList<Product>

    @GET("api/products/favourites")
    suspend fun getFavouriteProducts(): MutableList<FavouriteProduct>

    @GET("api/products/favourites/{product_id}")
    suspend fun getFavouriteProduct(@Path("product_id") productID: Int): FavouriteProduct

    @GET("api/products/{product_id}")
    suspend fun getProduct(@Path("product_id") product_id: Int): Product

    @Multipart
    @POST("api/products")
    suspend fun addProduct(@Part("product") product: RequestBody, @Part image: MultipartBody.Part? = null): Response<Product>

    @FormUrlEncoded
    @POST("api/products/favourites")
    suspend fun addFavouriteProduct(@Field("product_id") productID: Int): FavouriteProduct

    @DELETE("api/products/favourites/{product_id}")
    suspend fun deleteFavouriteProduct(@Path("product_id") productID: Int): Boolean



    //========================================MEALS========================================\\
    @Multipart
    @POST("api/meals")
    suspend fun addMeal(@Part("meal") meal: RequestBody, @Part image: MultipartBody.Part? = null): Response<Meal>

    @Multipart
    @PUT("api/meals/{meal_id}")
    suspend fun updateMeal(@Path("meal_id") mealID: Int, @Part("meal") meal: RequestBody, @Part image: MultipartBody.Part? = null): Boolean

    @GET("/api/meals")
    suspend fun getMeals(@Query("start_date") startDate: String?, @Query("end_date") endDate: String?): List<Meal>

    @GET("/api/meals/{meal_id}")
    suspend fun getMeal(@Path("meal_id") mealID: Int): Meal

    @GET("/api/meals/today")
    suspend fun getTodayMeals(): List<Meal>

    @DELETE("/api/meals/{meal_id}")
    suspend fun deleteMeal(@Path("meal_id") mealID: Int)

    @GET("/api/meals/{meal_type_id}/total")
    suspend fun getMealTypeTotal(@Path("meal_type_id") mealTypeID: Int, @Query("start_date") startDate: String, @Query("end_date") endDate: String): MealType

    @GET("api/meals/chart/year")
    suspend fun getAverageCaloriesYearChartData(): List<CaloriesChartData.ChartDataByWeek>

    @GET("api/meals/chart/month")
    suspend fun getAverageCaloriesMonthChartData(): List<CaloriesChartData.ChartDataByDay>

    @GET("api/meals/chart/week")
    suspend fun getAverageCaloriesWeekChartData(): List<CaloriesChartData.ChartDataByDay>

    @GET("/api/meals/saved")
    suspend fun getSavedMeals(): List<SavedMeal>

    @GET("/api/meals/saved/{meal_id}")
    suspend fun getSavedMeal(@Path("meal_id") mealID: Int): Response<SavedMeal?>

    @FormUrlEncoded
    @POST("api/meals/saved")
    suspend fun addSavedMeal(@Field("title") title: String, @Field("meal_id") mealID: Int): SavedMeal

    @DELETE("/api/meals/saved/{meal_id}")
    suspend fun deleteSavedMeal(@Path("meal_id") mealID: Int)

    //======================================SERVINGS========================================\\

    @POST("/api/servings")
    suspend fun addServing(@Query("meal_id") mealID: Int, @Body serving: Serving): Response<Serving>

    @FormUrlEncoded
    @POST("api/servings/recent")
    suspend fun addRecentServing(@Field("serving_id") servingID: Int): RecentServing

    @GET("/api/servings")
    suspend fun getServings(): MutableList<Serving>

    @GET("/api/servings")
    suspend fun getMealServings(@Query("meal_id") mealID: Int): MutableList<Serving>

    @GET("/api/servings/{serving_id}")
    suspend fun getServing(@Path("serving_id") servingID: Int): Serving

    @GET("/api/servings/recent")
    suspend fun getRecentServings(): MutableList<RecentServing>

    @GET("/api/servings/recent/{serving_id}")
    suspend fun getRecentServing(@Path("serving_id") servingID: Int): RecentServing?

    @DELETE("/api/servings/{serving_id}")
    suspend fun deleteServing(@Path("serving_id") servingID: Int)

    @DELETE("/api/servings/recent/{serving_id}")
    suspend fun deleteRecentServing(@Path("serving_id") servingID: Int): Boolean

    @PUT("/api/servings/{serving_id}")
    suspend fun updateServing(@Path("serving_id") servingID: Int, @Body serving: Serving)
}