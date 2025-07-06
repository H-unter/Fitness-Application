package com.example.fitnessapp.data

data class Workout(
    val id: Int,
    val locationId: Int,
    val startTime: Long,
    val endTime: Long,
    val setGroups: List<SetGroup> // list of the ExerciseSet class
)