package com.example.fitnessapp.data.repositories
import com.example.fitnessapp.data.SetGroup
import com.example.fitnessapp.data.Workout
import com.example.fitnessapp.data.room.WorkoutEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

// Current workout is a list of exercises

interface CurrentWorkoutRepository {
    val currentWorkout: StateFlow<WorkoutEntity?>

    suspend fun startNewWorkout(gymId: Int = 0): Long
    suspend fun finishCurrentWorkout(endTime: Long = System.currentTimeMillis())

    fun getCurrentWorkoutOrNull(): Flow<Workout?>
    suspend fun updateWorkoutGym(workoutId: Long, gymId: Int)
    fun getSetGroups(): Flow<List<SetGroup>>

    suspend fun addSetGroupToWorkout(setGroup: SetGroup) // add a setGroup to the current workout
    suspend fun removeExercise(setGroup: SetGroup) // remove a setGroup from the current workout

    suspend fun addSetToExercise(exerciseIndex: Int) // add a set to a specific exercise
    suspend fun removeSetFromExercise(exerciseIndex: Int, setIndex: Int) // remove a set from a specific exercise

    suspend fun updateSetWeight(exerciseIndex: Int, setIndex: Int, weight: String)
    suspend fun updateSetReps(exerciseIndex: Int, setIndex: Int, reps: String)

    suspend fun updateSetCompletion(exerciseIndex: Int, setIndex: Int, completed: Boolean)
}
