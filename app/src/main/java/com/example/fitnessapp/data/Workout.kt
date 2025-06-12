package com.example.fitnessapp.data

data class Workout(
    val id: Int,
    val gymId: Int,
    val startTime: Long, // use Instant or LocalDateTime if you prefer
    val exercises: List<Exercise>
)