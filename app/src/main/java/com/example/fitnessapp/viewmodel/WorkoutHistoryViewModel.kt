package com.example.fitnessapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.data.WorkoutDao
import com.example.fitnessapp.views.WorkoutHistoryItem
import com.example.fitnessapp.views.WorkoutHistoryUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WorkoutHistoryViewModel(
    private val workoutDao: WorkoutDao
) : ViewModel() {
    private val _uiState = MutableStateFlow(WorkoutHistoryUIState())
    val uiState: StateFlow<WorkoutHistoryUIState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            workoutDao.getWorkouts()
                .map { workouts ->
                    workouts.map { workout ->
                        val workoutWithGroups = workoutDao.getWorkoutWithSetGroupsAndEntries(workout.workoutId)
                            .first()

                        val exercises = workoutWithGroups.setGroups
                            .mapNotNull { it.exercise?.name }
                            .distinct()

                        WorkoutHistoryItem(
                            workoutId = workout.workoutId,
                            date = formatDate(workout.startTime),
                            exercises = exercises,
                            duration = calculateDuration(workout.startTime, workout.endTime)
                        )
                    }
                }
                .collect { items ->
                    _uiState.value = WorkoutHistoryUIState(workouts = items)
                }
        }
    }

    private fun formatDate(timestamp: Long): String {
        return SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            .format(Date(timestamp))
    }

    private fun calculateDuration(start: Long, end: Long): String {
        val durationMinutes = (end - start) / (1000 * 60)
        return "${durationMinutes}min"
    }
}