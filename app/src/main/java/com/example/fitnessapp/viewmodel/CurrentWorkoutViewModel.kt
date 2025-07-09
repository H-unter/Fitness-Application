package com.example.fitnessapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.data.CurrentWorkoutRepository
import com.example.fitnessapp.data.Exercise
import com.example.fitnessapp.data.ExerciseRepository
import com.example.fitnessapp.data.SetGroup
import com.example.fitnessapp.data.SetItem
import com.example.fitnessapp.data.WeightUnit
import com.example.fitnessapp.data.Workout
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CurrentWorkoutViewModel(
    private val workoutRepository: CurrentWorkoutRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    // the current workout (or null if none started)
    val currentWorkout: StateFlow<Workout?> =
        workoutRepository
            .getCurrentWorkout()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // the list of exercises (setGroups) in that workout
    val setGroups: StateFlow<List<SetGroup>> =
        workoutRepository
            .getSetGroups()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // kick off a brand new workout; returns the new rowId under the hood
    fun startNewWorkout(gymId: Int = 0) = viewModelScope.launch {
        workoutRepository.startNewWorkout(gymId)
    }

    // mark the current workout as finished
    fun finishCurrentWorkout() = viewModelScope.launch {
        workoutRepository.finishCurrentWorkout()
    }

    // add an exercise (setGroup) by its id
    fun addExerciseById(exerciseId: Long) = viewModelScope.launch {
        val picked = exerciseRepository.getExerciseById(exerciseId) ?: return@launch
        Log.d("CurrentWorkoutViewModel", "picked = $picked, exerciseId = $exerciseId, name = ${picked.name}")
        val workout = currentWorkout.value ?: return@launch
        val newSetGroup = SetGroup(
            id         = 0,
            workoutId  = workout.id,
            name       = picked.name,
            weightUnit = WeightUnit.KG,
            sets       = listOf(SetItem(weight = "0", reps = "0"))
        )
        workoutRepository.addExercise(newSetGroup)
    }

    // remove an exercise SetGroup
    fun removeExercise(exerciseIndex: Int) = viewModelScope.launch {
        val group = setGroups.value.getOrNull(exerciseIndex) ?: return@launch
        workoutRepository.removeExercise(group)
    }

    // add a new set to a specific exercise
    fun addSetToExercise(exerciseIndex: Int) = viewModelScope.launch {
        workoutRepository.addSetToExercise(exerciseIndex)
    }

    // remove a set from a specific exercise
    fun removeSetFromExercise(exerciseIndex: Int, setIndex: Int) = viewModelScope.launch {
        workoutRepository.removeSetFromExercise(exerciseIndex, setIndex)
    }

    // update one set’s weight in a given exercise
    fun updateSetWeight(exerciseIndex: Int, setIndex: Int, newWeight: String) = viewModelScope.launch {
        workoutRepository.updateSetWeight(exerciseIndex, setIndex, newWeight)
    }

    // Update one sets reps in a given exercise
    fun updateSetReps(exerciseIndex: Int, setIndex: Int, newReps: String) = viewModelScope.launch {
        workoutRepository.updateSetReps(exerciseIndex, setIndex, newReps)
    }
}
