package com.example.muscletruth.utils

import android.net.Uri
import java.text.SimpleDateFormat
import java.util.Locale
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

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
    }

}