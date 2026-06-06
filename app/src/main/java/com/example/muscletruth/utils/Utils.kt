package com.example.muscletruth.utils

import android.net.Uri
import java.text.SimpleDateFormat
import java.util.Locale
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit

class Utils {
    object DateUtils {
        public fun convertTimestamp(timestamp: String?): String{
            val result = timestamp.toString()
            return try{
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd-MM", Locale.getDefault())

                val date = inputFormat.parse(result)
                outputFormat.format(date)
            }
            catch(e: Exception){
                result
            }
        }
    }

    object NetworkUtils {
        public lateinit var connectivityManager: ConnectivityManager
        fun checkForInternetConnection(): Boolean{
            return connectivityManager.activeNetwork !== null
        }
    }

    object ImageUtils {
        const val BASE_URL = "http://10.0.2.2:8000"

        fun getImagePath(path: String): String{
            val cleanPath = path.removePrefix("/")
            return "$BASE_URL/$cleanPath"
        }

        fun createImagePart(context: Context, uri: Uri): MultipartBody.Part? {
            val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
            return bytes?.let {
                val requestBody = it.toRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("image", "upload.jpg", requestBody)
            }
        }

        fun openGallery(selectImageLauncher: ActivityResultLauncher<Intent>){
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            selectImageLauncher.launch(intent)
        }

        suspend fun copyImageToLocalStorage(context: Context, uri: Uri): String? = withContext(Dispatchers.IO){
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val filename = "${UUID.randomUUID()}.jpeg";
            val file = File(context.filesDir, "images/${filename}");
            file.parentFile?.mkdirs();
            file.outputStream().use {output ->
                inputStream.copyTo(output);
            }

            return@withContext file.absolutePath;
        }

        suspend fun saveImageFromServer(context: Context, url: String): String? = withContext(Dispatchers.IO){
            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()
            val request = Request.Builder().url(url).build()

            try{
                val response = client.newCall(request).execute()
                if(response.isSuccessful === false) return@withContext null

                response.body?.let { body ->
                    val filename = "${UUID.randomUUID()}.jpeg"
                    val file = File(context.filesDir, "images/${filename}")
                    file.parentFile?.mkdirs()
                    file.outputStream().use {out -> body.byteStream().copyTo(out)}
                    return@withContext file.absolutePath
                }
            }
            catch (e: Exception){
                Log.e("APP_DEBUG", "SYNC: Image download FAIL: - ${e.toString()}");
                return@withContext null
            }
        }
    }

}