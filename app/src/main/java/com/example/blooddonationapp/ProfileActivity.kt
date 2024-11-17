package com.example.blooddonationapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileActivity : AppCompatActivity() {
    private lateinit var userRepository: UserRepository
    private lateinit var dbHelper: DatabaseHelper
    private var currentUser: User? = null
    private lateinit var profileImage: ImageView
    private lateinit var profileName: TextView
    private lateinit var logoutButton: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        dbHelper = DatabaseHelper(this)
        userRepository = UserRepository(dbHelper)

        profileImage = findViewById(R.id.profileimg)
        profileName = findViewById(R.id.profilename)
        logoutButton = findViewById(R.id.Logout)

        val email = intent.getStringExtra("EMAIL")
        val password = intent.getStringExtra("PASSWORD")

        if (email != null && password != null) {
            loadUserData(email, password)
        }

        logoutButton.setOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun loadUserData(email: String, password: String) {
        lifecycleScope.launch {
            try {
                currentUser = withContext(Dispatchers.IO) {
                    userRepository.getUserByEmailAndPassword(email, password)
                }

                if (currentUser != null) {
                    profileImage.setImageBitmap(currentUser?.image)
                    profileName.text = currentUser?.name
                } else {
                    profileName.text = "User not found"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                profileName.text = "Error loading user data"
            }
        }
    }
}
