package com.example.fitnessapp.views

import com.example.fitnessapp.data.SetGroup

data class WorkoutHistoryUIState(
    val workouts: List<WorkoutHistoryItem> = emptyList(),
    val permissionsGranted: Boolean = false,
    val permissionsChecked: Boolean = false
)

data class WorkoutHistoryItem(
    val id: Long,
    val date: String,
    val startTime: String,
    val duration: String,
    val setGroups: List<SetGroup>,
    val gymName: String?,
    val rawStartTimeMs: Long,
    val rawEndTimeMs: Long
)