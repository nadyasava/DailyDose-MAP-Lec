package com.example.dailydose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.dailydose.databinding.FragmentAddTwoBinding
import com.example.dailydose.databinding.FragmentEditJournalTwoBinding
import com.example.dailydose.model.Journal
import com.example.dailydose.model.Mood
import com.example.dailydose.model.MoodType
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Timestamp

class EditJournalFragmentTwo : Fragment() {
    private lateinit var binding: FragmentEditJournalTwoBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var journalTitle: String? = null
    private var selectedImageUrl: String? = null
    private var selectedMood: String? = null
    private var prev_content: String? = null
    private var journalId: String? = null
    private var content: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditJournalTwoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase instances
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Retrieve the journal title and selected image URL passed from AddFragment
        journalTitle = arguments?.getString("journalTitle")
        selectedImageUrl = arguments?.getString("selectedImageUrl") // Retrieve the selected image URL
        selectedMood = arguments?.getString("selectedMood")
        prev_content = arguments?.getString("content")
        journalId = arguments?.getString("id")

        binding.editTextContent.setText(prev_content)

        binding.buttonUpdate.setOnClickListener {
            submitJournal()
        }

        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun submitJournal() {
        content = binding.editTextContent.text.toString()

        // Validate input fields
        if (journalTitle.isNullOrEmpty() || content!!.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a Journal object
        val journal = Journal(
            id = journalId,
            journalTitle = journalTitle!!,
            journalText = content!!,
            imageUrl = selectedImageUrl, // Set the selected image URL
            userId = auth.currentUser?.uid ?: "",
            mood = selectedMood,
            timestamp = Timestamp.now()
        )

        if(!journalId.isNullOrEmpty()){
            firestore.collection("journals")
                .document(journalId!!)
                .set(journal)
                .addOnSuccessListener {

                    submitMood()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to update journal field", Toast.LENGTH_SHORT).show()
                }
        }

    }

    private fun submitMood() {

        val moodType = when (selectedMood) {
            "Frustrated" -> MoodType.Frustrated
            "Neutral" -> MoodType.Neutral
            "Sad" -> MoodType.Sad
            "Happy" -> MoodType.Happy
            "Excited" -> MoodType.Excited
            else -> null // Jika selectedMood tidak cocok dengan salah satu mood yang ditentukan
        }

        if (moodType != null) {

            firestore.collection("moods").whereEqualTo("journalId", journalId)
                .get()
                .addOnSuccessListener { documents ->

                    for (document in documents){
                        updateMood(document.id, moodType)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to upload journal. Please try again.", Toast.LENGTH_SHORT).show()
                }

        } else {
            Toast.makeText(requireContext(), "Please select a valid mood", Toast.LENGTH_SHORT).show()
        }

    }

    private fun updateMood(moodId: String, mood: MoodType){
        firestore.collection("moods")
            .document(moodId)
            .update("moodTitle", mood)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Journal updated successfully", Toast.LENGTH_SHORT).show()

                findNavController().navigate(R.id.homeFragment)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to update mood. Please try again.", Toast.LENGTH_SHORT).show()
            }
    }
}