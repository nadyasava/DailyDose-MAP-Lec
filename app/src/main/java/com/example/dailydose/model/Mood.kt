package com.example.dailydose.model


data class Mood (
    val moodTitle: MoodType,
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
