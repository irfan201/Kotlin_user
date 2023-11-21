package com.example.kotlin_user.auth


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.kotlin_user.MainActivity
import com.example.kotlin_user.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    val db = Firebase.firestore
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.tvRegister.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    RegisterActivity::class.java
                )
            )
        }

        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
        }

        binding.tvForgotPassword.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    ForgotPasswordActivity::class.java
                )
            )
        }
        binding.btnLogin.setOnClickListener {
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
            loginUser(email, password)

        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        "Authentication failed. Check your email and password.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}