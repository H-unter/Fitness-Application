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

                        val gymName = workoutWithGroups.gym?.name

                        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                        val date = dateFormat.format(Date(workout.startTime))
                        val startTime = timeFormat.format(Date(workout.startTime))

                        WorkoutHistoryItem(
                            id = workout.workoutId.toLong(),
                            date = date,
                            startTime = startTime,
                            duration = calculateDuration(workout.startTime, workout.endTime),
                            exercises = exercises,
                            gymName = gymName,
                            rawStartTimeMs = workout.startTime,
                            rawEndTimeMs = workout.endTime
                        )
                    }
                }
                .collect { items ->
                    _uiState.value = WorkoutHistoryUIState(workouts = items)
                }
        }
    }

    private fun calculateDuration(start: Long, end: Long): String {
        val durationSeconds = (end - start) / 1000
        val hours = durationSeconds / 3600
        val minutes = (durationSeconds % 3600) / 60
        val seconds = durationSeconds % 60

        return when {
            hours > 0 -> String.format("%dh %02dm %02ds", hours, minutes, seconds)
            minutes > 0 -> String.format("%dm %02ds", minutes, seconds)
            else -> String.format("%ds", seconds)
        }
    }
}