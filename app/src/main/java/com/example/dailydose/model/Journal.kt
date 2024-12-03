package com.example.dailydose.model

import com.google.firebase.Timestamp

data class Journal(
    var id: String? = null,
    var journalTitle: String,
    var journalText: String,
    var imageUrl: String?,
    var userId: String,
    var mood: String? = null,
    var timestamp: Timestamp
)