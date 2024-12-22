package com.example.firebaseauth

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
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
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
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

        // Inisialisasi Firebase dan SharedPreferences
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        sharedPreferences = requireActivity().getSharedPreferences("UStoryPrefs", 0)

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        val loginEmail = view.findViewById<EditText>(R.id.loginEmail)
        val loginPassword = view.findViewById<EditText>(R.id.loginPassword)
        val loginButton = view.findViewById<Button>(R.id.loginButton)
        val toRegister = view.findViewById<TextView>(R.id.toRegister)
        val googleSignInButton = view.findViewById<Button>(R.id.googleSignInButton)
        val forgotPassword = view.findViewById<TextView>(R.id.forgotPassword)

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
                            showToast("Login Failed: ${task.exception?.message}")
                        }
                    }
            } else {
                showToast("Please enter email and password")
            }
        }

        forgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_resetPasswordFragment)
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
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { authTask ->
                        if (authTask.isSuccessful) {
                            auth.currentUser?.uid?.let { userId ->
                                handleGoogleSignInSuccess(userId, account)
                            }
                        } else {
                            showToast("Authentication Failed: ${authTask.exception?.message}")
                        }
                    }
            } catch (e: ApiException) {
                if (e.statusCode == GoogleSignInStatusCodes.CANCELED) {
                    showToast("Sign-In was canceled by the user")
                } else {
                    showToast("Sign-In Failed: ${e.statusCode}")
                }
            }
        }

    private fun handleGoogleSignInSuccess(userId: String, account: GoogleSignInAccount) {
        // Buat map data user dari akun Google
        val userData = hashMapOf(
            "email" to account.email,
            "Fname" to (account.givenName ?: ""),
            "Lname" to (account.familyName ?: ""),
            "profileImage" to (account.photoUrl?.toString() ?: "default_profile")
        )

        // Simpan data ke Firestore
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    // Jika dokumen belum ada, buat baru
                    firestore.collection("users").document(userId)
                        .set(userData)
                        .addOnSuccessListener {
                            fetchUserData(userId)
                        }
                        .addOnFailureListener { e ->
                            showToast("Error saving user data: ${e.message}")
                        }
                } else {
                    // Jika dokumen sudah ada, langsung fetch data
                    fetchUserData(userId)
                }
            }
    }

    private fun startGoogleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        Log.d("LoginFragment", "Launching Google Sign-In Intent")
        googleSignInLauncher.launch(signInIntent)
    }

    private fun fetchUserData(userId: String) {
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val name = document.getString("name")
                    sharedPreferences.edit().putString("userName", name).apply()
                    (activity as MainActivity).loginUser()

                    val navHostFragment = requireActivity().supportFragmentManager
                        .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                    val navController = navHostFragment.navController
                    navController.setGraph(R.navigation.nav_graph)
                    navController.navigate(R.id.homeFragment)
                } else {
                    showToast("No such user")
                }
            }
            .addOnFailureListener { e ->
                showToast("Error fetching user data: ${e.message}")
            }
    }

    private fun saveLoginStatus(isLoggedIn: Boolean) {
        sharedPreferences.edit().putBoolean("isLoggedIn", isLoggedIn).apply()
    }

    private fun showToast(message: String) {
        if (isAdded) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }
}