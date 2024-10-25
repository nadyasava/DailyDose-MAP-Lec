package com.example.dailydose.model

import com.google.firebase.Timestamp

data class Journal(
    val journalTitle: String,
    val journalText: String,
    val imageUrl: String?,
    val userId: String,
    var mood: String? = null,
    val timestamp: Timestamp
)