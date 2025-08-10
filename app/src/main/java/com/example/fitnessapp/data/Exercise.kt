package com.example.fitnessapp.data


/**
Domain Data class that represents a type of exercise, with a name as the identifier.
* Each set group will have an associated exercise, but it is the job of the SetGroup class
* to store the SetEntries nested within it.
 */
data class Exercise(
    val exerciseId: Long,
    val name: String
)