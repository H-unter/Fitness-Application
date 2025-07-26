package com.example.fitnessapp.views

data class WorkoutHistoryUIState(
    val workouts: List<WorkoutHistoryItem> = emptyList()
)

data class WorkoutHistoryItem(
    val id: Long,
    val date: String,
    val startTime: String,
    val duration: String,
    val exercises: List<String>,
    val gymName: String?,
    val rawStartTimeMs: Long,
    val rawEndTimeMs: Long
)