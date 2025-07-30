package com.example.fitnessapp.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "Workout")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true) val workoutId: Int,
    val gymId: Int, // future 1:M relationship to be modelled
    val startTime: Long,
    val endTime: Long,
    val isInProgress: Boolean,
//    val isAndroidHealthConnectSynced: Boolean
)
