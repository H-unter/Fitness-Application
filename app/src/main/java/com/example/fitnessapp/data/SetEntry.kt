package com.example.fitnessapp.data

/**
 * This data class represents a single entry in a set of an exercise.
 * It contains the weight lifted, the number of repetitions performed, as well as a boolean indicating whether the set was completed.
*/
data class SetEntry (
    val weight: String = "",
    val reps: String = "",
    val completed: Boolean = false
)