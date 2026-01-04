package com.example.muscletruth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AuthorizationActivity : AppCompatActivity() {
    val username = "sam_sulek"
    val correctEmail = "samsulek14@yandex.ru"
    val correctPassword = "1020304050"

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
            val emailField = findViewById<TextView>(R.id.authorizationEmailText)
            val passwordField = findViewById< TextView>(R.id.authorizationPasswordText)
            Log.i("authorizationLogging", "Авторизация:")
            Log.i("authorizationLogging", "email: ${emailField.text.toString()}, password: ${passwordField.text.toString()}")
            Log.i("authorizationLogging", "correctEmail: ${correctEmail}, password: ${correctPassword}")

            if(emailField.text.toString() == correctEmail && passwordField.text.toString() == correctPassword){
                val intent = Intent(this, MainMenuActivity::class.java)
                intent.putExtra("username", username)
                intent.putExtra("email", emailField.text.toString())
                intent.putExtra("password", passwordField.text.toString())
                startActivity(intent)
            }
            else{
                val errorText = findViewById< TextView>(R.id.authorizationErrorText)
                errorText.text = "Неправильные данные!"
            }
        }
    }
}