package com.example.muscletruth

import android.app.Activity
import android.content.Intent
import android.media.Image
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
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.muscletruth.data.api.models.User
import com.example.muscletruth.data.repository.UserRepository
import com.example.muscletruth.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody

class ChangeProfileActivity : AppCompatActivity() {
    lateinit var userName: EditText
    lateinit var email: EditText
    lateinit var passwordText: EditText
    lateinit var passwordConfirmText: EditText
    lateinit var profilePicture: ImageView
    lateinit var ageText: TextView
    private lateinit var selectImageLauncher: ActivityResultLauncher<Intent>
    var imageURI: Uri? = null
    var userRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_change_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        userName = findViewById<EditText>(R.id.change_profiile_et_username)
        email = findViewById<EditText>(R.id.change_profile_et_email)
        passwordText = findViewById<EditText>(R.id.change_profile_et_password)
        passwordConfirmText = findViewById<EditText>(R.id.change_profile_et_confirm_password)
        profilePicture = findViewById<ImageView>(R.id.change_profile_iv)
        ageText = findViewById<EditText>(R.id.change_profile_et_age)
        loadUserData()

        selectImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                result ->
            if(result.resultCode == Activity.RESULT_OK){
                val selectedImageUri: Uri? = result.data?.data

                profilePicture.setImageURI(selectedImageUri)
                imageURI = selectedImageUri
            }
        }

        val addPicture = findViewById<Button>(R.id.change_profile_btn_picture)
        addPicture.setOnClickListener {
            Utils.ImageUtils.openGallery(selectImageLauncher)
        }

        val saveButton = findViewById<Button>(R.id.change_profile_btn_save)
        saveButton.setOnClickListener {
            if(userName.length() == 0){
                userName.error = "Введите имя!"
                return@setOnClickListener
            }
            else if(email.length() <= 5){
                email.error = "Введите корректный email!"
                return@setOnClickListener
            }
            else if(passwordText.length() < 3){
                passwordText.error = "Введите пароль!"
                return@setOnClickListener
            }
            else if(passwordConfirmText.length() < 3){
                passwordConfirmText.error = "Введите пароль!"
                return@setOnClickListener
            }
            else if(passwordText.text.toString() != passwordConfirmText.text.toString()){
                passwordText.error = "Пароли не совпадают!"
                passwordConfirmText.error = "Пароли не совпадают!"
                return@setOnClickListener
            }
            else if(ageText.length() == 0){
                ageText.error = "Введите возраст!"
                return@setOnClickListener
            }
            else if(ageText.text.toString().toInt() < 6){
                ageText.error = "Вам должно быть не меньше 6!"
                return@setOnClickListener
            }
            else if(ageText.text.toString().toInt() > 130){
                ageText.error = "Вам должно быть не больше 130!"
                return@setOnClickListener
            }

            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirm_password, null)

            val dialog = AlertDialog.Builder(this)
                .setTitle("Подтвердите пароль")
                .setView(dialogView)
                .setPositiveButton("Сохранить", null)
                .setNegativeButton("Отменить", null)
                .create()

            dialog.setOnShowListener {
                val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                button.setOnClickListener {
                    val passwordField = dialogView.findViewById<EditText>(R.id.dialog_confirm_password_et_password)
                    val passwordConfirmField = dialogView.findViewById<EditText>(R.id.dialog_confirm_password_et_password_confirm)
                    val password = passwordField.text.toString()
                    val passwordConfirm = passwordConfirmField.text.toString()

                    if (password == passwordConfirm) {
                        lifecycleScope.launch {
                            val isPasswordCorrect = checkUserPassword(password)

                            if(isPasswordCorrect){
                                var isEmailRegistered = checkUserEmail(email.text.toString())
                                if(isEmailRegistered == false){
                                    saveUserData()
                                }
                                else{
                                    Toast.makeText(dialog.context, "Почта уже занята!", Toast.LENGTH_SHORT).show()
                                }
                            }
                            else{
                                passwordField.error = "Некорректный пароль!"
                                passwordConfirmField.error = "Некорректный пароль!"
                            }
                        }
                    }
                    else {
                        passwordField.error = "Пароли не совпадают!"
                        passwordConfirmField.error = "Пароли не совпадают!"
                        passwordText.text.clear()
                        passwordConfirmText.text.clear()
                        passwordText.requestFocus()
                    }
                }
            }

            dialog.show()
        }
    }

    private fun loadUserData(){
        lifecycleScope.launch {
            with(Dispatchers.IO){
                val response = userRepository.getUser()
                response.onSuccess { user ->
                    userName.setText(user.name)
                    email.setText(user.email)
                    passwordText.setText(user.password)
                    passwordConfirmText.setText(user.password)
                    ageText.setText(user.age.toString())
                    if(user.profilePicture != null){
                        Glide.with(this@ChangeProfileActivity)
                            .load(Utils.ImageUtils.getImagePath(user.profilePicture))
                            .placeholder(R.drawable.ic_launcher_foreground)
                            .into(profilePicture)
                    }
                }
            }
        }
    }

    private suspend fun checkUserPassword(password: String): Boolean{
        return withContext(Dispatchers.IO){
            userRepository.checkUserPassword(password).response
        }
    }

    private suspend fun checkUserEmail(email: String): Boolean{
        return with(Dispatchers.IO){
            var checkEmail = userRepository.checkEmail(email)
            if(checkEmail == true){
                userRepository.getUser().onSuccess { user ->
                    //If email is not users, email is registered for someone else
                    checkEmail = user.email != email
                }
            }
            checkEmail
        }
    }

    private fun saveUserData(){
        var imagePart: MultipartBody.Part? = null
        if(imageURI != null){
            imagePart = imageURI?.let { uri ->
                Utils.ImageUtils.createImagePart(this, uri)
            }
        }

        val user = User.UserCreate(
            name = userName.text.toString(),
            email = email.text.toString(),
            password = passwordText.text.toString(),
            age = ageText.text.toString().toInt(),
        )

        lifecycleScope.launch {
            with(Dispatchers.IO){
                val response = UserRepository().updateUser(user, imagePart)
                response.onSuccess {
                    Toast.makeText(this@ChangeProfileActivity, "Успешное сохранение", Toast.LENGTH_LONG).show()
                    finish()
                }.onFailure {
                    Toast.makeText(this@ChangeProfileActivity, "Ошибка сохранения", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}