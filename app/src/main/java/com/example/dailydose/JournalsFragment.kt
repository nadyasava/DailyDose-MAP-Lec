package com.example.dailydose

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
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
        savedInstanceState: Bundle?,
    ): View {

        _binding = FragmentJournalsBinding.inflate(inflater, container, false)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val filterButton: ImageButton = binding.buttonFilter
        val backButton: Button = binding.backBtn
        val clearFilterButton: Button = binding.clearFilterBtn

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        journalAdapter = JournalAdapter(filteredJournalList, isHomeFragment = false) // Update adapter to use filtered list
        binding.recyclerViewJournals.adapter = journalAdapter
        binding.recyclerViewJournals.layoutManager = LinearLayoutManager(requireContext())


        fetchJournals()
        setupSearchFunctionality()

        filterButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
        }

        backButton.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
        }

        clearFilterButton.setOnClickListener {
            filteredJournalList.clear()
            filteredJournalList.addAll(journalList)

            binding.frustratedSelectedBtn.visibility = View.GONE
            binding.neutralSelectedBtn.visibility = View.GONE
            binding.sadSelectedBtn.visibility = View.GONE
            binding.happySelectedBtn.visibility = View.GONE
            binding.excitedSelectedBtn.visibility = View.GONE

            clearFilterButton.visibility = View.GONE

            updateJournalListUI()
        }

        setupFilterByMood(drawerLayout)

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
                    val mood = document.getString("mood")  ?: ""
                    val timestamp: Timestamp? = document.getTimestamp("timestamp")

                    val journal = Journal(journalTitle, journalText, imageUrl, userId, mood,timestamp ?: Timestamp.now())
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

    private fun setupFilterByMood(drawerLayout: DrawerLayout) {
        val selectedMoods = mutableListOf<String>()
        val searchButton = binding.searchBtn

        searchButton.setOnClickListener {
            for (moodButton in binding.moodButtonGroup.selectedButtons) {
                selectedMoods.add(moodButton.text)
            }

            if (selectedMoods.isNotEmpty()) {
                filterJournalsByMood(selectedMoods)

                selectedMoods.clear()
                drawerLayout.closeDrawer(GravityCompat.END)
            } else {
                Toast.makeText(requireContext(), "Silahkan Pilih Mood", Toast.LENGTH_SHORT).show()
            }
        }
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

    private fun filterJournalsByMood(selectedMoods: List<String>) {
        // Jika tidak ada mood yang dipilih, tampilkan semua jurnal
        if (selectedMoods.isEmpty()) {
            filteredJournalList.clear()
            filteredJournalList.addAll(journalList)
            updateJournalListUI()
            return
        }

        binding.clearFilterBtn.visibility = View.VISIBLE

        binding.frustratedSelectedBtn.visibility = View.GONE
        binding.neutralSelectedBtn.visibility = View.GONE
        binding.sadSelectedBtn.visibility = View.GONE
        binding.happySelectedBtn.visibility = View.GONE
        binding.excitedSelectedBtn.visibility = View.GONE

        for (mood in selectedMoods){
            when(mood) {
                "Frustrated" -> binding.frustratedSelectedBtn.visibility = View.VISIBLE
                "Neutral" -> binding.neutralSelectedBtn.visibility = View.VISIBLE
                "Sad" -> binding.sadSelectedBtn.visibility = View.VISIBLE
                "Happy" -> binding.happySelectedBtn.visibility = View.VISIBLE
                "Excited" -> binding.excitedSelectedBtn.visibility = View.VISIBLE
            }
        }

        // Ambil userId dari user yang sedang login
        val userId = auth.currentUser?.uid ?: return

        // Buat query ke koleksi "moods" di Firestore berdasarkan moodTitle yang dipilih
        firestore.collection("moods")
            .whereEqualTo("userId", userId) // Filter hanya untuk user yang sedang login
            .whereIn("moodTitle", selectedMoods) // Filter berdasarkan mood yang dipilih
            .get()
            .addOnSuccessListener { moodDocuments ->
                val moodTitles = moodDocuments.mapNotNull { it.getString("moodTitle") }

                // Filter jurnal berdasarkan journalId yang ditemukan
                filteredJournalList.clear()
                journalList.forEach { journal ->
                    if (moodTitles.contains(journal.mood)) {
                        filteredJournalList.add(journal)
                    }
                }

                // Update UI dengan daftar jurnal yang terfilter
                updateJournalListUI()
            }
            .addOnFailureListener { exception ->
                Log.e("JournalsFragment", "Error fetching moods: ", exception)
                Toast.makeText(requireContext(), "Error fetching mood filters", Toast.LENGTH_SHORT).show()
            }
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