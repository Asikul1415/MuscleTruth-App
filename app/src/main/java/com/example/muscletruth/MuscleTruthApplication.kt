package com.example.muscletruth

import android.app.Application
import com.example.muscletruth.data.repositories.UserRepository

class MuscleTruthApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        UserRepository.init(applicationContext)
    }
}