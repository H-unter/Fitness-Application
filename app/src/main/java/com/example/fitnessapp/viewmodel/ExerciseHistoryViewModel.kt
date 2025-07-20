package com.example.fitnessapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.data.ExerciseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class SetGroupDisplayData(
    val label: String,
    val sets: List<Pair<String, String>>,
    val rpe: Int = 8 // Default value; replace or compute as needed
)

data class ExerciseHistoryScreenState(
    val exerciseName: String = "",
    val history: List<SetGroupDisplayData> = emptyList()
)

class ExerciseHistoryViewModel(
    private val exerciseRepository: ExerciseRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val exerciseId: Long = savedStateHandle["exerciseId"] ?: 0L

    private val _screenState: StateFlow<ExerciseHistoryScreenState> =
        exerciseRepository.getExerciseActivityById(exerciseId)
            .map { setGroups ->
                val history = setGroups.map { setGroup ->
                    SetGroupDisplayData(
                        label = setGroup.name,
                        sets = setGroup.entries.map { it.weight.toString() to it.reps.toString() }
                    )
                }
                val exerciseName = setGroups.firstOrNull()?.exerciseName ?: "Unknown"
                ExerciseHistoryScreenState(
                    exerciseName = exerciseName,
                    history = history
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = ExerciseHistoryScreenState()
            )

    val screenState: StateFlow<ExerciseHistoryScreenState> get() = _screenState
}
