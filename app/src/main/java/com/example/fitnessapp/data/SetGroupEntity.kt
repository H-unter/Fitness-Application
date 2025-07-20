package com.example.fitnessapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "SetGroup")
data class SetGroupEntity(
    @PrimaryKey(autoGenerate = true) val setGroupId: Int,
    val exerciseId: Int, // foreign key to ExerciseEntity
    val workoutId: Int, // foreign key to WorkoutEntity
    val weightUnit: WeightUnit
)
