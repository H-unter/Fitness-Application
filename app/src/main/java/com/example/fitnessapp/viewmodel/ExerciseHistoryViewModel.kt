package com.example.fitnessapp.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.text.style.TextDecoration.Companion.combine
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.data.ExerciseRepository
import com.example.fitnessapp.data.SetGroup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
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

class ExerciseHistoryViewModel(
    private val exerciseRepository: ExerciseRepository,
    savedStateHandle: SavedStateHandle
): ViewModel() {
    private val exerciseId: Long = savedStateHandle["exerciseId"] ?: 0L
    val exerciseName: String = savedStateHandle["exerciseName"] ?: ""
    // just the raw history of set-groups (no dates)
    val setGroups: StateFlow<List<SetGroup>> =
        exerciseRepository
            .getExerciseActivityById(exerciseId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // each set-group’s formatted date
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
