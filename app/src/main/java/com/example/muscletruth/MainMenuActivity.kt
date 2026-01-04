package com.example.muscletruth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
class MainMenuActivity : AppCompatActivity() {
    private var username = ""
    private var email = ""
    private var password = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_menu)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        username = intent.getStringExtra("username").toString()
        email = intent.getStringExtra("email").toString()
        password = intent.getStringExtra("password").toString()
        Log.i("mainMenuLogging", "username: ${username}; email: ${email}; password: ${password}")

        val addWeightingButton = findViewById<Button>(R.id.mainMenuAddWeightingButton)
        addWeightingButton.setOnClickListener {
            val intent = Intent(this, AddWeightingActivity::class.java)
            startActivity(intent)
        }

        val addMealButton = findViewById<Button>(R.id.mainMenuAddMealButton)
        addMealButton.setOnClickListener {
            val intent = Intent(this, AddMealActivity::class.java)
            startActivity(intent)
        }

        val addProductButton = findViewById<Button>(R.id.mainMenuAddProductButton)
        addProductButton.setOnClickListener {
            val intent = Intent(this, AddProductActivity::class.java)
            startActivity(intent)
        }

        val myProfileButton = findViewById<Button>(R.id.mainMenuMyProfileButton)
        myProfileButton.setOnClickListener {
            val intent = Intent(this, MyProfileActivity::class.java)
            intent.putExtra("userName", username)
            intent.putExtra("userEmail", email)
            intent.putExtra("userPassword", password)
            intent.putExtra("userProfilePictureURI", "something about cats")
            startActivity(intent)
        }
    }
}