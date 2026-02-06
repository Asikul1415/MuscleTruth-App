package com.example.muscletruth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

        val addProductButton = findViewById<Button>(R.id.main_menu_btn_products)
        addProductButton.setOnClickListener {
            val intent = Intent(this, AddProductActivity::class.java)
            startActivity(intent)
        }

        val myProfileButton = findViewById<Button>(R.id.main_menu_btn_profile)
        myProfileButton.setOnClickListener {
            val intent = Intent(this, MyProfileActivity::class.java)
            startActivity(intent)
        }
    }
}