package com.example.fitnessapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.data.ExerciseRepository
import com.example.fitnessapp.data.SetGroup
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ExerciseHistoryViewModel (
    private val exerciseRepository: ExerciseRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val exerciseId: Long = savedStateHandle.get<Long>("exerciseId") ?: 0L

    val exerciseName: String = savedStateHandle.get<String>("exerciseName") ?: ""

    val exerciseHistory: StateFlow<List<Triple<String, List<Pair<String, String>>, Int>>> =
        exerciseRepository
            .getExerciseActivityById(exerciseId)
            .map { setGroups ->
                setGroups.map { setGroup ->
                    val label = setGroup.name // using name as a placeholder for date
                    val sets = setGroup.entries.map { it.weight.toString() to it.reps.toString() }
                    Triple(label, sets, 8) // TODO: make rpe not hardcoded
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )
}

