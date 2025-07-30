package com.example.fitnessapp.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.data.repositories.ExerciseRepository
import com.example.fitnessapp.data.SetGroup
import com.example.fitnessapp.views.ExerciseHistoryUIState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class SetGroupDisplayData(
    val timestamp: String,
    val sets: List<Pair<String, String>>
)

@RequiresApi(Build.VERSION_CODES.O)
class ExerciseHistoryViewModel(
    private val exerciseRepository: ExerciseRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val exerciseId: Long =
        savedStateHandle["exerciseId"] ?: error("No exerciseId in nav args")

    // raw flow of SetGroups from the repository
    private val setGroups: StateFlow<List<SetGroup>> =
        exerciseRepository
            .getExerciseActivityById(exerciseId = exerciseId, excludeCurrentWorkout = true)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // one-shot flow for the exercise name
    private val exerciseNameFlow: Flow<String> = flow {
        emit(exerciseRepository.getExerciseNameById(exerciseId))
    }

    // formatter for history timestamps
    private val formatter = DateTimeFormatter
        .ofPattern("dd MMM yyyy, HH:mm")
        .withZone(ZoneId.systemDefault())

    // x-axis values: epoch milliseconds as Doubles
    @OptIn(ExperimentalCoroutinesApi::class)
    private val timestamps: StateFlow<List<Double>> = setGroups
        .flatMapLatest { groups ->
            if (groups.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(groups.map { sg ->
                    exerciseRepository.getWorkoutStartTimeForSetGroup(sg.setGroupId.toLong())
                }) { epochs -> epochs.map(Long::toDouble) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // total volume per group
    private val volumeSeries: StateFlow<List<Double>> = setGroups
        .map { groups ->
            groups.map { sg ->
                sg.entries.sumOf { it.weight.toDouble() * it.reps.toDouble() }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // estimated 1RM per group
    private val oneRepMaxSeries: StateFlow<List<Double>> = setGroups
        .map { groups ->
            groups.map { sg ->
                sg.entries.maxOfOrNull { entry ->
                    val w = entry.weight.toDouble()
                    val r = entry.reps.toDouble()
                    w * (1 + r / 30.0)
                } ?: 0.0
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // rows for the history list
    @OptIn(ExperimentalCoroutinesApi::class)
    private val historyItems: StateFlow<List<SetGroupDisplayData>> = setGroups
        .flatMapLatest { groups ->
            if (groups.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(groups.map { sg ->
                    // format each group's date
                    exerciseRepository
                        .getWorkoutStartTimeForSetGroup(sg.setGroupId.toLong())
                        .map { epoch -> formatter.format(java.time.Instant.ofEpochMilli(epoch)) }
                        .map { formatted ->
                            SetGroupDisplayData(
                                timestamp = formatted,
                                sets      = sg.entries.map { it.weight to it.reps }
                            )
                        }
                }) { items -> items.toList() }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // single UI state combining everything the view needs
    val uiState: StateFlow<ExerciseHistoryUIState> = combine(
        exerciseNameFlow,
        timestamps,
        volumeSeries,
        oneRepMaxSeries,
        historyItems
    ) { name, xs, vols, oneRm, history ->
        ExerciseHistoryUIState(
            exerciseName    = name,
            xValues         = xs,
            volumeSeries    = vols,
            oneRepMaxSeries = oneRm,
            historyItems    = history
        )
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExerciseHistoryUIState())
}
