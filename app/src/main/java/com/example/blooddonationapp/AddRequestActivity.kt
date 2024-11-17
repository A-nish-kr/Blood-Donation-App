package com.example.blooddonationapp

import LocationHelper
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddRequestActivity : AppCompatActivity() {

    private lateinit var descriptionInput: EditText
    private lateinit var bloodTypeSpinner: Spinner
    private lateinit var locationInput: EditText
    private lateinit var contactNumberInput: EditText
    private lateinit var requesterNameInput: EditText
    private lateinit var submitRequestButton: Button
    private lateinit var useCurrentLocationButton: Button

    private lateinit var requestRepository: RequestRepository
    private lateinit var userRepository: UserRepository
    private var currentUser: User? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_request)

        val email = intent.getStringExtra("EMAIL")
        val password = intent.getStringExtra("PASSWORD")

        descriptionInput = findViewById(R.id.requestDescriptionInput)
        bloodTypeSpinner = findViewById(R.id.bloodTypeSpinner)
        locationInput = findViewById(R.id.locationInput)
        contactNumberInput = findViewById(R.id.contactNumberInput)
        requesterNameInput = findViewById(R.id.requesterNameInput)
        submitRequestButton = findViewById(R.id.submitRequestButton)
        useCurrentLocationButton = findViewById(R.id.useCurrentLocationButton)

        requestRepository = RequestRepository(DatabaseHelper(this))
        userRepository = UserRepository(DatabaseHelper(this))

        if (!email.isNullOrEmpty() && !password.isNullOrEmpty()) {
            lifecycleScope.launch {
                currentUser = withContext(Dispatchers.IO) {
                    userRepository.getUserByEmailAndPassword(email, password)
                }
            }
        }

        val bloodTypes = arrayOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, bloodTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        bloodTypeSpinner.adapter = adapter

        useCurrentLocationButton.setOnClickListener {
            val locationHelper = LocationHelper(this)
            locationHelper.getCurrentLocation { address ->
                locationInput.setText(address)
            }
        }

        submitRequestButton.setOnClickListener {
            lifecycleScope.launch {
                submitRequest()
            }
        }
    }

    private suspend fun submitRequest() {
        val description = descriptionInput.text.toString().trim()
        val bloodType = bloodTypeSpinner.selectedItem.toString()
        val location = locationInput.text.toString().trim()
        val contactNumber = contactNumberInput.text.toString().trim()
        val requesterName = requesterNameInput.text.toString().trim()

        if (description.isEmpty() || location.isEmpty() || contactNumber.isEmpty() || requesterName.isEmpty()) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@AddRequestActivity, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            }
            return
        }

        val userImage = currentUser?.image ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888) // Placeholder if no image

        val requestId = withContext(Dispatchers.IO) {
            requestRepository.insertRequest(
                requesterId = currentUser?.id ?: 1,
                requesterName = requesterName,
                donorId = null,
                bloodType = bloodType,
                location = location,
                message = description,
                image = userImage,
                mobile = contactNumber
            )
        }

        withContext(Dispatchers.Main) {
            if (requestId > 0) {
                Toast.makeText(this@AddRequestActivity, "Request submitted successfully", Toast.LENGTH_SHORT).show()

                val resultIntent = Intent()
                resultIntent.putExtra("REFRESH_REQUESTS", true)
                setResult(RESULT_OK, resultIntent)
                finish()
            } else {
                Toast.makeText(this@AddRequestActivity, "Failed to submit request", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
