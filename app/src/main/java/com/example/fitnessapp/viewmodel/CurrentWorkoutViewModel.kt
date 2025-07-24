package com.example.fitnessapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.data.CurrentWorkoutRepository
import com.example.fitnessapp.data.ExerciseRepository
import com.example.fitnessapp.data.SetEntry
import com.example.fitnessapp.data.SetGroup
import com.example.fitnessapp.data.WeightUnit
import com.example.fitnessapp.views.CurrentWorkoutUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class CurrentWorkoutViewModel(
    private val workoutRepository: CurrentWorkoutRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CurrentWorkoutUIState(
            currentWorkout = null,
            setGroups = emptyList(),
            exerciseUiList = emptyList()
        )
    )
    val uiState: StateFlow<CurrentWorkoutUIState> = _uiState.asStateFlow()

    init {
        // observe current workout
        workoutRepository.getCurrentWorkoutOrNull()
            .onEach { workout ->
                _uiState.value = _uiState.value.copy(currentWorkout = workout)
            }
            .launchIn(viewModelScope)

        // observe set groups
        workoutRepository.getSetGroups()
            .onEach { groups ->
                _uiState.value = _uiState.value.copy(
                    setGroups = groups,
                    exerciseUiList = groups.map { sg ->
                        sg.exerciseName to sg.entries.map { entry ->
                            entry.weight.toString() to entry.reps.toString()
                        }
                    }
                )
            }
            .launchIn(viewModelScope)
    }

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
        val selectedExercise = exerciseRepository.getExerciseById(exerciseId) ?: return@launch
        val workout = _uiState.value.currentWorkout ?: return@launch

        val newSetGroup = SetGroup(
            setGroupId    = 0,
            workoutId     = workout.id,
            exerciseId    = exerciseId.toInt(),
            name          = selectedExercise.name,
            weightUnit    = WeightUnit.KG,
            exerciseName  = selectedExercise.name,
            entries       = listOf(
                SetEntry(
                    weight      = "0",
                    reps        = "0",
                )
            )
        )

        workoutRepository.addSetGroupToWorkout(newSetGroup)
    }


    // remove an exercise SetGroup
    fun removeExercise(exerciseIndex: Int) = viewModelScope.launch {
        val group = _uiState.value.setGroups.getOrNull(exerciseIndex) ?: return@launch
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
