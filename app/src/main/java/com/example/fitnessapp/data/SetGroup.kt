package com.example.fitnessapp.data

/**
 * Domain class to represent a list of sets for a specific exercise in a workout.
 * This class is used to group sets together, allowing for better organization and management
 * of workout data.
 */
data class SetGroup(
    val setGroupId: Int,
    val workoutId: Int,
    val exerciseId: Int,
    val name: String,
    val weightUnit: WeightUnit,
    val exerciseName: String,
    val entries: List<SetEntry>
)


