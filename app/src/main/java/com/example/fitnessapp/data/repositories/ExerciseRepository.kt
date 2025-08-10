package com.example.fitnessapp.data.repositories
import com.example.fitnessapp.data.Exercise
import com.example.fitnessapp.data.SetGroup
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    // return all exercise types in the exercise table
    fun getAllExercises(): Flow<List<Exercise>>

    // insert a type of exercises into the exercise table
    suspend fun insertExercise(name: String): Int

    // return an exercise type by its id
    suspend fun getExerciseById(id: Long): Exercise?

    // return all exercise activity (all setGroups) given an exercise id, used in the ExerciseHistoryScreen
    fun getExerciseActivityById(exerciseId: Long, excludeCurrentWorkout: Boolean): Flow<List<SetGroup>>

    // return the name of an exercise given its id
    suspend fun getExerciseNameById(exerciseId: Long): String

    // return the workout that corresponds to a given setGroup
    fun getWorkoutStartTimeForSetGroup(setGroupId: Long): Flow<Long>

    // return the gym name for a given setGroup
    suspend fun getGymNameForSetGroup(setGroupId: Long): String?

    // update the name of an exercise given its id
    suspend fun updateExerciseName(exerciseId: Long, newName: String)
}