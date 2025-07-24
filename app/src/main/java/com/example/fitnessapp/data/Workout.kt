package com.example.fitnessapp.data

/** Domain data class to represent a workout which intern contains a list of set groups */
data class Workout(
    val id: Int,
    val locationId: Int,
    val startTime: Long,
    val endTime: Long,
    val isInProgress: Boolean,
    val setGroups: List<SetGroup>
)