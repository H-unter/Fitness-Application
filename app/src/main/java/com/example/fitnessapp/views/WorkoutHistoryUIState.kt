package com.example.fitnessapp.views

import com.example.fitnessapp.data.SetGroup
import java.time.Instant

data class WorkoutHistoryUIState(
    val workouts: List<WorkoutHistoryItem> = emptyList(),
    val permissionsGranted: Boolean = false,
    val permissionsChecked: Boolean = false,
    val healthConnectTestResult: String? = null,
    val healthConnectSessions: List<HealthConnectSession>? = null,
    val showHealthConnectDialog: Boolean = false,
    val healthConnectWorkoutIds: Set<String>? = null
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

data class HealthConnectSession(
    val id: String,
    val title: String?,
    val startTime: Instant,
    val endTime: Instant,
    val exerciseType: Int,
    val segmentCount: Int,
    val totalReps: Int = 0,
    val clientRecordId: String?
)
