package com.example.firebaseauth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.dailydose.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val registerFname = view.findViewById<EditText>(R.id.registerFname)
        val registerLname = view.findViewById<EditText>(R.id.registerLname)
        val registerEmail = view.findViewById<EditText>(R.id.registerEmail)
        val registerPassword = view.findViewById<EditText>(R.id.registerPassword)
        val registerButton = view.findViewById<Button>(R.id.registerButton)
        val toLogin = view.findViewById<TextView>(R.id.toLogin)

        registerButton.setOnClickListener {
            val fname = registerFname.text.toString().trim()
            val lname = registerLname.text.toString().trim()
            val email = registerEmail.text.toString().trim()
            val password = registerPassword.text.toString().trim()

            when {
                fname.isEmpty() || lname.isEmpty() || email.isEmpty() || password.isEmpty() -> {
                    Toast.makeText(requireContext(), "Please fill all the fields", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                fname.length > 15 -> {
                    Toast.makeText(requireContext(), "First name cannot be longer than 15 characters", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                lname.length > 30 -> {
                    Toast.makeText(requireContext(), "Last name cannot be longer than 30 characters", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                !isPasswordStrong(password) -> {
                    Toast.makeText(requireContext(), "Password must be at least 8 characters long and include an uppercase letter, a lowercase letter, a number, and a special character.", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                else -> registerUser(fname, lname, email, password)
            }
        }

        toLogin.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        return view
    }

    private fun registerUser(fname: String, lname: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid
                if (userId != null) {
                    saveUserToFirestore(userId, fname, lname, email)
                } else {
                    Toast.makeText(requireContext(), "Error: User ID is null", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Registration Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserToFirestore(userId: String, fname: String, lname: String, email: String) {
        val user = hashMapOf(
            "userId" to userId,
            "Fname" to fname,
            "Lname" to lname,
            "email" to email,
            "profileImage" to "default_profile"
        )

        firestore.collection("users").document(userId)
            .set(user)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Registration Successful", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
            }
            .addOnFailureListener { e ->
                // If Firestore save fails, delete the Auth user
                auth.currentUser?.delete()
                Toast.makeText(requireContext(), "Error saving user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun isPasswordStrong(password: String): Boolean {
        val passwordPattern = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#\$%^&+=!])(?=\\S+\$).{8,}\$")
        return password.matches(passwordPattern)
    }
}