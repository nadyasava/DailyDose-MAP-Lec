package com.example.dailydose

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dailydose.adapter.JournalAdapter
import com.example.dailydose.databinding.FragmentJournalsBinding
import com.example.dailydose.model.Journal
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.Timestamp

class JournalsFragment : Fragment() {

    private var _binding: FragmentJournalsBinding? = null
    private val binding get() = _binding!!

    private lateinit var journalAdapter: JournalAdapter
    private val journalList = mutableListOf<Journal>()
    private var filteredJournalList = mutableListOf<Journal>()
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJournalsBinding.inflate(inflater, container, false)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        journalAdapter = JournalAdapter(filteredJournalList, isHomeFragment = false) // Update adapter to use filtered list
        binding.recyclerViewJournals.adapter = journalAdapter
        binding.recyclerViewJournals.layoutManager = LinearLayoutManager(requireContext())

        fetchJournals()
        setupSearchFunctionality()

        return binding.root
    }

    private fun fetchJournals() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("journals")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                if (view == null || _binding == null) return@addOnSuccessListener

                journalList.clear()
                for (document in documents) {
                    val journalTitle = document.getString("journalTitle") ?: ""
                    val journalText = document.getString("journalText") ?: ""
                    val imageUrl = document.getString("imageUrl") ?: ""
                    val timestamp: Timestamp? = document.getTimestamp("timestamp")

                    val journal = Journal(journalTitle, journalText, imageUrl, userId, timestamp ?: Timestamp.now())
                    journalList.add(journal)
                }

                filteredJournalList.clear()
                filteredJournalList.addAll(journalList)
                updateJournalListUI()
            }
            .addOnFailureListener { exception ->
                if (view == null || _binding == null) return@addOnFailureListener
                Log.w("JournalsFragment", "Error getting documents: ", exception)
                Toast.makeText(requireContext(), "Error fetching journals", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateJournalListUI() {
        if (filteredJournalList.isEmpty()) {
            binding.textNoJournalsFound.visibility = View.VISIBLE
            binding.textNoJournalsFound.text = "No journals found! Try searching for a different title 😊"
        } else {
            binding.textNoJournalsFound.visibility = View.GONE
        }
        journalAdapter.notifyDataSetChanged()
    }

    private fun setupSearchFunctionality() {
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterJournalsByTitle(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterJournalsByTitle(query: String) {
        filteredJournalList.clear()
        if (query.isEmpty()) {
            filteredJournalList.addAll(journalList)
        } else {
            journalList.forEach { journal ->
                if (journal.journalTitle.contains(query, ignoreCase = true)) {
                    filteredJournalList.add(journal)
                }
            }
        }
        updateJournalListUI()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
