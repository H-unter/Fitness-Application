package com.example.fitnessapp.data

import com.example.fitnessapp.data.WeightUnit

data class SetGroup(
    val setGroupId: Int,
    val workoutId: Int,
    val exerciseId: Int,
    val name: String,
    val weightUnit: WeightUnit,
    val exerciseName: String,
    val entries: List<SetEntry>
)


