package com.example.muscletruth

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.w3c.dom.Text

class MyProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_my_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val userNameField = findViewById<TextView>(R.id.myProfileUserNameText)
        userNameField.text = intent.getStringExtra("userName")

        val userEmailField = findViewById<TextView>(R.id.myProfileEmailText)
        userEmailField.text = intent.getStringExtra("userEmail")

        val userPasswordField = findViewById<TextView>(R.id.myProfilePasswordText)
        userPasswordField.text = intent.getStringExtra("userPassword")
    }
}