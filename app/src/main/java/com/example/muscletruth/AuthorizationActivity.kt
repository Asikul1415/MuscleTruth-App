package com.example.muscletruth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.muscletruth.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthorizationActivity : AppCompatActivity() {
    private val userRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_authorization)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_authorization)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val enterButton = findViewById<Button>(R.id.authorizationEnterButton)
        enterButton.setOnClickListener {
            val email = findViewById<TextView>(R.id.authorizationEmailText).text.toString()
            val password = findViewById< TextView>(R.id.authorizationPasswordText).text.toString()

            authorizeUser(email, password)
        }
    }

    private fun authorizeUser(email: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = userRepository.login(email, password)

            withContext(Dispatchers.Main) {
                result.onSuccess {response ->
                    saveUserId(response.userID)

                    val intent = Intent(this@AuthorizationActivity, MainMenuActivity::class.java)
                    intent.putExtra("user_id", response.userID)
                    startActivity(intent)
                }.onFailure { error ->
                    Toast.makeText(
                        this@AuthorizationActivity,
                        "Ошибка: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun saveUserId(userId: Int) {
        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putInt("user_id", userId)
            apply()
        }
    }
}