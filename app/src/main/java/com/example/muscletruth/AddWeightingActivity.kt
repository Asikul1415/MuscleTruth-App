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

class AddWeightingActivity : AppCompatActivity() {
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
                    val imageView = findViewById<ImageView>(R.id.addWeightingWeightingImage)

                    imageView.setImageURI(selectedImageUri)
                }
        }

        val addImageButton = findViewById<Button>(R.id.addWeightingAddImageButton)
        addImageButton.setOnClickListener {
            openGallery()
        }

        val weightingResultText = findViewById<EditText>(R.id.addWeightingWeightingResultText)
        weightingResultText.addTextChangedListener {
            val text = it.toString()
            if(text.isNotEmpty()){

                //Value validation
                val value = text.toDoubleOrNull()
                if(value != null && (value <= 0 || value > 400)){
                    weightingResultText.error = "Неподходящее значение!"
                }

                //Decimal part symbols count validation. If more than two, remove last
                val splitText = text.split(".")
                if(splitText.count() == 2 && splitText[1].length > 2){
                    weightingResultText.setText(text.dropLast(1));
                    weightingResultText.setSelection(weightingResultText.text.length)
                }

            }
        }

        val saveButton = findViewById<Button>(R.id.saveWeightingAddWeightingButton)
        saveButton.setOnClickListener {
            saveWeighting(weightingResultText.text.toString().toInt())
        }
    }

    private fun openGallery(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        selectImageLauncher.launch(intent)
    }

    private fun saveWeighting(weightingResult: Int){
        val intent = Intent(this, MainMenuActivity::class.java)
        intent.putExtra("lastWeighting", weightingResult)
        startActivity(intent)
    }
}