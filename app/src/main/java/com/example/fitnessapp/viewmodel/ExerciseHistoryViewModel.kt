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
    val sets: List<Pair<String, String>>,
    val isInProgress: Boolean = false
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
                    val isInProgressDeferred = viewModelScope.async {
                        exerciseRepository.isSetGroupInProgress(setGroup.setGroupId.toLong())
                    }

                    val workoutTime = workoutTimeDeferred.await()
                    val gymName = gymNameDeferred.await()
                    val isInProgress = isInProgressDeferred.await()

                    timestamps.add(workoutTime.toDouble())

                    fun convertToKg(weight: String, unit: com.example.fitnessapp.data.WeightUnit): Double {
                        val w = weight.toDoubleOrNull() ?: 0.0
                        return when (unit) {
                            com.example.fitnessapp.data.WeightUnit.KG, com.example.fitnessapp.data.WeightUnit.UNIT -> w
                            com.example.fitnessapp.data.WeightUnit.LB -> w * 0.453592
                        }
                    }

                    val volume = setGroup.entries.sumOf { entry ->
                        val weightKg = convertToKg(entry.weight, setGroup.weightUnit)
                        val reps = entry.reps.toDoubleOrNull() ?: 0.0
                        weightKg * reps
                    }
                    volumeSeries.add(volume)

                    val oneRepMax = setGroup.entries.maxOfOrNull { entry ->
                        val weightKg = convertToKg(entry.weight, setGroup.weightUnit)
                        val reps = entry.reps.toDoubleOrNull() ?: 0.0
                        weightKg * (1 + reps / 30.0)
                    } ?: 0.0
                    oneRepMaxSeries.add(oneRepMax)

                    // For display, show both units if conversion was made
                    val displaySets = setGroup.entries.map { entry ->
                        val weightKg = convertToKg(entry.weight, setGroup.weightUnit)
                        when (setGroup.weightUnit) {
                            com.example.fitnessapp.data.WeightUnit.KG -> "%.1f kg".format(weightKg) to entry.reps
                            com.example.fitnessapp.data.WeightUnit.LB -> "%s lbs (%.1f kg)".format(entry.weight, weightKg) to entry.reps
                            com.example.fitnessapp.data.WeightUnit.UNIT -> "%s units (%.1f kg)".format(entry.weight, weightKg) to entry.reps
                        }
                    }

                    historyItems.add(
                        SetGroupDisplayData(
                            timestamp = formatter.format(java.time.Instant.ofEpochMilli(workoutTime)),
                            gymName = gymName,
                            sets = displaySets,
                            isInProgress = isInProgress
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
