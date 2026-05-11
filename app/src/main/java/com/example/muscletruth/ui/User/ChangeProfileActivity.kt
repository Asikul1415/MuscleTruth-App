package com.example.muscletruth.ui.User

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import com.bumptech.glide.Glide
import com.example.muscletruth.R
import com.example.muscletruth.data.models.User
import com.example.muscletruth.data.models.UserUpdate
import com.example.muscletruth.data.repository.UserRepository
import com.example.muscletruth.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import java.io.File

class ChangeProfileActivity : AppCompatActivity() {
    lateinit var userName: EditText
    lateinit var email: EditText
    lateinit var passwordText: EditText
    lateinit var passwordConfirmText: EditText
    lateinit var profilePicture: ImageView
    lateinit var ageText: TextView
    private lateinit var selectImageLauncher: ActivityResultLauncher<Intent>
    var imageURI: Uri? = null

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
        loadUserPicture()

        selectImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                result ->
            if(result.resultCode == RESULT_OK){
                val selectedImageUri: Uri? = result.data?.data

                profilePicture.setImageURI(selectedImageUri)
                imageURI = selectedImageUri
            }
        }

        val addPicture = findViewById<Button>(R.id.change_profile_btn_picture)
        addPicture.setOnClickListener {
            Utils.ImageUtils.openGallery(selectImageLauncher)
        }

        val backButton = findViewById<Button>(R.id.change_profile_btn_back)
        backButton.setOnClickListener {
            finish()
        }

        val saveButton = findViewById<Button>(R.id.change_profile_btn_save)
        saveButton.setOnClickListener {
            if(userName.length() > 0 && userName.length() < 3){
                userName.error = "Имя пользователя должно состоять минимум из 3 символов!"
                return@setOnClickListener
            }
            else if(email.length() > 0 && email.length() < 6){
                email.error = "Введите корректный email!"
                return@setOnClickListener
            }
            else if(passwordText.length() > 0 && passwordText.length() < 8){
                passwordText.error = "Пароль должен состоять минимум из 8 символов!"
                return@setOnClickListener
            }
            else if(passwordConfirmText.length() > 0 && passwordConfirmText.length() < 8){
                passwordConfirmText.error = "Пароль должен состоять минимум из 8 символов!"
                return@setOnClickListener
            }
            else if((passwordConfirmText.length() > 0 && passwordText.length() > 0) && passwordText.text.toString() != passwordConfirmText.text.toString()){
                passwordText.error = "Пароли не совпадают!"
                passwordConfirmText.error = "Пароли не совпадают!"
                return@setOnClickListener
            }
            else if(ageText.text.toString().isNotEmpty() && ageText.text.toString().toInt() < 6){
                ageText.error = "Вам должно быть не меньше 6!"
                return@setOnClickListener
            }
            else if(ageText.text.toString().isNotEmpty() && ageText.text.toString().toInt() > 130){
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
                            Log.d("APP_DEBUG", "PROBLEM $password")
                            val isPasswordCorrect = checkUserPassword(password)

                            if(isPasswordCorrect){
                                if(email.text.isNotEmpty()){
                                    var isEmailRegistered = checkUserEmail(email.text.toString())
                                    if(isEmailRegistered === false){
                                        saveUserData()
                                    }
                                    else{
                                        Toast.makeText(dialog.context, "Почта уже занята!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                else{
                                    saveUserData()
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

    private fun loadUserPicture(){
        lifecycleScope.launch {
            with(Dispatchers.IO){
                val user = UserRepository.getUser(this@ChangeProfileActivity)
                if(user !== null){
                    if(user.serverPicture !== null){
                        val path = user.serverPicture
                        Glide.with(this@ChangeProfileActivity)
                            .load(Utils.ImageUtils.getImagePath(path!!))
                            .placeholder(R.drawable.ic_launcher_foreground)
                            .override(250, 300)
                            .into(profilePicture)
                    }
                    else if(user.localPicture !== null){
                        val uri = Uri.fromFile(File(user.localPicture))
                        Glide.with(this@ChangeProfileActivity)
                            .load(uri)
                            .placeholder(R.drawable.ic_launcher_foreground)
                            .override(250, 300)
                            .into(profilePicture)
                    }
                }
            }
        }
    }

    private suspend fun checkUserPassword(password: String): Boolean{
        return withContext(Dispatchers.IO){
            UserRepository.checkUserPassword(password)
        }
    }

    private suspend fun checkUserEmail(email: String): Boolean{
        return with(Dispatchers.IO){
            var isEmailValid = true;

            if(UserRepository.checkIfEmailRegistered(email) === true){
                val user = UserRepository.getUser(this@ChangeProfileActivity)
                if(user !== null) {
                    //If email is not users, email is registered for someone else
                    isEmailValid = user.email != email
                }
            }
            isEmailValid
        }
    }

    private fun saveUserData(){
        var imagePart: MultipartBody.Part? = null
        if(imageURI != null){
            imagePart = imageURI?.let { uri ->
                Utils.ImageUtils.createImagePart(this, uri)
            }
        }
        val user = UserUpdate(
            name = userName.text.toString().takeIf{it -> it.isNotEmpty()},
            email = email.text.toString().takeIf{it -> it.isNotEmpty()},
            password = passwordText.text.toString().takeIf{it -> it.isNotEmpty()},
            age = ageText.text.toString().takeIf{it -> it.isNotEmpty()}?.toInt(),
        )

        lifecycleScope.launch {
            with(Dispatchers.IO){
                val isUserUpdateSuccessful = UserRepository.updateUser(user, imagePart)
                if(isUserUpdateSuccessful) {
                    Toast.makeText(this@ChangeProfileActivity, "Успешное сохранение", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this@ChangeProfileActivity, "Ошибка сохранения", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}