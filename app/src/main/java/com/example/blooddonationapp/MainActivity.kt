package com.example.blooddonationapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var requestRecyclerView: RecyclerView
    private lateinit var requestAdapter: RequestAdapter
    private lateinit var requestRepository: RequestRepository
    private lateinit var profileImage: ImageView
    private var requestList: MutableList<Request> = mutableListOf()
    private lateinit var userRepository: UserRepository
    private var currentUser: User? = null

    private val ADD_REQUEST_REQUEST_CODE = 1001

    private var email: String? = null
    private var password: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        email = intent.getStringExtra("EMAIL")
        password = intent.getStringExtra("PASSWORD")

        val dbHelper = DatabaseHelper(this)
        userRepository = UserRepository(dbHelper)
        requestRepository = RequestRepository(dbHelper)

        if (email != null && password != null) {
            currentUser = userRepository.getUserByEmailAndPassword(email!!, password!!)
        }

        if (currentUser == null) {
            Toast.makeText(this, "User not found or invalid credentials", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        profileImage = findViewById(R.id.profileImage)
        profileImage.setImageBitmap(currentUser?.image)

        requestRecyclerView = findViewById(R.id.requestsRecyclerView)
        requestRecyclerView.layoutManager = LinearLayoutManager(this)

        loadRequests()

        val searchView = findViewById<SearchView>(R.id.searchView)
        setupSearch(searchView)

        profileImage.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("EMAIL", email)
            intent.putExtra("PASSWORD", password)
            startActivity(intent)
        }

        findViewById<FloatingActionButton>(R.id.floatingActionButton).setOnClickListener {
            val intent = Intent(this, AddRequestActivity::class.java)
            intent.putExtra("EMAIL", email)
            intent.putExtra("PASSWORD", password)
            startActivityForResult(intent, ADD_REQUEST_REQUEST_CODE)
        }
    }

    private fun loadRequests() {
        lifecycleScope.launch {
            try {
                val allRequests = withContext(Dispatchers.IO) {
                    requestRepository.getAllRequests()
                }
                if (allRequests != null) {
                    requestList.clear()
                    requestList.addAll(allRequests)
                } else {
                    Toast.makeText(this@MainActivity, "Failed to load requests", Toast.LENGTH_SHORT).show()
                }

                requestAdapter = RequestAdapter(requestList) { request ->
                    val intent = Intent(this@MainActivity, ReadMoreActivity::class.java)
                    intent.putExtra("request_id", request.id)
                    intent.putExtra("EMAIL", email)
                    intent.putExtra("PASSWORD", password)
                    startActivityForResult(intent, ADD_REQUEST_REQUEST_CODE)
                }
                requestRecyclerView.adapter = requestAdapter

            } catch (e: Exception) {
                Log.e("MainActivity", "Error loading requests: ${e.message}")
                Toast.makeText(this@MainActivity, "Error loading requests", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSearch(searchView: SearchView) {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterRequests(newText)
                return true
            }
        })
    }

    private fun filterRequests(query: String?) {
        val filteredList = if (query.isNullOrEmpty()) {
            requestList
        } else {
            requestList.filter { it.requesterName?.contains(query, ignoreCase = true) == true }
        }

        requestAdapter = RequestAdapter(filteredList.toMutableList()) { request ->
            val intent = Intent(this, ReadMoreActivity::class.java)
            intent.putExtra("request_id", request.id) // Pass the request ID
            startActivity(intent)
        }
        requestRecyclerView.adapter = requestAdapter
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ADD_REQUEST_REQUEST_CODE && resultCode == RESULT_OK) {
            val shouldRefresh = data?.getBooleanExtra("REFRESH_REQUESTS", false) ?: false
            if (shouldRefresh) {
                refreshRecyclerView()
            }
        }
    }

    private fun refreshRecyclerView() {
        loadRequests()
        requestAdapter.notifyDataSetChanged()
    }
}
