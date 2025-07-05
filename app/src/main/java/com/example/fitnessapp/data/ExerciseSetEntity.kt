package com.example.fitnessapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "set")
data class ExerciseSetEntity(
    @PrimaryKey(autoGenerate = true) val setId: Int,
    val workoutId: Int,
    val exerciseId: Int,
    val weight: Double,
    val reps: Int
)
