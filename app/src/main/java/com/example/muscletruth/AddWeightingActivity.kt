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
import androidx.core.widget.addTextChangedListener
import com.example.muscletruth.data.api.models.Weighting
import com.example.muscletruth.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddWeightingActivity : AppCompatActivity() {
    private val userRepository = UserRepository()
    private lateinit var selectImageLauncher: ActivityResultLauncher<Intent>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_weighting)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        selectImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
                if(result.resultCode == Activity.RESULT_OK){
                    val selectedImageUri: Uri? = result.data?.data
                    val imageView = findViewById<ImageView>(R.id.add_weighting_iv_weighting)

                    imageView.setImageURI(selectedImageUri)
                }
        }

        val addImageButton = findViewById<Button>(R.id.add_weighting_btn_attach)
        addImageButton.setOnClickListener {
            openGallery()
        }

        val weightingResultText = findViewById<EditText>(R.id.add_weighting_et_result)
        weightingResultText.addTextChangedListener {
            val text = it.toString()
            if(text.isNotEmpty()){
                val value = text.toDoubleOrNull()
                if(value != null && (value <= 0 || value > 400)){
                    weightingResultText.error = "Неподходящее значение!"
                }

                val splitText = text.split(".")
                //If decimal part length is more than two
                if(splitText.count() == 2 && splitText[1].length > 2){
                    weightingResultText.setText(text.dropLast(1));
                    weightingResultText.setSelection(weightingResultText.text.length)
                }

            }
        }

        val saveButton = findViewById<Button>(R.id.add_weighting_btn_save)
        saveButton.setOnClickListener {
            val text = weightingResultText.text.toString()
            saveWeighting(text.toDouble())
        }
    }

    private fun openGallery(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        selectImageLauncher.launch(intent)
    }

    private fun saveWeighting(weightingResult: Double){
        CoroutineScope(Dispatchers.IO).launch {
            userRepository.addWeighting(Weighting.WeightingBase(result=weightingResult))
        }
        finish()
    }
}