package com.example.fitnessapp.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

data class Exercise(
    val id: Int,
    val workoutId: Int,
    val name: String,
    val weightUnit: WeightUnit,
    val sets: SnapshotStateList<ExerciseSet> = mutableStateListOf()
)
enum class WeightUnit {
    KG, LB
}