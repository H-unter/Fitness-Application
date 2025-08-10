package com.example.fitnessapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.data.repositories.ExerciseRepository
import com.example.fitnessapp.views.ExerciseHistoryUIState
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class SetGroupDisplayData(
    val timestamp: String,
    val gymName: String,
    val sets: List<Pair<String, String>>
)

class ExerciseHistoryViewModel(
    private val exerciseRepository: ExerciseRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val exerciseId: Long = savedStateHandle["exerciseId"] ?: error("No exerciseId in nav args")

    private val formatter = DateTimeFormatter
        .ofPattern("dd MMM yyyy, HH:mm")
        .withZone(ZoneId.systemDefault())

    val uiState: StateFlow<ExerciseHistoryUIState> =
        exerciseRepository.getExerciseActivityById(exerciseId, excludeCurrentWorkout = true)
            .map { setGroups ->
                val exerciseName = exerciseRepository.getExerciseNameById(exerciseId)

                val timestamps = mutableListOf<Double>()
                val volumeSeries = mutableListOf<Double>()
                val oneRepMaxSeries = mutableListOf<Double>()
                val historyItems = mutableListOf<SetGroupDisplayData>()

                setGroups.forEach { setGroup ->
                    val workoutTimeDeferred = viewModelScope.async {
                        exerciseRepository.getWorkoutStartTimeForSetGroup(setGroup.setGroupId.toLong()).first()
                    }
                    val gymNameDeferred = viewModelScope.async {
                        exerciseRepository.getGymNameForSetGroup(setGroup.setGroupId.toLong()) ?: "Unknown Gym"
                    }

                    val workoutTime = workoutTimeDeferred.await()
                    val gymName = gymNameDeferred.await()

                    timestamps.add(workoutTime.toDouble())

                    val volume = setGroup.entries.sumOf { entry ->
                        val weight = entry.weight.toDoubleOrNull() ?: 0.0
                        val reps = entry.reps.toDoubleOrNull() ?: 0.0
                        weight * reps
                    }
                    volumeSeries.add(volume)

                    val oneRepMax = setGroup.entries.maxOfOrNull { entry ->
                        val weight = entry.weight.toDoubleOrNull() ?: 0.0
                        val reps = entry.reps.toDoubleOrNull() ?: 0.0
                        weight * (1 + reps / 30.0)
                    } ?: 0.0
                    oneRepMaxSeries.add(oneRepMax)

                    historyItems.add(
                        SetGroupDisplayData(
                            timestamp = formatter.format(java.time.Instant.ofEpochMilli(workoutTime)),
                            gymName = gymName,
                            sets = setGroup.entries.map { it.weight to it.reps }
                        )
                    )
                }

                ExerciseHistoryUIState(
                    exerciseName = exerciseName,
                    xValues = timestamps,
                    volumeSeries = volumeSeries,
                    oneRepMaxSeries = oneRepMaxSeries,
                    historyItems = historyItems
                )
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExerciseHistoryUIState())
}
