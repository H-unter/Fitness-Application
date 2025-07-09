package com.example.fitnessapp.data
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    // return all exercise types in the exercise table
    fun getAllExercises(): Flow<List<Exercise>>

    // insert a type of exercises into the exercise table
    suspend fun insertExercise(name: String)

    // return an exercise type by its id
    suspend fun getExerciseById(id: Long): Exercise?

    // return all exercise activity (all setGroups) given an exercise id, used in the ExerciseHistoryScreen
    fun getExerciseActivityById(exerciseId: Long): Flow<List<SetGroup>>

}