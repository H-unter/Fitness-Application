package com.example.fitnessapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.fitnessapp.data.ExerciseDao

@Database(
    entities = [ExerciseEntity::class],
    version = 1,
    exportSchema = false
)

abstract class GymActivityDatabase : RoomDatabase() {
    abstract val dao: ExerciseDao
}