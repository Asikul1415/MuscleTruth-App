package com.example.muscletruth

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.muscletruth.data.api.models.User.UserCreate
import com.example.muscletruth.data.repository.UserRepository
import com.example.muscletruth.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody

class RegistrationActivity : AppCompatActivity() {
    private val userRepository = UserRepository()

    lateinit var usernameField: TextView;
    lateinit var emailField: TextView;
    lateinit var passwordField: TextView;
    private lateinit var selectImageLauncher: ActivityResultLauncher<Intent>
    var imageURI: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registration)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_registration)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val enterButton = findViewById<Button>(R.id.register_btn_register)
        enterButton.setOnClickListener {
            usernameField = findViewById<TextView>(R.id.register_et_username)
            if(usernameField.length() == 0){
                usernameField.error = "Пустое поле!"
                return@setOnClickListener
            }

            emailField = findViewById<TextView>(R.id.register_et_email)
            if(emailField.length() == 0){
                emailField.error = "Пустое поле!"
                return@setOnClickListener
            }

            passwordField = findViewById<TextView>(R.id.register_et_password)
            if(passwordField.length() == 0){
                passwordField.error = "Пустое поле!"
                return@setOnClickListener
            }

            registerUser()
        }

        val profilePicture = findViewById<ImageView>(R.id.register_iv)
        selectImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                result ->
            if(result.resultCode == Activity.RESULT_OK){
                val selectedImageUri: Uri? = result.data?.data

                profilePicture.setImageURI(selectedImageUri)
                imageURI = selectedImageUri
            }
        }

        val addPicture = findViewById<Button>(R.id.register_btn_picture)
        addPicture.setOnClickListener {
            Utils.ImageUtils.openGallery(selectImageLauncher)
        }
    }

    private fun registerUser() {
        var isEmailRegistered = false
        lifecycleScope.launch {
            with(Dispatchers.IO){
                isEmailRegistered = checkUserEmail(emailField.text.toString())
                if(isEmailRegistered == true){
                    Toast.makeText(this@RegistrationActivity, "Данная почта уже занята!", Toast.LENGTH_LONG).show()
                }
                else{
                    showDialog()
                }
            }
        }
    }

    private suspend fun checkUserEmail(email: String): Boolean{
        return withContext(Dispatchers.IO){
            UserRepository().checkEmail(email)
        }
    }

    private suspend fun showDialog(){
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_age, null)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Сколько вам лет?")
            .setView(dialogView)
            .setPositiveButton("Сохранить", null)
            .setNegativeButton("Отменить", null)
            .create()

        dialog.setOnShowListener {
            val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                val ageField = dialogView.findViewById<EditText>(R.id.dialog_age_et)
                val age = ageField.text.toString()
                val ageValue = age.toInt()
                if(age == ""){
                    ageField.error = "Введите возраст!"
                    return@setOnClickListener
                }
                else if(ageValue < 6){
                    ageField.error = "Вам должно быть больше 6 лет!"
                    return@setOnClickListener
                }
                else if(ageValue > 120){
                    ageField.error = "Вам должго быть не больше 120 лет!"
                    return@setOnClickListener
                }

                val newUser = UserCreate(
                    name = usernameField.text.toString(),
                    email = emailField.text.toString(),
                    password = passwordField.text.toString(),
                    age = ageValue
                )

                CoroutineScope(Dispatchers.IO).launch {
                    var imagePart: MultipartBody.Part? = null
                    if(imageURI != null){
                        imagePart = imageURI?.let { uri ->
                            Utils.ImageUtils.createImagePart(this@RegistrationActivity, uri)
                        }
                    }

                    val result = userRepository.registerUser(newUser, imagePart)

                    withContext(Dispatchers.Main) {
                        result.onSuccess {user ->
                            val manager = PreferencesManager(this@RegistrationActivity)
                            manager.saveAuthToken(user.accessToken)
                            manager.saveUserId(user.userID)

                            val intent = Intent(this@RegistrationActivity, MainMenuActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()

                        }.onFailure { error ->
                            Toast.makeText(
                                this@RegistrationActivity,
                                "Ошибка при регистрации!",
                                Toast.LENGTH_LONG
                            ).show()

                        }
                    }
                }
            }
        }

        dialog.show()
    }
}