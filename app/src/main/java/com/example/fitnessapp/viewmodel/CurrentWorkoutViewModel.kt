package com.example.fitnessapp.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.fitnessapp.data.CurrentWorkoutRepository
import com.example.fitnessapp.data.ExerciseSet
import com.example.fitnessapp.data.Exercise
import com.example.fitnessapp.data.Workout
import com.example.fitnessapp.data.WeightUnit

class CurrentWorkoutViewModel(
    private val workoutRepository: CurrentWorkoutRepository // Injected via Koin
) : ViewModel() {

    private val _exerciseList = mutableStateListOf<Exercise>() // private version that cant be messed with from the outside
    val exerciseList: List<Exercise> get() = _exerciseList

    init {
        addExercise()
    }

    fun addExercise() { // TODO: implement a list of exercises and add a new exercise to the list
        val newExercise = Exercise(
            id = _exerciseList.size + 1,
            workoutId = 0,
            name = "Exercise ${_exerciseList.size + 1}",
            weightUnit = WeightUnit.KG,
            sets = mutableStateListOf(ExerciseSet("0", "0"))
        )
        _exerciseList.add(newExercise)
    }

    fun removeExercise() {
        if (_exerciseList.isNotEmpty()) {
            _exerciseList.removeAt(_exerciseList.lastIndex)
        }
    }

    fun addSetToExercise(exerciseIndex: Int) {
        _exerciseList[exerciseIndex].sets.add(ExerciseSet("0", "0"))
    }

    fun removeSetFromExercise(exerciseIndex: Int) {
        val sets = _exerciseList[exerciseIndex].sets
        if (sets.isNotEmpty()) sets.removeAt(sets.lastIndex)
    }

    fun updateSetWeight(exerciseIndex: Int, setIndex: Int, newWeight: String) {
        val exercise = _exerciseList[exerciseIndex]
        val updatedSet = exercise.sets[setIndex].copy(weight = newWeight)
        exercise.sets[setIndex] = updatedSet
    }

    fun updateSetReps(exerciseIndex: Int, setIndex: Int, newReps: String) {
        val exercise = _exerciseList[exerciseIndex]
        val updatedSet = exercise.sets[setIndex].copy(reps = newReps)
        exercise.sets[setIndex] = updatedSet
    }
}
