package com.example.fitnessapp.data
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {

    fun getAllExercises(): Flow<List<Exercise>>

    suspend fun insertExercise(name: String)

    suspend fun getExerciseById(id: Long): Exercise?
}