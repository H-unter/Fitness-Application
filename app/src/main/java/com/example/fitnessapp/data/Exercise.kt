package com.example.fitnessapp.data

data class Exercise(
    val id: Int,
    val workoutId: Int, // infer gym from workout id
    val name: String,
    val weightUnit: WeightUnit = WeightUnit.Kg,
    val sets: List<ExerciseSet>
)

enum class WeightUnit {
    Kg, Lb
}