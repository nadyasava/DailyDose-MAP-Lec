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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.example.dailydose.MainActivity
import com.example.dailydose.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        sharedPreferences = requireActivity().getSharedPreferences("UStoryPrefs", 0)

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("966560822785-etjgfhe3lh1kee77bl3tlfnnu02m8pu0.apps.googleusercontent.com") // Replace with your web client ID
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        val loginEmail = view.findViewById<EditText>(R.id.loginEmail)
        val loginPassword = view.findViewById<EditText>(R.id.loginPassword)
        val loginButton = view.findViewById<Button>(R.id.loginButton)
        val toRegister = view.findViewById<TextView>(R.id.toRegister)
        val googleSignInButton = view.findViewById<Button>(R.id.googleSignInButton)

        loginButton.setOnClickListener {
            val email = loginEmail.text.toString().trim()
            val password = loginPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            saveLoginStatus(true)
                            auth.currentUser?.uid?.let { fetchUserData(it) }
                        } else {
                            Toast.makeText(requireContext(), "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(requireContext(), "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }

        googleSignInButton.setOnClickListener {
            startGoogleSignIn()
        }

        toRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        return view
    }

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                // Proceed with authentication
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { authTask ->
                        if (authTask.isSuccessful) {
                            // Fetch user data or navigate
                            auth.currentUser?.uid?.let { fetchUserData(it) }
                        } else {
                            Toast.makeText(requireContext(), "Authentication Failed: ${authTask.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } catch (e: ApiException) {
                if (e.statusCode == GoogleSignInStatusCodes.CANCELED) {
                    Toast.makeText(requireContext(), "Sign-In was canceled by the user", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Sign-In Failed: ${e.statusCode}", Toast.LENGTH_SHORT).show()
                }
            }


        }

    private fun startGoogleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
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

                    // Switch to the main navigation graph
                    val navHostFragment = requireActivity().supportFragmentManager
                        .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                    val navController = navHostFragment.navController
                    navController.setGraph(R.navigation.nav_graph)

                    // Navigate to the home fragment after switching graph
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
