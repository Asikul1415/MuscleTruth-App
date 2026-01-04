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
import org.w3c.dom.Text

class RegistrationActivity : AppCompatActivity() {
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
            val usernameField = findViewById<TextView>(R.id.registrationUsernameText)
            if(usernameField.length() == 0){
                usernameField.setError("Пустое поле!")
                return@setOnClickListener
            }

            val emailField = findViewById<TextView>(R.id.registrationEmailText)
            if(emailField.length() == 0){
                emailField.setError("Пустое поле!")
                return@setOnClickListener
            }

            val passwordField = findViewById<TextView>(R.id.registrationPasswordText)
            if(passwordField.length() == 0){
                passwordField.setError("Пустое поле!")
                return@setOnClickListener
            }

            Log.i("registrationLogging", "Регистрация")
            Log.i("registrationLogging", "username: ${usernameField.text.toString()}; email: ${emailField.text.toString()}; password: ${passwordField.text.toString()}")

            val intent = Intent(this, MainMenuActivity::class.java)
            intent.putExtra("username", usernameField.text.toString())
            intent.putExtra("email", emailField.text.toString())
            intent.putExtra("password", passwordField.text.toString())
            startActivity(intent)
        }
    }
}