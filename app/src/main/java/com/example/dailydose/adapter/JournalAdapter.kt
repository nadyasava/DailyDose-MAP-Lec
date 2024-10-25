package com.example.dailydose.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.dailydose.R
import com.example.dailydose.model.Journal
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Locale

class JournalAdapter(
    private val journals: List<Journal>,
    private val isHomeFragment: Boolean
) : RecyclerView.Adapter<JournalAdapter.JournalViewHolder>() {

    inner class JournalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val journalImage: ImageView = itemView.findViewById(R.id.journalImage)
        private val journalTitle: TextView = itemView.findViewById(R.id.journalTitle)
        private val journalContent: TextView = itemView.findViewById(R.id.journalContent)
        private val journalTimestamp: TextView = itemView.findViewById(R.id.journalTimestamp)
        private val journalMood: TextView = itemView.findViewById(R.id.journal_mood)

        fun bind(journal: Journal) {
            journalTitle.text = journal.journalTitle
            journalContent.text = journal.journalText
            journalTimestamp.text = formatTimestamp(journal.timestamp)
            journalMood.text = journal.mood

            // Load image if exists
            journal.imageUrl?.let {
                Glide.with(itemView.context)
                    .load(it)
                    .override(ViewGroup.LayoutParams.MATCH_PARENT, 300)
                    .into(journalImage)
            }

            // Set click listener for the entire card view
            itemView.setOnClickListener {
                val bundle = Bundle().apply {
                    putString("imageUrl", journal.imageUrl)
                    putString("content", journal.journalText)
                    putString("timestamp", formatTimestamp(journal.timestamp))
                    putString("title", journal.journalTitle)
                    putString("mood", journal.mood)
                }
                itemView.findNavController().navigate(R.id.detailsFragment, bundle)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JournalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.journal_item, parent, false)
        return JournalViewHolder(view)
    }

    override fun onBindViewHolder(holder: JournalViewHolder, position: Int) {
        holder.bind(journals[position])
    }

    override fun getItemCount(): Int = journals.size

    private fun formatTimestamp(timestamp: com.google.firebase.Timestamp): String {
        val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
        return dateFormat.format(timestamp.toDate())
    }
}