package com.example.muscletruth

import android.content.Context
import android.content.SharedPreferences
import com.example.muscletruth.data.api.ApiClient

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun saveAuthToken(token: String) {
        prefs.edit().putString("auth_token", token).commit()
        ApiClient.setAuthToken(token)
    }

    fun getAuthToken(): String? = prefs.getString("auth_token", null)

    fun clearAuthToken() {
        prefs.edit().remove("auth_token").commit()
        ApiClient.clearAuthToken()
    }

    fun saveUserId(userId: Int) {
        prefs.edit().putInt("user_id", userId).commit()
    }

    fun clearUserId() {
        prefs.edit().remove("user_id").commit()
    }

    fun getUserId(): Int = prefs.getInt("user_id", -1)
}