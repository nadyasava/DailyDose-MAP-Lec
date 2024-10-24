package com.example.dailydose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.dailydose.databinding.FragmentAddTwoBinding
import com.example.dailydose.model.Journal
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Timestamp

class AddFragmentTwo : Fragment() {

    private lateinit var binding: FragmentAddTwoBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var journalTitle: String? = null
    private var selectedImageUrl: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddTwoBinding.inflate(inflater, container, false)
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

        binding.buttonSubmit.setOnClickListener {
            submitJournal()
        }

        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun submitJournal() {
        val content = binding.editTextContent.text.toString()

        // Validate input fields
        if (journalTitle.isNullOrEmpty() || content.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a Journal object
        val journal = Journal(
            journalTitle = journalTitle!!,
            journalText = content,
            imageUrl = selectedImageUrl, // Set the selected image URL
            userId = auth.currentUser?.uid ?: "",
            timestamp = Timestamp.now()
        )

        // Save the journal to Firestore
        firestore.collection("journals").add(journal)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Journal uploaded successfully", Toast.LENGTH_SHORT).show()
                // Navigate directly to HomeFragment after successful upload
                findNavController().navigate(R.id.action_addFragmentTwo_to_homeFragment)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to upload journal. Please try again.", Toast.LENGTH_SHORT).show()
            }
    }
}
