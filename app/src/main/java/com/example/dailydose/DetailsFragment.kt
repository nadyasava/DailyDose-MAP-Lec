package com.example.dailydose

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import androidx.fragment.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.google.api.Distribution.BucketOptions.Linear
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DetailsFragment : Fragment() {
    private var id: String? = null
    private var imageUrl: String? = null
    private var content: String? = null
    private var timestamp: String? = null
    private var title: String? = null
    private var mood: String? = null

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            id = it.getString("id")
            imageUrl = it.getString("imageUrl")
            content = it.getString("content")
            timestamp = it.getString("timestamp")
            title = it.getString("title")
            mood = it.getString("mood")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val storyImageView: ShapeableImageView = view.findViewById(R.id.storyImageView)
        val storyTextView: TextView = view.findViewById(R.id.storyTextView)
        val timestampTextView: TextView = view.findViewById(R.id.timestampTextView)
        val titleTextView: TextView = view.findViewById(R.id.storyTitle)
        val backButton: ImageButton = view.findViewById(R.id.buttonBack)
        val moodType: TextView = view.findViewById(R.id.moodType)
        val dialogButton: ImageButton = view.findViewById(R.id.buttonAction)

        // Load the image using Glide
        Glide.with(this)
            .load(imageUrl)
            .into(storyImageView)

        // Set text for content, timestamp, and title
        storyTextView.text = content
        timestampTextView.text = timestamp
        titleTextView.text = title
        moodType.text = mood

        val background = moodType.background as GradientDrawable

        when (mood) {
            "Frustrated" -> background.setColor(ContextCompat.getColor(requireContext(), R.color.mood_angry))
            "Neutral" -> background.setColor(ContextCompat.getColor(requireContext(), R.color.mood_neutral))
            "Sad" -> background.setColor(ContextCompat.getColor(requireContext(), R.color.mood_sad))
            "Happy" -> background.setColor(ContextCompat.getColor(requireContext(), R.color.mood_happy))
            "Excited" -> background.setColor(ContextCompat.getColor(requireContext(), R.color.mood_excited))
        }

        dialogButton.setOnClickListener {

            showDialog()
        }

        // Set up back button click listener
        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun showDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.fragment_dialog)
        dialog.setCancelable(true)

        // Ambil referensi dari elemen-elemen dalam layout dialog
        val editLayout: LinearLayout = dialog.findViewById(R.id.editOption)
        val deleteLayout: LinearLayout = dialog.findViewById(R.id.deleteOption)

        // Tambahkan listener untuk tombol-tombol dalam dialog
        editLayout.setOnClickListener {
            dialog.dismiss()
            val bundle = Bundle().apply {
                putString("id", id)
                putString("title", title)
                putString("imageUrl", imageUrl)
                putString("mood", mood)
                putString("content", content)
            }

            findNavController().navigate(R.id.editJournalFragment, bundle)
        }

        deleteLayout.setOnClickListener {
            dialog.dismiss()
            showDeleteConfirmationDialog()
        }

        dialog.show()
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)
    }

    private fun deleteJournal(journalId : String){
        if (journalId.isEmpty()) {
            Toast.makeText(requireContext(), "Journal ID Not Found", Toast.LENGTH_SHORT).show()
            return
        }

        // Delete the journal document
        firestore.collection("journals")
            .document(journalId)
            .delete()
            .addOnSuccessListener {

                firestore.collection("moods")
                    .whereEqualTo("journalId", journalId)
                    .get()
                    .addOnSuccessListener { documents ->
                        for (document in documents){
                            firestore.collection("moods").document(document.id).delete()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Failed to delete associated moods", Toast.LENGTH_SHORT).show()
                    }


                Toast.makeText(requireContext(), "Journal deleted successfully", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.homeFragment)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to delete journal. Please try again.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDeleteConfirmationDialog() {
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Delete Journal")
        builder.setMessage("Are you sure you want to delete this journal? This action cannot be undone.")

        // Set up the positive button
        builder.setPositiveButton("Delete") { _, _ ->
            deleteJournal(id.toString()) // Call the delete function
        }

        // Set up the negative button
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss() // Close the dialog
        }

        // Show the dialog
        builder.create().show()
    }


}
