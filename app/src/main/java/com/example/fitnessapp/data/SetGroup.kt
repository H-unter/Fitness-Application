package com.example.fitnessapp.data

data class SetGroup(
    val setGroupId: Int,
    val workoutId: Int,
    val exerciseId: Int,
    val name: String,
    val weightUnit: WeightUnit,
    val exerciseName: String,
    val entries: List<SetEntry>
)


