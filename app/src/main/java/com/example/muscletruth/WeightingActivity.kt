package com.example.muscletruth

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.muscletruth.data.api.models.Weighting
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class WeightingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_weighting)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val weighting = intent.getParcelableExtra<Weighting.WeightingBase>("weighting")

        val etResult = findViewById<EditText>(R.id.weighting_et_result)
        etResult.setText(weighting?.result.toString())

        val image = findViewById<ImageView>(R.id.weighting_iv)
        image.setImageURI(weighting?.picture?.toUri())

        val tvDate = findViewById<TextView>(R.id.weighting_tv_date)
        tvDate.text = ZonedDateTime.parse(weighting?.creationDate).format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        )
    }
}