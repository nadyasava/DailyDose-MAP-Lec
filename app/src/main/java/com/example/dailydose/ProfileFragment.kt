package com.example.dailydose

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private lateinit var textName: TextView
    private lateinit var textEmail: TextView
    private lateinit var profileImage: ImageView
    private lateinit var buttonEdit: Button
    private lateinit var buttonLogout: Button
    private lateinit var buttonResetPassword: Button
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        textName = view.findViewById(R.id.textName)
        textEmail = view.findViewById(R.id.textEmail)
        profileImage = view.findViewById(R.id.profileImage)
        buttonEdit = view.findViewById(R.id.buttonEdit)
        buttonLogout = view.findViewById(R.id.buttonLogout)
        buttonResetPassword = view.findViewById(R.id.buttonResetPassword) // Inisialisasi tombol

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Load user data
        loadUserData()

        // Set up button listeners
        buttonEdit.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }

        buttonLogout.setOnClickListener {
            firebaseAuth.signOut()
            (activity as MainActivity).signOut()
        }

        // Listener untuk tombol reset password
        buttonResetPassword.setOnClickListener {
            Log.d("ProfileFragment", "Navigating to ResetPasswordFragment")
            try {
                findNavController().navigate(R.id.action_profileFragment_to_resetPasswordFragment)
            } catch (e: Exception) {
                Log.e("ProfileFragment", "Navigation error: ${e.message}")
            }
        }

        return view
    }

    private fun loadUserData() {
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val email = document.getString("email")
                        val fname = document.getString("Fname")
                        val lname = document.getString("Lname")
                        val profileImageName = document.getString("profileImage")

                        textEmail.text = email
                        textName.text = "$fname $lname"

                        // Load profile image or set default from drawable
                        if (profileImageName == "default_profile") {
                            profileImage.setImageResource(R.drawable.default_profile)
                        } else {
                            Glide.with(this)
                                .load(profileImageName)
                                .placeholder(R.drawable.default_profile)
                                .into(profileImage)
                        }
                    } else {
                        textEmail.text = "Email Not found"
                        textName.text = "Name Not found"
                        profileImage.setImageResource(R.drawable.default_profile)
                    }
                }
                .addOnFailureListener { exception ->
                    textName.text = "Error loading name"
                    textEmail.text = "Error loading email"
                    profileImage.setImageResource(R.drawable.default_profile)
                }
        } else {
            textEmail.text = "Email Not available"
            textName.text = "User not logged in"
            profileImage.setImageResource(R.drawable.default_profile)
        }
    }
}