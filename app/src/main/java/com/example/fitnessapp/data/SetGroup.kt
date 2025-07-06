package com.example.fitnessapp.data

data class SetGroup(
    val id: Int,
    val workoutId: Int,
    val name: String,
    val weightUnit: WeightUnit,
    val sets: List<SetItem>
)


