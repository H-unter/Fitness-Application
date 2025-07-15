package com.example.fitnessapp.data
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

// Current workout is a list of exercises

interface CurrentWorkoutRepository {
    val currentWorkout: StateFlow<WorkoutEntity?>

    suspend fun startNewWorkout(gymId: Int = 0): Long
    suspend fun finishCurrentWorkout()

    fun getCurrentWorkout(): Flow<Workout>
    fun getSetGroups(): Flow<List<SetGroup>>

    suspend fun addExercise(setGroup: SetGroup) // add a setGroup to the current workout
    suspend fun removeExercise(setGroup: SetGroup) // remove a setGroup from the current workout

    suspend fun addSetToExercise(exerciseIndex: Int) // add a set to a specific exercise
    suspend fun removeSetFromExercise(exerciseIndex: Int, setIndex: Int) // remove a set from a specific exercise

    suspend fun updateSetWeight(exerciseIndex: Int, setIndex: Int, weight: String) // edit the enum attribute of one of the setGroup objects within the setGroup list
    suspend fun updateSetReps(exerciseIndex: Int, setIndex: Int, reps: String) // edit the enum attribute of one of the setGroup objects within the setGroup list
}
