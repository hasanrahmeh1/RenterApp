package com.example.renterapp

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.renterapp.databinding.ActivityWatchListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class WatchListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWatchListBinding
    private val db = FirebaseFirestore.getInstance()
    private val watchlistAdapter = WatchlistAdapter(mutableListOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWatchListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val currentUser = FirebaseAuth.getInstance().currentUser

        // Check if the user is logged in
        if (currentUser == null) {
            binding.watchlistRecyclerView.visibility = View.GONE

            return
        }

        // Set up RecyclerView
        binding.watchlistRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.watchlistRecyclerView.adapter = watchlistAdapter

        loadWatchlist()
    }

    private fun loadWatchlist() {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        db.collection("watchlist")
            .whereEqualTo("userId", currentUser.uid) // Use "userId" instead of "landlord"
            .get()
            .addOnSuccessListener { result ->
                val properties = result.map { it.toObject(Property::class.java) }
                watchlistAdapter.updateList(properties)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error loading watchlist", e)
                Toast.makeText(this, "Failed to load watchlist", Toast.LENGTH_SHORT).show()
            }

    }
}
