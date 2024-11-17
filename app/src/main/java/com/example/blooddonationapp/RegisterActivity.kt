package com.example.blooddonationapp

import LocationHelper
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.io.InputStream

class RegisterActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var bloodTypeSpinner: Spinner
    private lateinit var profileImageView: ImageView
    private lateinit var selectImageButton: Button
    private lateinit var registerButton: Button
    private lateinit var locationHelper: LocationHelper

    private var selectedImageUri: Uri? = null
    private var selectedBloodType: String = ""

    private val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        nameEditText = findViewById(R.id.nameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        phoneEditText = findViewById(R.id.phoneEditText)
        bloodTypeSpinner = findViewById(R.id.bloodTypeSpinner)
        profileImageView = findViewById(R.id.profileImageView)
        selectImageButton = findViewById(R.id.selectImageButton)
        registerButton = findViewById(R.id.registerButton)

        locationHelper = LocationHelper(this)

        setupBloodTypeSpinner()

        selectImageButton.setOnClickListener {
            pickImageFromGallery()
        }

        registerButton.setOnClickListener {
            requestLocationAndRegister()
        }
    }

    private fun setupBloodTypeSpinner() {
        val bloodTypes = arrayOf("Select Blood Type", "A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, bloodTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        bloodTypeSpinner.adapter = adapter
        bloodTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                selectedBloodType = parent.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 1000)
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationAndRegister() {
        if (!hasLocationPermission()) {
            ActivityCompat.requestPermissions(this, permissions, 1001)
        } else {
            lifecycleScope.launch {
                val address = withContext(Dispatchers.IO) {
                    locationHelper.getCurrentLocation { it }
                }
                if (address.toString().isEmpty()) {
                    Toast.makeText(this@RegisterActivity, "Failed to fetch location", Toast.LENGTH_SHORT).show()
                } else {
                    registerUser(address.toString())
                }
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun registerUser(address: String) {
        val name = nameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val phone = phoneEditText.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty() || selectedBloodType == "Select Blood Type") {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val imageBitmap = withContext(Dispatchers.IO) {
                selectedImageUri?.let { uri ->
                    try {
                        val inputStream: InputStream? = contentResolver.openInputStream(uri)
                        BitmapFactory.decodeStream(inputStream)
                    } catch (e: FileNotFoundException) {
                        null
                    }
                }
            }

            val isSuccess = withContext(Dispatchers.IO) {
                val userRepository = UserRepository(DatabaseHelper(this@RegisterActivity))
                userRepository.insertUser(
                    name = name,
                    email = email,
                    password = password,
                    bloodType = selectedBloodType,
                    location = address,
                    phone = phone,
                    image = imageBitmap ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                ) != -1L
            }

            if (isSuccess) {
                Toast.makeText(this@RegisterActivity, "User registered successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this@RegisterActivity, "Registration failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000 && resultCode == RESULT_OK) {
            selectedImageUri = data?.data
            profileImageView.setImageURI(selectedImageUri)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestLocationAndRegister()
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }
}
