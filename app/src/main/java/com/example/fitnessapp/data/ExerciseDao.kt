package com.example.fitnessapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: ExerciseEntity)

    @Query("Select * From Exercise")
    fun getExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM Exercise WHERE exerciseId = :id")
    suspend fun getExerciseById(id: Int): ExerciseEntity?
}