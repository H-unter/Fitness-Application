package com.example.fitnessapp.viewmodel

import android.annotation.SuppressLint
import android.util.Log
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.data.HealthConnectAvailability
import com.example.fitnessapp.data.HealthConnectManager
import com.example.fitnessapp.data.WeightUnit
import com.example.fitnessapp.data.room.WorkoutDao
import com.example.fitnessapp.data.room.toDomain
import com.example.fitnessapp.views.HealthConnectSession
import com.example.fitnessapp.views.WorkoutHistoryItem
import com.example.fitnessapp.views.WorkoutHistoryUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WorkoutHistoryViewModel(
    private val workoutDao: WorkoutDao,
    private val healthConnectManager: HealthConnectManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(WorkoutHistoryUIState())
    val uiState: StateFlow<WorkoutHistoryUIState> = _uiState.asStateFlow()

    companion object {
        private const val TAG = "WorkoutHistoryVM"
        @SuppressLint("ConstantLocale")
        private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    }
    val requestPermissionActivityContract = PermissionController.createRequestPermissionResultContract()

    init {
        loadWorkoutHistory()
        loadHealthConnectWorkoutIds()
    }

    private fun loadWorkoutHistory() {
        viewModelScope.launch {
            try {
                val healthConnectResult = healthConnectManager.readAllExerciseSessions()
                val healthConnectSessions = healthConnectResult.getOrNull()?.associateBy {
                    it.metadata.clientRecordId
                } ?: emptyMap()

                val workouts = workoutDao.getWorkouts().first()
                    .filter { !it.isInProgress }
                    .filter { it.endTime > 0 }

                // Update sync status for all workouts
                workouts.forEach { workout ->
                    val workoutId = workout.workoutId.toString()
                    val existsInHealthConnect = healthConnectSessions.containsKey(workoutId)

                    if (existsInHealthConnect && !workout.isAndroidHealthConnectSynced) {
                        // Update the flag if it exists in Health Connect but isn't marked as synced
                        workoutDao.updateHealthConnectSynced(workout.workoutId, true)
                        Log.d(TAG, "Updated sync status for workout ${workout.workoutId}")
                    }
                }

                // Only attempt to sync workouts that are truly not in Health Connect
                workouts.forEach { workout ->
                    val workoutId = workout.workoutId.toString()
                    if (!healthConnectSessions.containsKey(workoutId)) {
                        val workoutWithGroups = workoutDao.getWorkoutWithSetGroupsAndEntries(workout.workoutId).first()
                        val result = healthConnectManager.writeWorkoutToHealthConnect(workoutWithGroups)
                        if (result.isSuccess) {
                            workoutDao.updateHealthConnectSynced(workout.workoutId, true)
                        }
                    }
                }

                // Continue with existing workout history loading
                val historyItems = workouts.map { workout ->
                    val workoutWithGroups = workoutDao.getWorkoutWithSetGroupsAndEntries(workout.workoutId).first()
                    val setGroups = workoutWithGroups.setGroups.map { setGroupWithEntries ->
                        // Convert weights based on unit type
                        val convertedEntries = setGroupWithEntries.entries.map { entry ->
                            val weight = entry.weight?.toString() ?: ""
                            val convertedWeight = when (setGroupWithEntries.group.weightUnit) {
                                WeightUnit.KG -> weight
                                WeightUnit.LB -> {
                                    // Convert lbs to kg (1 lb = 0.453592 kg)
                                    weight.toFloatOrNull()?.let { it * 0.453592f }?.toString() ?: weight
                                }
                                WeightUnit.UNIT -> weight // Treat units as equivalent to kg
                            }
                            entry.copy(weight = convertedWeight.toFloatOrNull())
                        }
                        setGroupWithEntries.copy(entries = convertedEntries).toDomain()
                    }

                    WorkoutHistoryItem(
                        id = workout.workoutId.toLong(),
                        date = dateFormat.format(Date(workout.startTime)),
                        startTime = timeFormat.format(Date(workout.startTime)),
                        duration = calculateDuration(workout.startTime, workout.endTime),
                        setGroups = setGroups,
                        gymName = workoutWithGroups.gym?.name,
                        rawStartTimeMs = workout.startTime,
                        rawEndTimeMs = workout.endTime,
                        isAndroidHealthConnectSynced = workout.isAndroidHealthConnectSynced
                    )
                }

                _uiState.value = WorkoutHistoryUIState(workouts = historyItems)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading workout history", e)
                e.printStackTrace()
            }
        }
    }

    private fun loadHealthConnectWorkoutIds() {
        viewModelScope.launch {
            try {
                val availability = healthConnectManager.checkAvailability()
                if (availability != HealthConnectAvailability.INSTALLED || !healthConnectManager.hasAllPermissions()) {
                    return@launch
                }

                val result = healthConnectManager.readAllExerciseSessions()
                if (result.isSuccess) {
                    val sessions = result.getOrNull() ?: emptyList()
                    val workoutIds = sessions.mapNotNull { it.metadata.clientRecordId }.toSet()

                    _uiState.value = _uiState.value.copy(
                        healthConnectWorkoutIds = workoutIds
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading Health Connect workout IDs", e)
            }
        }
    }

    fun checkPermissionsOnly() {
        viewModelScope.launch {
            try {
                val availability = healthConnectManager.checkAvailability()
                Log.d(TAG, "Health Connect availability: $availability")

                if (availability != HealthConnectAvailability.INSTALLED) {
                    updatePermissionsState(false)
                    return@launch
                }

                val hasPermissions = healthConnectManager.hasAllPermissions()
                updatePermissionsState(hasPermissions)

                // Load Health Connect workout IDs only if permissions are granted
                if (hasPermissions) {
                    loadHealthConnectWorkoutIds()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking permissions", e)
                updatePermissionsState(false)
            }
        }
    }

    private fun updatePermissionsState(granted: Boolean) {
        _uiState.value = _uiState.value.copy(
            permissionsGranted = granted,
            permissionsChecked = true
        )
        Log.d(TAG, "Permissions state updated to: $granted")
    }

    fun syncHistoricalWorkoutsToHealthConnect() {
        viewModelScope.launch {
            try {
                val workouts = workoutDao.getWorkouts().first()

                for (workout in workouts) {
                    // Skip workouts in progress
                    if (workout.isInProgress) continue

                    val workoutWithGroups = workoutDao.getWorkoutWithSetGroupsAndEntries(workout.workoutId).first()

                    // Write to Health Connect
                    healthConnectManager.writeWorkoutToHealthConnect(workoutWithGroups)
                }
            } catch (e: Exception) {
                e.printStackTrace()
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

    fun readAllHealthConnectData() {
        viewModelScope.launch {
            try {
                val availability = healthConnectManager.checkAvailability()
                if (availability != HealthConnectAvailability.INSTALLED) {
                    Log.e(TAG, "Health Connect not installed")
                    return@launch
                }

                if (!healthConnectManager.hasAllPermissions()) {
                    Log.e(TAG, "Missing required permissions")
                    return@launch
                }

                Log.d(TAG, "Starting Health Connect data read test")
                val result = healthConnectManager.readAllExerciseSessions()

                if (result.isSuccess) {
                    val sessions = result.getOrNull()
                    Log.d(TAG, "Successfully read ${sessions?.size ?: 0} exercise sessions")

                    val healthConnectSessions = sessions?.map { session ->
                        // Calculate total reps from segments if available
                        val totalReps = session.segments.sumOf { it.repetitions }

                        HealthConnectSession(
                            id = session.metadata.id,
                            title = session.title,
                            startTime = session.startTime,
                            endTime = session.endTime,
                            exerciseType = session.exerciseType,
                            segmentCount = session.segments.size,
                            totalReps = totalReps,
                            clientRecordId = session.metadata.clientRecordId
                        )
                    }

                    _uiState.value = _uiState.value.copy(
                        healthConnectTestResult = "Found ${sessions?.size ?: 0} exercise sessions",
                        healthConnectSessions = healthConnectSessions,
                        showHealthConnectDialog = true
                    )
                } else {
                    val error = result.exceptionOrNull()
                    Log.e(TAG, "Failed to read exercise sessions: ${error?.message}", error)
                    _uiState.value = _uiState.value.copy(
                        healthConnectTestResult = "Error: ${error?.message}",
                        showHealthConnectDialog = false
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error reading Health Connect data", e)
                _uiState.value = _uiState.value.copy(
                    healthConnectTestResult = "Exception: ${e.message}",
                    showHealthConnectDialog = false
                )
            }
        }
    }

    fun dismissHealthConnectDialog() {
        _uiState.value = _uiState.value.copy(
            showHealthConnectDialog = false
        )
    }

    fun onPermissionsRevoked() {
        // Clear Health Connect related data when permissions are revoked
        _uiState.value = _uiState.value.copy(
            permissionsGranted = false,
            permissionsChecked = true,
            healthConnectWorkoutIds = null,
            healthConnectSessions = null,
            showHealthConnectDialog = false,
            healthConnectTestResult = null
        )
        Log.d(TAG, "Permissions revoked - ViewModel state updated")
    }

    fun refreshPermissionsState() {
        viewModelScope.launch {
            try {
                val availability = healthConnectManager.checkAvailability()
                if (availability != HealthConnectAvailability.INSTALLED) {
                    _uiState.value = _uiState.value.copy(
                        permissionsGranted = false,
                        permissionsChecked = true,
                        healthConnectWorkoutIds = null
                    )
                    return@launch
                }

                val hasPermissions = healthConnectManager.hasAllPermissions()
                _uiState.value = _uiState.value.copy(
                    permissionsGranted = hasPermissions,
                    permissionsChecked = true
                )

                if (!hasPermissions) {
                    // Clear Health Connect data if permissions were revoked
                    _uiState.value = _uiState.value.copy(
                        healthConnectWorkoutIds = null,
                        healthConnectSessions = null,
                        showHealthConnectDialog = false,
                        healthConnectTestResult = null
                    )
                } else {
                    loadHealthConnectWorkoutIds()
                }

                Log.d(TAG, "Permissions state refreshed: $hasPermissions")
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing permissions state", e)
                _uiState.value = _uiState.value.copy(
                    permissionsGranted = false,
                    permissionsChecked = true
                )
            }
        }
    }

    fun onPermissionsGranted() {
        viewModelScope.launch {
            try {
                // Update permissions state immediately
                _uiState.value = _uiState.value.copy(
                    permissionsGranted = true,
                    permissionsChecked = true
                )

                // Then load Health Connect data and sync workouts
                loadHealthConnectWorkoutIds()
                syncHistoricalWorkoutsToHealthConnect()

                Log.d(TAG, "Permissions granted - UI state updated and workouts synced")
            } catch (e: Exception) {
                Log.e(TAG, "Error after permissions granted", e)
            }
        }
    }

    fun onPermissionsDenied() {
        _uiState.value = _uiState.value.copy(
            permissionsGranted = false,
            permissionsChecked = true,
            healthConnectWorkoutIds = null
        )
        Log.d(TAG, "Permissions denied - UI state updated")
    }

}