package com.example.renterapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.renterapp.databinding.ActivityLoginBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Set up the login button
        binding.btnLogin.setOnClickListener {
            val emailFromUI = binding.etEmail.text.toString()
            val passwordFromUI = binding.etPassword.text.toString()

            if (emailFromUI.isNotEmpty() && passwordFromUI.isNotEmpty()) {
                loginUser(emailFromUI, passwordFromUI)
            } else {
                Snackbar.make(
                    binding.root,
                    "Email and Password cannot be empty",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }


    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                // Show success alert dialog
                AlertDialog.Builder(this)
                    .setTitle("Login Successful")
                    .setMessage("Welcome back!")
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                        // Navigate back to MainActivity
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    .show()
            }
            .addOnFailureListener { error ->
                // Show failure alert dialog
                AlertDialog.Builder(this)
                    .setTitle("Login Failed")
                    .setMessage("Error: ${error.message}")
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()

                Log.e("LoginActivity", "Login error", error)
            }

    }
}

