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

import com.example.muscletruth.data.api.models.User.UserCreate
import com.example.muscletruth.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegistrationActivity : AppCompatActivity() {
    private val userRepository = UserRepository()

    lateinit var usernameField: TextView;
    lateinit var emailField: TextView;
    lateinit var passwordField: TextView;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registration)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_registration)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val enterButton = findViewById<Button>(R.id.registrationEnterButton)
        enterButton.setOnClickListener {
            usernameField = findViewById<TextView>(R.id.registrationUsernameText)
            if(usernameField.length() == 0){
                usernameField.setError("Пустое поле!")
                return@setOnClickListener
            }

            emailField = findViewById<TextView>(R.id.registrationEmailText)
            if(emailField.length() == 0){
                emailField.setError("Пустое поле!")
                return@setOnClickListener
            }

            passwordField = findViewById<TextView>(R.id.registrationPasswordText)
            if(passwordField.length() == 0){
                passwordField.setError("Пустое поле!")
                return@setOnClickListener
            }

            registerUser()
        }
    }

    private fun registerUser() {
        val newUser = UserCreate(
            name = usernameField.text.toString(),
            email = emailField.text.toString(),
            password = passwordField.text.toString(),
            age = 25  //Need to add age field in future
        )

        CoroutineScope(Dispatchers.IO).launch {
            val result = userRepository.registerUser(newUser)

            withContext(Dispatchers.Main) {
                result.onSuccess {user ->
                    saveUserId(user.id)

                    val intent = Intent(this@RegistrationActivity, MainMenuActivity::class.java)
                    intent.putExtra("user_id", user.id)
                    startActivity(intent)
                }.onFailure { error ->
                    Toast.makeText(
                        this@RegistrationActivity,
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