package com.example.dailydose

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth

class SetNewPasswordFragment : Fragment() {

    private lateinit var emailEditText: EditText
    private lateinit var savePasswordButton: Button
    private lateinit var backButton: ImageButton
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_set_new_password, container, false)

        auth = FirebaseAuth.getInstance()
        emailEditText = view.findViewById(R.id.emailAddress)
        savePasswordButton = view.findViewById(R.id.sendEmailButton)
        backButton = view.findViewById(R.id.backButton)

        savePasswordButton.setOnClickListener {
            sendResetPasswordEmail()
        }

        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        return view
    }

    private fun sendResetPasswordEmail() {
        val email = emailEditText.text.toString()

        if (email.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter your email", Toast.LENGTH_SHORT).show()
            return
        }

        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(requireContext(), "Reset password email sent", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_set_new_password_fragment_to_profile_fragment)
            } else {
                Toast.makeText(requireContext(), "Error sending email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
