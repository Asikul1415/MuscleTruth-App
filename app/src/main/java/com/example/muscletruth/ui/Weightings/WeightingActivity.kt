package com.example.muscletruth.ui.Weightings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.muscletruth.R
import com.example.muscletruth.data.models.Weighting
import com.example.muscletruth.data.repository.UserRepository
import com.example.muscletruth.utils.Utils.NetworkUtils.checkForInternetConnection
import com.example.muscletruth.data.repository.WeightingRepository
import com.example.muscletruth.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class WeightingActivity : AppCompatActivity() {
    private lateinit var selectImageLauncher: ActivityResultLauncher<Intent>
    private lateinit var picture: ImageView
    var imageURI: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_weighting)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val weighting = intent.getParcelableExtra<Weighting>("weighting")

        picture = findViewById<ImageView>(R.id.weighting_iv)
        selectImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                result ->
            if(result.resultCode == RESULT_OK){
                val selectedImageUri: Uri? = result.data?.data

                picture.setImageURI(selectedImageUri)
                imageURI = selectedImageUri
            }
        }
        val imageButton = findViewById<Button>(R.id.weighting_btn_image)
        imageButton.setOnClickListener {
            Utils.ImageUtils.openGallery(selectImageLauncher)
        }

        if(checkForInternetConnection() && weighting?.serverPicture != null){
            Glide.with(this@WeightingActivity)
                .load(Utils.ImageUtils.getImagePath(weighting.serverPicture!!))
                .placeholder(R.drawable.ic_launcher_foreground)
                .override(250, 300)
                .into(picture)
        }
        else{
            if(weighting !== null){
                lifecycleScope.launch {
//                    val localWeighting = UserRepository.localDb.weightingDao().getWeighting(weighting.localID)
                    Glide.with(this@WeightingActivity)
                        .load(weighting.localPicture)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .override(250, 300)
                        .into(picture)
                }
            }
        }

        val tvDate = findViewById<TextView>(R.id.weighting_tv_date)
        tvDate.text = ZonedDateTime.parse(weighting?.creationDate).format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        )

        val etResult = findViewById<EditText>(R.id.weighting_et_result)
        etResult.setText(weighting?.result.toString())
        etResult.addTextChangedListener {
            val text = it.toString()
            if(text.isNotEmpty()){
                val value = text.toDoubleOrNull()
                if(value != null && (value <= 0 || value > 400)){
                    etResult.error = "Неподходящее значение!"
                }

                val splitText = text.split(".")
                //If decimal part length is more than two
                if(splitText.count() == 2 && splitText[1].length > 2){
                    etResult.setText(text.dropLast(1));
                    etResult.setSelection(etResult.text.length)
                }

            }
        }

        val saveButton = findViewById<Button>(R.id.weighting_btn_save)
        saveButton.setOnClickListener {
            val weightingResult = etResult.text.toString().toDouble()
            if(weighting != null){
                if(etResult.length() < 2){
                    etResult.error = "Введите корректный вес!"
                    return@setOnClickListener
                }
                if(weightingResult <= 0 || weightingResult > 400){
                    etResult.error = "Введите корректный вес!"
                    return@setOnClickListener
                }

                if(weighting.result.toDouble() == weightingResult && imageURI == null){
                    finish()
                }
                else{
                    var updatedWeighting = weighting
                    updatedWeighting.result = weightingResult

                    lifecycleScope.launch {
                        var imagePart: MultipartBody.Part? = null
                        if(imageURI != null){
                            imagePart = imageURI?.let { uri ->
                                Utils.ImageUtils.createImagePart(this@WeightingActivity, uri)
                            }
                        }

                        with(Dispatchers.IO){
                            val isWeightingUpdateSuccessful = WeightingRepository.updateWeighting(weighting.serverID!!, updatedWeighting, imagePart)
                            if(isWeightingUpdateSuccessful){
                                Toast.makeText(this@WeightingActivity, "Успешно сохранено", Toast.LENGTH_LONG).show()
                                finish()
                            }
                        }
                    }
                }
            }
            else{
                Toast.makeText(this@WeightingActivity, "Ошибка! Взвешивания не существует!", Toast.LENGTH_LONG).show()
            }
        }
    }
}