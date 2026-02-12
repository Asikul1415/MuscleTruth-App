package com.example.muscletruth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.muscletruth.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainMenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_menu)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        lifecycleScope.launch {
            val userResponse = withContext(Dispatchers.IO){
                UserRepository().getUser()
            }
            userResponse.onSuccess{}.onFailure { error ->
                Toast.makeText(this@MainMenuActivity, "Вы не авторизованы! Авторизуйтесь ещё раз!", Toast.LENGTH_LONG).show()
                Log.e("APP_DEBUG", "${error.toString()}")

                val manager = PreferencesManager(this@MainMenuActivity)
                manager.clearAuthToken()
                manager.clearUserId()

                val intent = Intent(this@MainMenuActivity, EnterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }

        val addWeightingButton = findViewById<Button>(R.id.main_menu_btn_weightings)
        addWeightingButton.setOnClickListener {
            val intent = Intent(this, WeightingsActivity::class.java)
            startActivity(intent)
        }

        val addMealButton = findViewById<Button>(R.id.main_menu_btn_add_meal)
        addMealButton.setOnClickListener {
            val intent = Intent(this, MealsActivity::class.java)
            startActivity(intent)
        }

        val myProfileButton = findViewById<Button>(R.id.main_menu_btn_profile)
        myProfileButton.setOnClickListener {
            val intent = Intent(this, MyProfileActivity::class.java)
            startActivity(intent)
        }
    }
}