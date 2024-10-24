package com.example.firebaseauth

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.example.dailydose.MainActivity
import com.example.dailydose.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        sharedPreferences = requireActivity().getSharedPreferences("UStoryPrefs", 0)

        val loginEmail = view.findViewById<EditText>(R.id.loginEmail)
        val loginPassword = view.findViewById<EditText>(R.id.loginPassword)
        val loginButton = view.findViewById<Button>(R.id.loginButton)
        val toRegister = view.findViewById<TextView>(R.id.toRegister)

        loginButton.setOnClickListener {
            val email = loginEmail.text.toString().trim()
            val password = loginPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Save login status
                            saveLoginStatus(true)

                            // Fetch user data
                            val userId = auth.currentUser?.uid
                            if (userId != null) {
                                fetchUserData(userId)
                            }
                        } else {
                            Toast.makeText(requireContext(), "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(requireContext(), "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }

        toRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        return view
    }

    private fun fetchUserData(userId: String) {
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val name = document.getString("name")
                    sharedPreferences.edit().putString("userName", name).apply()

                    // Update login status
                    (activity as MainActivity).loginUser()

                    // Ganti graph ke main graph
                    val navHostFragment = requireActivity().supportFragmentManager
                        .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                    val navController = navHostFragment.navController
                    navController.setGraph(R.navigation.nav_graph) // Ganti ke main graph

                    // Navigasi ke homeFragment setelah mengganti graph
                    navController.navigate(R.id.homeFragment)
                } else {
                    Toast.makeText(requireContext(), "No such user", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error fetching user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveLoginStatus(isLoggedIn: Boolean) {
        sharedPreferences.edit().putBoolean("isLoggedIn", isLoggedIn).apply()
    }
}