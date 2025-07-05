package com.example.fitnessapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Exercise")
data class ExerciseEntity (
    @PrimaryKey
    val id: String,
    val name: String
)