package com.example.dailydose.model


data class Mood (
    var moodTitle: MoodType,
    val journalId: String,
    val userId: String,
)

enum class MoodType {
    Frustrated,
    Neutral,
    Sad,
    Happy,
    Excited
}
