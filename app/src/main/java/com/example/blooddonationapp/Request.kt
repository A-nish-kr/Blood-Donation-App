package com.example.blooddonationapp

import android.graphics.Bitmap

data class Request(
    val id: Long,
    val requesterId: Long,
    val requesterName: String?,
    val donorId: Long?,
    val bloodType: String,
    val location: String,
    val message: String,
    val createdAt: String,
    val mobile : String,
    val status: String,
    val imageUrl: Bitmap?
)
