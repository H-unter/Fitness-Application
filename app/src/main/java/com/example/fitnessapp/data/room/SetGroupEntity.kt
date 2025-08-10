package com.example.fitnessapp.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.fitnessapp.data.WeightUnit

@Entity(tableName = "SetGroup")
data class SetGroupEntity(
    @PrimaryKey(autoGenerate = true) val setGroupId: Int,
    val exerciseId: Int,
    val workoutId: Int,
    val weightUnit: WeightUnit
)
