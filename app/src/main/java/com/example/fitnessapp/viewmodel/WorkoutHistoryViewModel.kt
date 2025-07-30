package com.example.fitnessapp.viewmodel

import android.util.Log
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.data.HealthConnectAvailability
import com.example.fitnessapp.data.HealthConnectManager
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
    }
    val requestPermissionActivityContract = PermissionController.createRequestPermissionResultContract()

    init {
        loadWorkoutHistory()
    }
    private fun loadWorkoutHistory() {
        viewModelScope.launch {
            try {
                val workouts = workoutDao.getWorkouts().first()

                val historyItems = workouts.map { workout ->
                    val workoutWithGroups = workoutDao.getWorkoutWithSetGroupsAndEntries(workout.workoutId).first()
                    val setGroups = workoutWithGroups.setGroups.map { it.toDomain() }

                    // format dates
                    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                    val date = dateFormat.format(Date(workout.startTime))
                    val startTime = timeFormat.format(Date(workout.startTime))

                    // Create the WorkoutHistoryItem
                    WorkoutHistoryItem(
                        id = workout.workoutId.toLong(), // Ensure ID is Long
                        date = date,
                        startTime = startTime,
                        duration = calculateDuration(workout.startTime, workout.endTime),
                        setGroups = setGroups,
                        gymName = workoutWithGroups.gym?.name,
                        rawStartTimeMs = workout.startTime,
                        rawEndTimeMs = workout.endTime
                    )
                }

                _uiState.value = WorkoutHistoryUIState(workouts = historyItems)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun checkPermissionsAndRun() {
        val granted = healthConnectManager.healthConnectClient.permissionController.getGrantedPermissions()
        if (granted.containsAll(HealthConnectManager.PERMISSIONS)) {
            // Permissions already granted; proceed with inserting or reading data
            syncHistoricalWorkoutsToHealthConnect()
        } else {
            // Will need to request permissions via launcher
            Log.d(TAG, "Permissions missing, need to request")
        }
    }

    fun checkAndRequestPermissions() {
        viewModelScope.launch {
            try {
                val availability = healthConnectManager.checkAvailability()
                Log.d(TAG, "Health Connect availability: $availability")

                if (availability != HealthConnectAvailability.INSTALLED) {
                    _uiState.value = _uiState.value.copy(
                        permissionsGranted = false,
                        permissionsChecked = true
                    )
                    return@launch
                }

                checkPermissionsAndRun()

                val hasPermissions = healthConnectManager.hasAllPermissions()
                _uiState.value = _uiState.value.copy(
                    permissionsGranted = hasPermissions,
                    permissionsChecked = true
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error checking permissions", e)
                _uiState.value = _uiState.value.copy(
                    permissionsGranted = false,
                    permissionsChecked = true
                )
            }
        }
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


    fun getPermissionLauncher() = healthConnectManager.getPermissionLauncher()

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
}