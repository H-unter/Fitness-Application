package com.example.fitnessapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        WorkoutEntity::class,
        ExerciseEntity::class,
        SetGroupEntity::class,
        SetEntryEntity::class,
        GymEntity::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(WeightUnitTypeConverters::class)
abstract class GymActivityDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun gymDao(): GymDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun setGroupDao(): SetGroupDao
    abstract fun setEntryDao(): SetEntryDao
}