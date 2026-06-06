package com.example.muscletruth.ui.Auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.muscletruth.R
import com.example.muscletruth.data.repositories.UserRepository
import com.example.muscletruth.ui.MainMenuActivity
import com.example.muscletruth.utils.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthorizationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_authorization)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_authorization)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val enterButton = findViewById<Button>(R.id.auth_btn_login)
        enterButton.setOnClickListener {
            val email = findViewById<TextView>(R.id.auth_et_email)
            val password = findViewById< TextView>(R.id.auth_et_password)

            authorizeUser(email, password)
        }

        val backButton = findViewById<Button>(R.id.auth_btn_back)
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun authorizeUser(emailField: TextView, passwordField: TextView) {
        val email = emailField.text.toString()
        if(emailField.length() == 0){
            emailField.error = "Введите email!"
            return
        }
        val password = passwordField.text.toString()
        if(passwordField.length() == 0){
            passwordField.error = "Введите пароль!"
            return
        }

        val manager = PreferencesManager(this)
        CoroutineScope(Dispatchers.IO).launch {
            val result = UserRepository.login(email, password)

            withContext(Dispatchers.Main) {
                result.onSuccess {response ->
                    manager.saveUserId(response.userID)
                    manager.saveAuthToken(response.accessToken)

                    val intent = Intent(this@AuthorizationActivity, MainMenuActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }.onFailure { error ->
                    if(error.message?.contains("401") == true){
                        Toast.makeText(
                            this@AuthorizationActivity,
                            "Неверные данные!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    else{
                        Toast.makeText(
                            this@AuthorizationActivity,
                            "Ошибка авторизации!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }
}