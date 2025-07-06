package com.example.fitnessapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "Workout")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true) val workoutId: Int,
    val gymId: Int, // future foreign key
    val startTime: Long,
    val endTime: Long,
    val isInProgress: Boolean,
//    val isAndroidHealthConnectSynced: Boolean
)
