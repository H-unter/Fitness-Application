package com.example.fitnessapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.data.CurrentWorkoutRepository
import com.example.fitnessapp.data.SetGroup
import com.example.fitnessapp.data.SetItem
import com.example.fitnessapp.data.WeightUnit
import com.example.fitnessapp.data.Workout
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CurrentWorkoutViewModel(
    private val workoutRepository: CurrentWorkoutRepository
) : ViewModel() {

    /** The current workout (or null if none started) */
    val currentWorkout: StateFlow<Workout?> =
        workoutRepository
            .getCurrentWorkout()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** The list of exercises (set-groups) in that workout */
    val setGroups: StateFlow<List<SetGroup>> =
        workoutRepository
            .getSetGroups()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Kick off a brand-new workout; returns the new rowId under the hood */
    fun startNewWorkout(gymId: Int = 0) = viewModelScope.launch {
        workoutRepository.startNewWorkout(gymId)
    }

    /** Mark the current workout as finished */
    fun finishCurrentWorkout() = viewModelScope.launch {
        workoutRepository.finishCurrentWorkout()
    }

    /** Add a new exercise (set-group) to the workout */
    fun addExercise() = viewModelScope.launch {
        Log.i("CurrentWorkoutViewModel", "addExercise() — currentWorkout = ${currentWorkout.value}")
        val workout = currentWorkout.value ?: return@launch
        val newGroup = SetGroup(
            id         = 0,
            workoutId  = workout.id,
            name       = "Exercise ${'$'}{setGroups.value.size + 1}",
            weightUnit = WeightUnit.KG,
            sets       = listOf( SetItem(weight = "0", reps = "0") )
        )
        workoutRepository.addExercise(newGroup)
    }

    /** Remove an exercise (set-group) */
    fun removeExercise(group: SetGroup) = viewModelScope.launch {
        workoutRepository.removeExercise(group)
    }

    /** Add a new set to a specific exercise */
    fun addSetToExercise(exerciseIndex: Int) = viewModelScope.launch {
        workoutRepository.addSetToExercise(exerciseIndex)
    }

    /** Remove a set from a specific exercise */
    fun removeSetFromExercise(exerciseIndex: Int, setIndex: Int) = viewModelScope.launch {
        workoutRepository.removeSetFromExercise(exerciseIndex, setIndex)
    }

    /** Update one set’s weight in a given exercise */
    fun updateSetWeight(exerciseIndex: Int, setIndex: Int, newWeight: String) = viewModelScope.launch {
        workoutRepository.updateSetWeight(exerciseIndex, setIndex, newWeight)
    }

    /** Update one set’s reps in a given exercise */
    fun updateSetReps(exerciseIndex: Int, setIndex: Int, newReps: String) = viewModelScope.launch {
        workoutRepository.updateSetReps(exerciseIndex, setIndex, newReps)
    }
}
