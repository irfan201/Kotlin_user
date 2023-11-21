package com.example.kotlin_user

import UserAdapter
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kotlin_user.databinding.ActivityMainBinding
import com.example.kotlin_user.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.example.kotlin_user.auth.LoginActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: UserAdapter
    private lateinit var list: ArrayList<User>
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var confirmSpinner: Spinner
    private var isConfirmedFilter = true
    private var isNotConfirmedFilter = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.rvUser.layoutManager = LinearLayoutManager(this)
        binding.rvUser.setHasFixedSize(true)

        list = arrayListOf()
        adapter = UserAdapter(list)
        binding.rvUser.adapter = adapter


        confirmSpinner = binding.selectConfirm

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
        }


        confirmSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                val selectedItem = parent?.getItemAtPosition(position).toString()

                updateFilter(selectedItem)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }


        val items = arrayOf("All", "Confirmed", "Not Confirmed")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
        confirmSpinner.adapter = adapter


        listUser()
    }

    private fun listUser() {
        db = FirebaseFirestore.getInstance()

        val userId = auth.currentUser?.uid

        if (userId != null) {
            // Query berdasarkan status email dikonfirmasi atau tidak
            val confirmedEmailQuery = db.collection("users")
                .whereEqualTo("isEmailConfirmed", true)

            val unconfirmedEmailQuery = db.collection("users")
                .whereEqualTo("isEmailConfirmed", false)


            val finalQuery = when {
                isConfirmedFilter && isNotConfirmedFilter -> {

                    confirmedEmailQuery.get().addOnSuccessListener { result ->
                        list.clear()
                        for (document in result) {
                            val user = User(
                                document.getString("name"),
                                document.getString("email"),
                            )
                            list.add(user)
                        }
                        adapter.notifyDataSetChanged()
                    }

                    unconfirmedEmailQuery.get().addOnSuccessListener { result ->
                        for (document in result) {
                            val user = User(
                                document.getString("name"),
                                document.getString("email"),
                            )
                            list.add(user)
                        }
                        adapter.notifyDataSetChanged()
                    }
                    return
                }
                isConfirmedFilter -> confirmedEmailQuery
                isNotConfirmedFilter -> unconfirmedEmailQuery
                else -> null
            }


            finalQuery?.get()
                ?.addOnSuccessListener { result ->
                    list.clear()
                    for (document in result) {
                        val user = User(
                            document.getString("name"),
                            document.getString("email"),
                        )
                        list.add(user)
                    }
                    adapter.notifyDataSetChanged()
                }
                ?.addOnFailureListener { exception ->
                    println("Error getting documents: $exception")
                }
        } else {
            println("User ID is null")

        }
    }

    private fun updateFilter(selectedItem: String) {
        when (selectedItem) {
            "All" -> {
                isConfirmedFilter = true
                isNotConfirmedFilter = true
            }
            "Confirmed" -> {
                isConfirmedFilter = true
                isNotConfirmedFilter = false
            }
            "Not Confirmed" -> {
                isConfirmedFilter = false
                isNotConfirmedFilter = true
            }
        }
        listUser()
    }
}