package com.example.blooddonationapp

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReadMoreActivity : AppCompatActivity() {

    private lateinit var requestTitleTextView: TextView
    private lateinit var requestDescriptionTextView: TextView
    private lateinit var bloodTypeTextView: TextView
    private lateinit var locationTextView: TextView
    private lateinit var contactNumberTextView: TextView
    private lateinit var requestImageView: ImageView
    private lateinit var DonateBtn: Button

    private lateinit var requestRepository: RequestRepository
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read_more)

        requestTitleTextView = findViewById(R.id.requestTitleTextView)
        requestDescriptionTextView = findViewById(R.id.requestDescriptionTextView)
        bloodTypeTextView = findViewById(R.id.bloodTypeTextView)
        locationTextView = findViewById(R.id.locationTextView)
        contactNumberTextView = findViewById(R.id.contactNumberTextView)
        requestImageView = findViewById(R.id.requestImageView)
        DonateBtn = findViewById(R.id.donateButton)

        val dbHelper = DatabaseHelper(this)
        userRepository = UserRepository(dbHelper)
        requestRepository = RequestRepository(dbHelper)

        val requestId = intent.getLongExtra("request_id", -1)
        val email = intent.getStringExtra("EMAIL")
        val password = intent.getStringExtra("PASSWORD")
        Log.e("email", "$email")
        Log.e("password", "$password")

        if (requestId != -1L) {
            loadRequestDetails(requestId)
        } else {
            Toast.makeText(this, "Invalid Request ID", Toast.LENGTH_SHORT).show()
        }

        DonateBtn.setOnClickListener {
            if (email != null && password != null) {
                handleDonation(requestId, email, password)
            } else {
                Toast.makeText(this, "Invalid user credentials", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadRequestDetails(requestId: Long) {
        lifecycleScope.launch {
            try {
                val request = withContext(Dispatchers.IO) {
                    requestRepository.getRequestById(requestId)
                }

                if (request != null) {
                    Log.e("Request Details", "Request: $request")
                    requestTitleTextView.text = request.requesterName
                    requestDescriptionTextView.text = request.message
                    bloodTypeTextView.text = "Blood Type: ${request.bloodType}"
                    locationTextView.text = "Location: ${request.location}"
                    contactNumberTextView.text = "Contact: ${request.mobile}"

                    if (request.imageUrl != null) {
                        requestImageView.setImageBitmap(request.imageUrl)
                    } else {
                        requestImageView.setImageResource(R.drawable.avatar)
                    }
                } else {
                    Toast.makeText(this@ReadMoreActivity, "Request not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e("ReadMoreActivity", "Error loading request details: ${e.message}")
                Toast.makeText(this@ReadMoreActivity, "Error loading request details", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleDonation(requestId: Long, email: String, password: String) {
        lifecycleScope.launch {
            try {
                val currentUser = withContext(Dispatchers.IO) {
                    userRepository.getUserByEmailAndPassword(email, password)
                }

                if (currentUser != null) {
                    val request = withContext(Dispatchers.IO) {
                        requestRepository.getRequestById(requestId)
                    }

                    if (request != null) {
                        if (currentUser.id == request.requesterId) {
                            Toast.makeText(this@ReadMoreActivity, "You cannot donate to your own request.", Toast.LENGTH_SHORT).show()
                            return@launch
                        }

                        val rowsUpdated = withContext(Dispatchers.IO) {
                            requestRepository.updateDonorIdAndStatus(requestId, currentUser.id)
                        }

                        if (rowsUpdated > 0) {
                            Toast.makeText(this@ReadMoreActivity, "Donation successful!", Toast.LENGTH_SHORT).show()
                            val resultIntent = Intent()
                            resultIntent.putExtra("REFRESH_REQUESTS", true)
                            setResult(RESULT_OK, resultIntent)
                            finish()
                        } else {
                            Toast.makeText(this@ReadMoreActivity, "Failed to mark as donated.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@ReadMoreActivity, "Request not found.", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    Toast.makeText(this@ReadMoreActivity, "User not found. Please login again.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e("ReadMoreActivity", "Error handling donation: ${e.message}")
                Toast.makeText(this@ReadMoreActivity, "Error handling donation", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
