package com.example.fitnessapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.data.repositories.ExerciseRepository
import com.example.fitnessapp.data.Exercise
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExerciseListSelectionViewModel(
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    // the list of all exercises available for selection
    val exercises: StateFlow<List<Exercise>> =
        exerciseRepository
            .getAllExercises()
            .stateIn(
                scope        = viewModelScope,
                started      = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    // create a brand new exercise with the given name //
    fun createExercise(name: String) = viewModelScope.launch {
        Log.i(
            "ExerciseListSelectionViewModel",
            "createExercise() — name = $name"
        )
        exerciseRepository.insertExercise(name)
    }

    // update an exercise name
    fun updateExerciseName(exerciseId: Long, newName: String) = viewModelScope.launch {
        exerciseRepository.updateExerciseName(exerciseId, newName)
    }
}
