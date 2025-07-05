package com.example.fitnessapp.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [WorkoutEntity::class, ExerciseEntity::class, ExerciseSetEntity::class],
    version = 1,
    exportSchema = false
)

abstract class GymActivityDatabase : RoomDatabase() {
    abstract val WorkoutDao: WorkoutDao
    abstract val ExerciseDao: ExerciseDao
    abstract val ExerciseSetDao: ExerciseSetDao
}