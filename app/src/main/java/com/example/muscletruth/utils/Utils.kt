package com.example.muscletruth.utils

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

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

}