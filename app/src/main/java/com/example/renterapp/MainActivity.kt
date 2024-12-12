package com.example.renterapp


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.renterapp.databinding.ActivityMainBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var mMap: GoogleMap
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        // Set up the map
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        // Set up filter button

        binding.filterButton.setOnClickListener {
            val maxPrice = binding.priceFilterInput.text.toString().toIntOrNull()
            if (maxPrice == null || maxPrice <= 0) {
                loadPropertiesFromFirestore() // Reset map
            } else {
                filterPropertiesByPrice(maxPrice)
            }
        }

    }



    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true

        // Center map on default property
        val defaultPropertyLocation = LatLng(43.7923809, -79.4427984)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultPropertyLocation, 12f))

        // Load properties from Firestore
        loadPropertiesFromFirestore()

        // Set marker click listener
        mMap.setOnMarkerClickListener { marker ->
            val property = marker.tag as? Property
            if (property != null) {
                displayPropertyDetails(property)
            }
            true
        }

        // Set map click listener
        mMap.setOnMapClickListener {
            hidePropertyDetails()
        }
    }
    // Function to hide property details
    private fun hidePropertyDetails() {
        binding.propertyDetailsContainer.visibility = View.GONE
    }

    private fun displayPropertyDetails(property: Property) {
        // Set property details
        with(binding) {
            propertyDetailsContainer.visibility = View.VISIBLE
            propertyAddress.text = property.address
            propertyDetails.text = "Price: $${property.monthlyRentalPrice}\nBedrooms: ${property.numberOfBedrooms}\nAvailable: ${if (property.isAvailable) "Yes" else "No"}"

            // Load image using Glide
            Glide.with(this@MainActivity)
                .load(property.imageUrl)
                .into(propertyImage)

            // Handle Add to Watchlist button click
            addToWatchlistButton.setOnClickListener {
                addToWatchlist(property)
            }
        }
    }

    private fun filterPropertiesByPrice(maxPrice: Int) {
        db.collection("listings")
            .whereLessThanOrEqualTo("monthlyRentalPrice", maxPrice)
            .get()
            .addOnSuccessListener { result ->
                mMap.clear() // Clear existing markers
                for (document in result) {
                    val property = document.toObject(Property::class.java)
                    val location = LatLng(property.lat, property.lon)

                    // Add marker for property
                    mMap.addMarker(
                        MarkerOptions()
                            .position(location)
                            .title(property.address)
                            .snippet("Price: $${property.monthlyRentalPrice}, Bedrooms: ${property.numberOfBedrooms}")
                    )?.tag = property
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error filtering properties", e)
                Toast.makeText(this, "Failed to filter properties", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addToWatchlist(property: Property) {

        val currentUser = auth.currentUser

        if (currentUser == null) {
            // User is not logged in
            Snackbar.make(binding.root, "Please log in to add properties to your watchlist.", Snackbar.LENGTH_LONG)
                .setAction("Login") {
                    val loginIntent = Intent(this, LoginActivity::class.java)
                    startActivity(loginIntent)
                }
                .show()
            return
        }
        db.collection("watchlist")
            .add(
                hashMapOf(
                    "userId" to auth.currentUser?.uid,
                    "address" to property.address,
                    "imageUrl" to property.imageUrl,
                    "monthlyRentalPrice" to property.monthlyRentalPrice,
                    "numberOfBedrooms" to property.numberOfBedrooms
                )
            )
            .addOnSuccessListener {
                Snackbar.make(binding.root, "Property added to Watchlist", Snackbar.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error adding to watchlist", e)
                Toast.makeText(this, "Failed to add to watchlist", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadPropertiesFromFirestore() {
        db.collection("listings")
            .get()
            .addOnSuccessListener { result ->
                mMap.clear() // Clear existing markers
                for (document in result) {
                    val property = document.toObject(Property::class.java) // Explicitly specify the class type
                    val location = LatLng(property.lat, property.lon)

                    // Add marker for property
                    mMap.addMarker(
                        MarkerOptions()
                            .position(location)
                            .title(property.address)
                            .snippet("Price: $${property.monthlyRentalPrice}, Bedrooms: ${property.numberOfBedrooms}")
                    )?.tag = property // Attach property object to marker
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error loading properties", e)
                Toast.makeText(this, "Failed to load properties", Toast.LENGTH_SHORT).show()
            }
    }






    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_login -> {
                val loginIntent = Intent(this, LoginActivity::class.java)
                startActivity(loginIntent)
            }
            R.id.menu_logout -> {
                logoutCurrentUser()
            }
            R.id.menu_watchlist -> {
                if (auth.currentUser != null) {
                    val watchlistIntent = Intent(this, WatchListActivity::class.java)
                    startActivity(watchlistIntent)
                } else {
                    Snackbar.make(binding.root, "Please login to access your Watch List", Snackbar.LENGTH_LONG).show()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun logoutCurrentUser() {
        auth.signOut()
        Snackbar.make(binding.root, "Logged out successfully", Snackbar.LENGTH_LONG).show()
    }
}

data class Property(
    val address: String = "",
    val imageUrl: String = "",
    val isAvailable: Boolean = true,
    val landlord: String = "",
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    val monthlyRentalPrice: Int = 0,
    val numberOfBedrooms: Int = 0, // Ensure this is an Int, as per your structure
    @DocumentId var id: String = "" // Firestore document ID
)

