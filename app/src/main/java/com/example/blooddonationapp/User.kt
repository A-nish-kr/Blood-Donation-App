package com.example.blooddonationapp

import android.graphics.Bitmap

class User(
    val id: Long,
    val name: String,
    val email: String,
    val password: String,
    val bloodType: String,
    val location: String,
    val phone: String,
    val image: Bitmap
)
