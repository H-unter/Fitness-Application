package com.example.fitnessapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "SetGroup")
data class SetGroupEntity(
    @PrimaryKey(autoGenerate = true) val setGroupId: Int,
    val workoutId: Int,
    val exerciseId: Int,
    val weightUnit: WeightUnit,
    val sets: List<SetItem>
)
