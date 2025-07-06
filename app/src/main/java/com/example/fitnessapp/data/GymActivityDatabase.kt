package com.example.fitnessapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [WorkoutEntity::class, ExerciseEntity::class, SetGroupEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(SetTypeConverters::class, WeightUnitConverters::class)
abstract class GymActivityDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun setGroupDao(): SetGroupDao
}