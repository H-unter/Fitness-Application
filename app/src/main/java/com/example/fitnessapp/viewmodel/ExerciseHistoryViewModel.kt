package com.example.fitnessapp.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.data.ExerciseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class SetGroupDisplayData(
    val timestamp: String,
    val sets: List<Pair<String, String>>
)

data class ExerciseHistoryScreenState(
    val exerciseName: String = "",
    val history: List<SetGroupDisplayData> = emptyList()
)

@RequiresApi(Build.VERSION_CODES.O)
class ExerciseHistoryViewModel(
    private val exerciseRepository: ExerciseRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val exerciseId: Long = savedStateHandle["exerciseId"] ?: 0L

    companion object {
        @RequiresApi(Build.VERSION_CODES.O)
        private val formatter: DateTimeFormatter =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
                .withZone(ZoneId.systemDefault())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    val screenState: StateFlow<ExerciseHistoryScreenState> =
        exerciseRepository
            .getExerciseActivityById(exerciseId)
            .map { setGroups ->
                val history = setGroups.map { setGroup ->
                    // parse the raw epoch (stored in name) and format it
                    val raw = setGroup.name.toLongOrNull() ?: 0L
                    val formatted = formatter.format(Instant.ofEpochMilli(raw))
                    SetGroupDisplayData(
                        timestamp = formatted,
                        sets = setGroup.entries.map { it.weight.toString() to it.reps.toString() }
                    )
                }
                val exerciseName = setGroups.firstOrNull()?.exerciseName ?: "Unknown"
                ExerciseHistoryScreenState(exerciseName, history)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = ExerciseHistoryScreenState()
            )
}