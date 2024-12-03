package com.example.dailydose

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import android.util.Log
import android.view.View
import com.example.dailydose.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("UStoryPrefs", MODE_PRIVATE)

        setupNavigation()
        handleEdgeToEdgeInsets()
        setInitialNavGraph()
        setupBottomNavigation()
    }

    private fun setInitialNavGraph() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val loggedIn = isLoggedIn()
        Log.d("MainActivity", "Checking login status: $loggedIn")

        if (loggedIn) {
            navController.setGraph(R.navigation.nav_graph) // Graph for Home
        } else {
            navController.setGraph(R.navigation.auth_graph) // Graph for Login
        }
    }

    private fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("isLoggedIn", false)
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment, R.id.registerFragment, R.id.resetPasswordFragment  -> {
                    hideNavBar()
                }
                else -> {
                    showNavBar()
                }
            }
        }
    }

    private fun handleEdgeToEdgeInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupBottomNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        NavigationUI.setupWithNavController(binding.bottomNavigation, navController)


        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> {
                    navController.navigate(R.id.homeFragment)
                    true
                }
                R.id.calendarFragment -> {
                    navController.navigate(R.id.calendarFragment)
                    true
                }
                R.id.addFragment -> {
                    navController.navigate(R.id.addFragment)
                    true
                }
                R.id.journalsFragment -> {
                    navController.navigate(R.id.journalsFragment)
                    true
                }
                R.id.profileFragment -> {
                    navController.navigate(R.id.profileFragment)
                    true
                }
                else -> false
            }
        }

    }

    private fun hideNavBar() {
        binding.bottomNavigation.visibility = View.GONE
    }

    private fun showNavBar() {
        binding.bottomNavigation.visibility = View.VISIBLE
    }

    fun signOut() {
        sharedPreferences.edit()
            .putBoolean("isLoggedIn", false) // Tandai pengguna sebagai tidak login
            .apply()
        Log.d("MainActivity", "User logged out: false") // Log status

        // Navigasi ke auth graph
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        navController.setGraph(R.navigation.auth_graph) // Kembali ke auth graph
    }

    private fun navigateToLogin() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        navController.navigate(R.id.loginFragment)
    }

    fun loginUser() {
        sharedPreferences.edit()
            .putBoolean("isLoggedIn", true)
            .apply()
        Log.d("MainActivity", "User logged in: true") // Log status
    }

    private fun navigateToHome() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        navController.navigate(R.id.homeFragment)
    }
}