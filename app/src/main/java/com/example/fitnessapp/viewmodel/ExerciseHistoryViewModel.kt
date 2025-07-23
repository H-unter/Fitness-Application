package com.example.fitnessapp.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.data.ExerciseRepository
import com.example.fitnessapp.data.SetGroup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf

data class SetGroupDisplayData(
    val timestamp: String,
    val sets: List<Pair<String, String>>
)

data class ExerciseHistoryScreenState(
    val exerciseName: String = "",
    val history: List<SetGroupDisplayData> = emptyList()
)

class ExerciseHistoryViewModel(
    private val exerciseRepository: ExerciseRepository,
    savedStateHandle: SavedStateHandle
): ViewModel() {
    private val exerciseId: Long = savedStateHandle["exerciseId"] ?: 0L

    // exercise name
    private val _exerciseName = MutableStateFlow("")
    val exerciseName: StateFlow<String> = _exerciseName

    init {
        viewModelScope.launch {
            _exerciseName.value = exerciseRepository
                .getExerciseNameById(exerciseId)
        }
    }

    val setGroups: StateFlow<List<SetGroup>> =
        exerciseRepository
            .getExerciseActivityById(exerciseId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())


    val timestamps: StateFlow<List<Double>> = setGroups
        .flatMapLatest { groups ->
            if (groups.isEmpty()) {
                flowOf(emptyList<Double>())
            } else {
                combine(
                    groups.map { sg ->
                        exerciseRepository.getWorkoutStartTimeForSetGroup(sg.setGroupId.toLong())
                    }
                ) { epochsArray: Array<Long> ->
                    epochsArray.map { it.toDouble() }
                }
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList<Double>()
        )


    // total volume per setGroup
    val volumes: Flow<List<Double>> = setGroups.map { groups ->
        groups.map { sg ->
            sg.entries.sumOf { e ->
                e.weight.toDouble() * e.reps.toDouble()
            }
        }
    }

    val oneRepMaxes: Flow<List<Double>> = setGroups.map { groups ->
        groups.map { setGroups ->
            setGroups.entries
                .maxOfOrNull { setEntry ->
                    val weight = setEntry.weight.toDouble()
                    val reps = setEntry.reps.toDouble()
                    weight * (1 + reps / 30.0)
                } ?: 0.0
        }
    }

    // formatted date for a single setGroup
    @RequiresApi(Build.VERSION_CODES.O)
    fun dateForSetGroup(setGroupId: Int): Flow<String> =
        exerciseRepository
            .getWorkoutStartTimeForSetGroup(setGroupId.toLong())
            .map { epoch ->
                formatter.format(Instant.ofEpochMilli(epoch))
            }

    companion object {
        @RequiresApi(Build.VERSION_CODES.O)
        private val formatter = DateTimeFormatter
            .ofPattern("dd MMM yyyy, HH:mm")
            .withZone(ZoneId.systemDefault())
    }
}
