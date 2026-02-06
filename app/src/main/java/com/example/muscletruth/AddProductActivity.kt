package com.example.muscletruth

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.muscletruth.data.api.models.Product
import com.example.muscletruth.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddProductActivity : AppCompatActivity() {
    private val userRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_product)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val titleEt = findViewById<EditText>(R.id.add_product_et_title)
        val proteinsEt = findViewById<EditText>(R.id.add_product_et_proteins)
        val fatsEt = findViewById<EditText>(R.id.add_product_et_fats)
        val carbsEt = findViewById<EditText>(R.id.add_product_et_carbs)

        val saveButton = findViewById<Button>(R.id.add_product_btn_save)
        saveButton.setOnClickListener {
            lifecycleScope.launch {
                withContext(Dispatchers.IO){
                    val product = Product.ProductBase(
                        title = titleEt.text.toString(),
                        proteins = proteinsEt.text.toString().toInt(),
                        fats = fatsEt.text.toString().toInt(),
                        carbs = carbsEt.text.toString().toInt()
                    )
                    userRepository.addProduct(product)
                    finish()
                }

            }
        }
    }
}