package com.example.muscletruth

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.muscletruth.data.api.models.Product
import com.example.muscletruth.data.repository.UserRepository
import com.example.muscletruth.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody

class AddProductActivity : AppCompatActivity() {
    private val userRepository = UserRepository()
    private lateinit var titleField: EditText
    private lateinit var proteinsField: EditText
    private lateinit var fatsField: EditText
    private lateinit var carbsField: EditText
    private lateinit var selectImageLauncher: ActivityResultLauncher<Intent>
    var imageURI: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_product)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        titleField = findViewById<EditText>(R.id.add_product_et_title)
        proteinsField = findViewById<EditText>(R.id.add_product_et_proteins)
        fatsField = findViewById<EditText>(R.id.add_product_et_fats)
        carbsField = findViewById<EditText>(R.id.add_product_et_carbs)
        val image = findViewById<ImageView>(R.id.add_product_iv)

        val addImage = findViewById<Button>(R.id.add_product_btn_picture)
        addImage.setOnClickListener {
            Utils.ImageUtils.openGallery(selectImageLauncher)
        }
        selectImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                result ->
            if(result.resultCode == Activity.RESULT_OK){
                val selectedImageUri: Uri? = result.data?.data

                image.setImageURI(selectedImageUri)
                imageURI = selectedImageUri
            }
        }

        val saveButton = findViewById<Button>(R.id.add_product_btn_save)
        saveButton.setOnClickListener {
            val title = titleField.text.toString()
            if(title.length == 0){
                titleField.error = "Введите название продукта!"
                return@setOnClickListener
            }
            else if(proteinsField.length() == 0){
                proteinsField.error = "Введите кол-во белков!"
                return@setOnClickListener
            }
            else if(fatsField.length() == 0){
                fatsField.error = "Введите кол-во жиров!"
                return@setOnClickListener
            }
            else if(carbsField.length() == 0){
                carbsField.error = "Введите кол-во углеводов!"
                return@setOnClickListener
            }

            lifecycleScope.launch {
                withContext(Dispatchers.IO){
                    var imagePart: MultipartBody.Part? = null
                    if(imageURI != null){
                        imagePart = imageURI?.let { uri ->
                            Utils.ImageUtils.createImagePart(this@AddProductActivity, uri)
                        }
                    }
                    val product = Product.ProductBase(
                        title = title,
                        proteins = proteinsField.text.toString().toIntOrNull() ?: 0,
                        fats = fatsField.text.toString().toIntOrNull() ?: 0,
                        carbs = carbsField.text.toString().toIntOrNull() ?: 0
                    )
                    userRepository.addProduct(product, imagePart)
                    val intent = Intent()
                    intent.putExtra("productTitle", product.title)
                    setResult(RESULT_OK, intent)
                    finish()
                }

            }
        }
    }
}