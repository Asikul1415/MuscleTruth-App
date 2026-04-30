package com.example.muscletruth.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.muscletruth.data.api.ApiClient
import com.example.muscletruth.data.models.Weighting
import com.example.muscletruth.data.serviceClasses.WeightingsChartData
import com.example.muscletruth.utils.Utils.NetworkUtils.checkForInternetConnection
import com.example.muscletruth.utils.Utils
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.LocalDate
import com.example.muscletruth.data.repository.UserRepository.localDb
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

object WeightingRepository {
    private val apiService = ApiClient.apiService

    suspend fun getWeightings(startDate: String? = null, endDate: String? = LocalDate.now().toString()): List<Weighting> {
        try{
            //SERVER
            if(checkForInternetConnection()){
                val response = apiService.getUserWeightings(startDate = startDate, endDate = endDate).map { weighting ->
                    var localWeighting = localDb.weightingDao().getServerWeighting(weighting.serverID)
                    val localID = localWeighting?.localID ?: UUID.randomUUID().toString()
                    weighting.copy(localID = localID)
                }
                return response
            }


            //LOCAL
            val weightings = localDb.weightingDao().getWeightingsByDate(startDate = startDate, endDate = endDate)
            Log.d("APP_DEBUG", "LOCAL_GET: WEIGHTINGS ${weightings}")
            return weightings
        }
        catch(e: Exception){
            Log.e("APP_DEBUG", "LOCAL_GET: WEIGHTINGS ERROR ${e.toString()}")
            return emptyList()
        }
    }

    suspend fun getLastWeighting(): Weighting?{
        try{
            //SERVER
            if(checkForInternetConnection()){
                return apiService.getUserLastWeighting()
            }


            //LOCAL
            return localDb.weightingDao().getLastWeighting()
        }
        catch(e: Exception){
            Log.d("APP_DEBUG", "${e.toString()}")
            return null
        }
    }

    suspend fun getWeightingsMonthChartData(): List<WeightingsChartData.MonthChartData>{
        try{
            //SERVER
            if(checkForInternetConnection()){
                val response = apiService.getWeightingsMonthChartData()
                return response
            }


            //LOCAL
            val end = LocalDate.now()
            val start = end.minusMonths(1)
            val weightings = localDb.weightingDao().getWeightingsFrom(start.toString())

            return weightings
                .groupBy { it.creationDate?.substring(0, 10) } // group by day (yyyy-MM-dd)
                .map { (day, list) ->
                    WeightingsChartData.MonthChartData(day!!, list.map { it.result }.average())
                }
                .sortedBy { it.day }
        }
        catch(e: Exception){
            Log.d("APP_DEBUG", "${e.toString()}")
            return emptyList()
        }
    }

    suspend fun getWeightingsYearChartData(): List<WeightingsChartData.YearChartData>{
        try{
            //SERVER
            if(checkForInternetConnection()){
                val response = apiService.getWeightingsYearChartData()
                return response
            }


            //LOCAL
            val end = LocalDate.now()
            val start = end.minusYears(1)
            val weightings = localDb.weightingDao().getWeightingsFrom(start.toString())

            fun getWeekStart(dateStr: String): String {
                val date = LocalDate.parse(dateStr.substring(0, 10))

                val monday = date.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                return monday.toString()
            }

            return weightings
                .groupBy { getWeekStart(it.creationDate!!) }
                .map { (weekStart, list) ->
                    WeightingsChartData.YearChartData(weekStart, list.map { it.result }.average())
                }
                .sortedBy { it.week_start }

        }
        catch(e: Exception){
            Log.d("APP_DEBUG", "${e.toString()}")
            return emptyList()
        }
    }

    suspend fun addWeighting(weighting: Weighting, image: MultipartBody.Part? = null, localPicture: Uri? = null, context: Context): Result<Weighting> {
        return try{
            var serverWeighting: Weighting? = null
            //SERVER
            if(checkForInternetConnection()){
                val weightingJson = Gson().toJson(weighting)
                val weightingBody = weightingJson.toRequestBody("application/json".toMediaTypeOrNull())

                apiService.addWeighting(weightingBody, image).onSuccess {addedWeighting ->
                    serverWeighting = addedWeighting
                }
            }


            //LOCAL
            var weightingToInsert = serverWeighting ?: Weighting(result = weighting.result)

            if(localPicture !== null){
                weightingToInsert.localPicture = Utils.ImageUtils.copyImageToLocalStorage(context, localPicture) ?: ""
            }
            if(weightingToInsert.serverPicture !== null) {
                val url = weightingToInsert.serverPicture!!
                Utils.ImageUtils.saveImageFromServer(context, Utils.ImageUtils.getImagePath(url))
            }
            if(weightingToInsert.localID === null){
                weightingToInsert.localID = UUID.randomUUID().toString()
            }
            if(weightingToInsert.creationDate === null){
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX")
                weightingToInsert.creationDate = OffsetDateTime.now().format(formatter)
            }

            localDb.weightingDao().insert(weightingToInsert)
            Log.d("APP_DEBUG","LOCAL_ADD: WEIGHTING ${weightingToInsert}")
            Result.success(weightingToInsert);
        } catch(e: Exception){
            Log.e("APP_DEBUG","LOCAL_ADD: WEIGHTING ERROR ${e.toString()}")
            Result.failure(e)
        }
    }

    suspend fun updateWeighting(weightingID: Int, weighting: Weighting, image: MultipartBody.Part? = null): Boolean{
        try {
            //SERVER
            if(checkForInternetConnection()){
                val weightingJson = Gson().toJson(weighting)
                val weightingBody = weightingJson.toRequestBody("application/json".toMediaTypeOrNull())

                return apiService.updateWeighting(weighting_id = weightingID, weighting = weightingBody, image = image )
            }

            //LOCAL
            localDb.weightingDao().update(weighting)
            return true;
        } catch (e: Exception) {
            Log.e("APP_DEBUG", "${e.toString()}")
            return false;
        }
    }
}