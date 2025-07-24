package com.example.fitnessapp.views

data class WorkoutHistoryUIState(
    val workouts: List<WorkoutHistoryItem> = emptyList()
)

data class WorkoutHistoryItem(
    val workoutId: Int,
    val date: String,
    val exercises: List<String>,
    val duration: String
)