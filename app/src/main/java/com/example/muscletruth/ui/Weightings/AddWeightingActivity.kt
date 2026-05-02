package com.example.muscletruth.ui.Weightings

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
import com.example.muscletruth.R
import com.example.muscletruth.data.models.Weighting
import com.example.muscletruth.data.repository.WeightingRepository
import com.example.muscletruth.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

class AddWeightingActivity : AppCompatActivity() {
    private lateinit var selectImageLauncher: ActivityResultLauncher<Intent>
    private lateinit var picture: ImageView
    var imageURI: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_weighting)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        picture = findViewById<ImageView>(R.id.add_weighting_iv_weighting)
        selectImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                result ->
            if(result.resultCode == RESULT_OK){
                val selectedImageUri: Uri? = result.data?.data
                imageURI = selectedImageUri

                picture.setImageURI(selectedImageUri)
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
            else{
                weightingResultText.error = "Введите вес!"
            }
        }

        val saveButton = findViewById<Button>(R.id.add_weighting_btn_save)
        saveButton.setOnClickListener {
            if(weightingResultText.text.isNotEmpty()){
                val text = weightingResultText.text.toString()
                if(text.toDouble() <= 0 || text.toDouble() > 400){
                    weightingResultText.error = "Неподходящее значение!"
                    return@setOnClickListener
                }
                saveWeighting(text.toDouble())
            }
            else{
                weightingResultText.error = "Введите вес!"
            }
        }
    }

    private fun openGallery(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        selectImageLauncher.launch(intent)
    }

    private fun saveWeighting(weightingResult: Double){
        CoroutineScope(Dispatchers.IO).launch {
            var imagePart: MultipartBody.Part? = null
            if(imageURI != null){
                imagePart = imageURI?.let { uri ->
                    Utils.ImageUtils.createImagePart(this@AddWeightingActivity, uri)
                }
            }
            WeightingRepository.addWeighting(Weighting(result=weightingResult), imagePart, context = this@AddWeightingActivity, localPicture = imageURI)

            setResult(RESULT_OK)
            finish()
        }
    }
}