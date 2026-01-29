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
    suspend fun getUserWeightings(): List<Weighting.WeightingBase>

    @POST("api/weightings")
    suspend fun addWeighting(@Body weighting: Weighting.WeightingBase): Response<Weighting.WeightingResponse>
}