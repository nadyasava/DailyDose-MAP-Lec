package com.example.dailydose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth

class SetNewPasswordFragment : Fragment() {

    private lateinit var newPasswordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var savePasswordButton: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_set_new_password, container, false)

        auth = FirebaseAuth.getInstance()
        newPasswordEditText = view.findViewById(R.id.newPassword)
        confirmPasswordEditText = view.findViewById(R.id.confirmPassword)
        savePasswordButton = view.findViewById(R.id.savePasswordButton)

        savePasswordButton.setOnClickListener {
            updatePassword()
        }

        return view
    }

    private fun updatePassword() {
        val newPassword = newPasswordEditText.text.toString()
        val confirmPassword = confirmPasswordEditText.text.toString()

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword != confirmPassword) {
            Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isPasswordStrong(newPassword)) {
            Toast.makeText(requireContext(), "Password is not strong enough", Toast.LENGTH_SHORT).show()
            return
        }

        val user = auth.currentUser
        user?.let {
            it.updatePassword(newPassword).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Password updated successfully", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_resetPasswordFragment_to_loginFragment)
                } else {
                    Toast.makeText(requireContext(), "Failed to update password", Toast.LENGTH_SHORT).show()
                }
            }
        } ?: run {
            Toast.makeText(requireContext(), "No user signed in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isPasswordStrong(password: String): Boolean {
        val passwordPattern = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#\$%^&+=!])(?=\\S+\$).{8,}\$")
        return password.matches(passwordPattern)
    }
}