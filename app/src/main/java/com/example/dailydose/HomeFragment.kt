package com.example.dailydose

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.dailydose.adapter.JournalAdapter
import com.example.dailydose.databinding.FragmentHomeBinding
import com.example.dailydose.model.Journal
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import java.util.Calendar
import com.google.firebase.Timestamp

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var journalAdapter: JournalAdapter
    private val journalList = mutableListOf<Journal>()
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        journalAdapter = JournalAdapter(journalList, isHomeFragment = true)
        binding.recyclerViewStories.adapter = journalAdapter
        binding.recyclerViewStories.layoutManager = LinearLayoutManager(requireContext())

        // Add click listeners
        binding.profileImage.setOnClickListener {
            // Navigate to ProfileFragment
            findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
        }

        binding.viewall.setOnClickListener {
            // Navigate to JournalsFragment
            findNavController().navigate(R.id.action_homeFragment_to_journalsFragment)
        }

        fetchUserNameAndJournals()

        return binding.root
    }

    private fun fetchUserNameAndJournals() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { userDocument ->
                if (_binding == null) return@addOnSuccessListener

                val firstName = userDocument.getString("Fname") ?: "there"
                val profileImageUrl = userDocument.getString("profileImage") // Ambil URL gambar profil

                // Set the greeting message with the user's first name
                setGreetingMessage(firstName)

                // Load the profile image using Glide
                if (profileImageUrl != null) {
                    Glide.with(this)
                        .load(profileImageUrl)
                        .placeholder(R.drawable.default_profile) // Gambar default jika belum ada
                        .into(binding.profileImage)
                }

                // Fetch journals for the user
                fetchJournals(userId)
            }
            .addOnFailureListener { exception ->
                Log.w("HomeFragment", "Error fetching user name: ", exception)
                if (_binding != null) {
                    binding.textHello.text = "Hello there!"
                }
                fetchJournals(userId)
            }
    }

    private fun setGreetingMessage(firstName: String) {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greetingMessage = when {
            currentHour < 12 -> "Good Morning, $firstName!"
            currentHour < 18 -> "Good Afternoon, $firstName!"
            else -> "Good Evening, $firstName!"
        }
        binding.textHello.text = greetingMessage
    }

    private fun fetchJournals(userId: String) {
        firestore.collection("journals")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(3)
            .get()
            .addOnSuccessListener { documents ->
                if (view == null || _binding == null) return@addOnSuccessListener

                journalList.clear()
                for (document in documents) {
                    val journalId = document.id
                    val journalTitle = document.getString("journalTitle") ?: ""
                    val journalText = document.getString("journalText") ?: ""
                    val imageUrl = document.getString("imageUrl") ?: ""
                    val mood = document.getString("mood") ?: ""
                    val timestamp: Timestamp? = document.getTimestamp("timestamp")

                    journalList.add(Journal(journalId, journalTitle, journalText, imageUrl, userId, mood,timestamp ?: Timestamp.now()))
                }

                // Update the UI based on the journal list
                updateJournalListUI()
            }
            .addOnFailureListener { exception ->
                if (view == null || _binding == null) return@addOnFailureListener
                Log.w("HomeFragment", "Error getting documents: ", exception)
                Toast.makeText(requireContext(), "Error fetching journals", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateJournalListUI() {
        if (journalList.isEmpty()) {
            binding.textNoJournals.visibility = View.VISIBLE
            binding.textNoJournals.text = "No journals yet! Start creating your own journal now 😊"
        } else {
            binding.textNoJournals.visibility = View.GONE
        }
        journalAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}