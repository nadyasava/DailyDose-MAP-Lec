package com.example.dailydose

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dailydose.adapter.JournalAdapter
import com.example.dailydose.databinding.FragmentCalendarBinding
import com.example.dailydose.model.Journal
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Timestamp
import java.util.*

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private lateinit var journalAdapter: JournalAdapter
    private val journalList = mutableListOf<Journal>()
    private val filteredJournalList = mutableListOf<Journal>()
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        journalAdapter = JournalAdapter(filteredJournalList, isHomeFragment = false)
        binding.recyclerViewJournals.adapter = journalAdapter
        binding.recyclerViewJournals.layoutManager = LinearLayoutManager(requireContext())

        // Set up CalendarView
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            fetchJournalsForDate(selectedDate.time)
        }

        // Load today's journals by default
        fetchJournalsForDate(Date())

        return binding.root
    }

    private fun fetchJournalsForDate(date: Date) {
        val userId = auth.currentUser?.uid ?: return

        // Set timestamp untuk awal dan akhir hari
        val calendar = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.time

        calendar.apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }
        val endOfDay = calendar.time

        firestore.collection("journals")
            .whereEqualTo("userId", userId)
            .whereGreaterThanOrEqualTo("timestamp", startOfDay)
            .whereLessThanOrEqualTo("timestamp", endOfDay)
            .get()
            .addOnSuccessListener { documents ->
                journalList.clear()
                for (document in documents) {
                    val journalId = document.id
                    val journalTitle = document.getString("journalTitle") ?: ""
                    val journalText = document.getString("journalText") ?: ""
                    val imageUrl = document.getString("imageUrl") ?: ""
                    val mood = document.getString("mood") ?: ""
                    val timestamp: Timestamp? = document.getTimestamp("timestamp")

                    val journal = Journal(journalId, journalTitle, journalText, imageUrl, userId, mood, timestamp ?: Timestamp.now())
                    journalList.add(journal)
                }

                updateJournalListUI()
            }
            .addOnFailureListener { exception ->
                Log.w("CalendarFragment", "Error getting documents: ", exception)
                Toast.makeText(requireContext(), "Error fetching journals", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateJournalListUI() {
        filteredJournalList.clear()
        filteredJournalList.addAll(journalList)

        if (filteredJournalList.isEmpty()) {
            binding.noJournalText.visibility = View.VISIBLE
        } else {
            binding.noJournalText.visibility = View.GONE
        }
        journalAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
