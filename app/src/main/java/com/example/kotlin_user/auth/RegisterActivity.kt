package com.example.kotlin_user.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.kotlin_user.MainActivity
import com.example.kotlin_user.R
import com.example.kotlin_user.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        val db = Firebase.firestore

        binding.btnRegister.setOnClickListener {

            val name = binding.editTextName.text.toString()
            if (name.isEmpty() || name.length < 3 || name.length > 50) {

                binding.tilName.error = "Name must be between 3 and 50 characters"
                return@setOnClickListener
            } else {
                binding.tilName.error = null
            }


            val email = binding.editTextEmail.text.toString()
            val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
            if (email.isEmpty() || !email.matches(emailPattern.toRegex())) {

                binding.tilEmail.error = "Enter a valid email address"
                return@setOnClickListener
            } else {
                binding.tilEmail.error = null
            }


            val password = binding.editTextPassword.text.toString()
            val passwordPattern = "^(?=.*?[a-z])(?=.*?[A-Z])(?=.*?[0-9]).{8,}\$"
            if (password.isEmpty() || !password.matches(passwordPattern.toRegex())) {

                binding.tilPassword.error =
                    "Password must be at least 8 characters and contain at least one uppercase letter, one lowercase letter, and one digit"
                return@setOnClickListener
            } else {
                binding.tilPassword.error = null
            }


            val confirmPassword = binding.editTextConfirmPassword.text.toString()
            if (confirmPassword.isEmpty() || confirmPassword != password) {

                binding.tilConfirmPassword.error = "Passwords do not match"
                return@setOnClickListener
            } else {
                binding.tilConfirmPassword.error = null
            }


            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val currentUser = auth.currentUser
                        currentUser?.sendEmailVerification()
                            ?.addOnCompleteListener { verificationTask ->
                                if (verificationTask.isSuccessful) {
                                    Toast.makeText(
                                        this@RegisterActivity,
                                        "Email verification sent to ${currentUser.email}",
                                        Toast.LENGTH_SHORT
                                    ).show()


                                    val user = hashMapOf(
                                        "name" to name,
                                        "email" to email,
                                        "password" to password,
                                        "isEmailConfirmed" to false
                                    )

                                    db.collection("users").document(currentUser.uid)
                                        .set(user)
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                this@RegisterActivity,
                                                "You have signed up successfully!",
                                                Toast.LENGTH_SHORT
                                            ).show()


                                            val intent = Intent(
                                                this@RegisterActivity,
                                                MainActivity::class.java
                                            )
                                            startActivity(intent)
                                        }
                                        .addOnFailureListener { exception ->
                                            Toast.makeText(
                                                this@RegisterActivity,
                                                "Failed to save user data to Firestore. ${exception.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                } else {
                                    Toast.makeText(
                                        this@RegisterActivity,
                                        "Failed to send verification email. ${verificationTask.exception?.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    } else {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Registration failed. ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

        }
    }
}