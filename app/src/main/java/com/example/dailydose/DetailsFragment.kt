package com.example.dailydose

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView

class DetailsFragment : Fragment() {
    private var imageUrl: String? = null
    private var content: String? = null
    private var timestamp: String? = null
    private var title: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            imageUrl = it.getString("imageUrl")
            content = it.getString("content")
            timestamp = it.getString("timestamp")
            title = it.getString("title")
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

        val storyImageView: ShapeableImageView = view.findViewById(R.id.storyImageView)
        val storyTextView: TextView = view.findViewById(R.id.storyTextView)
        val timestampTextView: TextView = view.findViewById(R.id.timestampTextView)
        val titleTextView: TextView = view.findViewById(R.id.storyTitle)
        val backButton: ImageButton = view.findViewById(R.id.buttonBack)

        // Load the image using Glide
        Glide.with(this)
            .load(imageUrl)
            .into(storyImageView)

        // Set text for content, timestamp, and title
        storyTextView.text = content
        timestampTextView.text = timestamp
        titleTextView.text = title

        // Set up back button click listener
        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }
}