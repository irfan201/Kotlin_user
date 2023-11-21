package com.example.kotlin_user.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.kotlin_user.R
import com.example.kotlin_user.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnReset.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
            if (email.isEmpty() || !email.matches(emailPattern.toRegex())) {

                binding.tilEmail.error = "Enter a valid email address"
                return@setOnClickListener
            } else {
                binding.tilEmail.error = null
            }
            val newPassword = binding.editTextNewPassword.text.toString()
            val newPasswordPattern = "^(?=.*?[a-z])(?=.*?[A-Z])(?=.*?[0-9]).{8,}\$"
            if (newPassword.isEmpty() || !newPassword.matches(newPasswordPattern.toRegex())) {
                // Handle validation error for Password
                binding.tilNewPassword.error =
                    "Password must be at least 8 characters and contain at least one uppercase letter, one lowercase letter, and one digit"
                return@setOnClickListener
            } else {
                binding.tilNewPassword.error = null
            }
            val confirmPassword = binding.editTextConfirmPassword.text.toString()
            if (confirmPassword.isEmpty() || confirmPassword != newPassword) {

                binding.tilConfirmPassword.error = "Passwords do not match"
                return@setOnClickListener
            } else {
                binding.tilConfirmPassword.error = null
            }

            resetPasswordByEmail(email, newPassword)
        }
    }

    private fun resetPasswordByEmail(email: String, newPassword: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    updatePasswordInFirestore(email, newPassword)
                    Toast.makeText(
                        this@ForgotPasswordActivity,
                        "Password reset email sent to $email",
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(
                        this@ForgotPasswordActivity,
                        "Failed to send password reset email. ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun updatePasswordInFirestore(email: String, newPassword: String) {
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.update("password", newPassword)
                        .addOnFailureListener { exception ->
                            Toast.makeText(
                                this@ForgotPasswordActivity,
                                "Failed to update password in Firestore. ${exception.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this@ForgotPasswordActivity,
                    "Failed to query Firestore. ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}