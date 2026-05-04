package com.example.muscletruth.ui.User

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.muscletruth.R
import com.example.muscletruth.data.models.User
import com.example.muscletruth.data.repository.UserRepository
import com.example.muscletruth.data.repository.WeightingRepository
import com.example.muscletruth.ui.EnterActivity
import com.example.muscletruth.ui.StatisticActivity
import com.example.muscletruth.utils.PreferencesManager
import com.example.muscletruth.utils.Utils
import com.example.muscletruth.utils.Utils.NetworkUtils.checkForInternetConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyProfileActivity : AppCompatActivity() {
    lateinit var userName: TextView
    lateinit var email: TextView
    lateinit var age: TextView
    lateinit var weight: TextView
    lateinit var picture: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_my_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        userName = findViewById<TextView>(R.id.profile_tv_username)
        email = findViewById<TextView>(R.id.profile_tv_email)
        age = findViewById<TextView>(R.id.profile_tv_age)
        weight = findViewById<TextView>(R.id.profile_tv_weight)
        picture = findViewById<ImageView>(R.id.profile_iv_picture)

        updateUserData()

        val statisticButton = findViewById<Button>(R.id.profile_btn_statistics)
        statisticButton.setOnClickListener {
            val intent = Intent(this@MyProfileActivity, StatisticActivity::class.java)
            startActivity(intent)
        }

        val changeProfileButton = findViewById<Button>(R.id.profile_btn_change)
        changeProfileButton.setOnClickListener {
            if(checkForInternetConnection()){
                val intent = Intent(this@MyProfileActivity, ChangeProfileActivity::class.java)
                startActivity(intent)
            }
            else{
                Toast.makeText(
                    this@MyProfileActivity,
                    "В оффлайн режиме изменение профиля недоступно!",
                    Toast.LENGTH_LONG
                )
                .show()
            }
        }

        val logoutButton = findViewById<Button>(R.id.profile_btn_logout)
        logoutButton.setOnClickListener {
            val manager = PreferencesManager(this)
            manager.clearAuthToken()
            manager.clearUserId()

            val intent = Intent(this@MyProfileActivity, EnterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        updateUserData()
    }

    fun updateUserData(){
        lifecycleScope.launch {
            var user: User? = null
            var lastWeightingResult: Double? = null

            withContext(Dispatchers.IO){
                user = UserRepository.getUser(this@MyProfileActivity)
                lastWeightingResult = WeightingRepository.getLastWeighting()?.result
            }

            if(user !== null){
                withContext(Dispatchers.Main){
                    userName.text = user.name
                    email.text = user.email
                    age.text = user.age.toString()
                    weight.text = if(lastWeightingResult != null) {
                        "$lastWeightingResult кг"
                    } else{
                        "0.00 кг"
                    }
                    if(user.serverPicture != null){
                        val path = user.serverPicture
                        Glide.with(this@MyProfileActivity)
                            .load(Utils.ImageUtils.getImagePath(path!!))
                            .placeholder(R.drawable.ic_launcher_foreground)
                            .into(picture)
                    }
                }
            }
            else{
                Toast.makeText(this@MyProfileActivity, "Ошибка!", Toast.LENGTH_LONG).show()
                Log.e("APP_DEBUG", "UPDATE USER INFO ERROR: USER IS NULL")
            }
        }
    }
}