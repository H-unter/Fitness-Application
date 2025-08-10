package com.example.fitnessapp.views

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.fitnessapp.R
import com.example.fitnessapp.data.HealthConnectManager
import com.example.fitnessapp.data.SetEntry
import com.example.fitnessapp.data.SetGroup
import com.example.fitnessapp.data.WeightUnit
import com.example.fitnessapp.ui.theme.FitnessappTheme
import com.example.fitnessapp.viewmodel.WorkoutHistoryViewModel
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WorkoutHistoryScreen(
    navController: NavHostController,
    viewModel: WorkoutHistoryViewModel = koinViewModel(),
    onPermissionsChecked: (Boolean) -> Unit = {},
    overridePermissionsGranted: Boolean? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val effectivePermissionsGranted = overridePermissionsGranted ?: uiState.permissionsGranted
    val requestPermissions = rememberLauncherForActivityResult(viewModel.requestPermissionActivityContract) { granted ->
        if (granted.containsAll(HealthConnectManager.PERMISSIONS)) {
            Log.d("WorkoutHistory", "Permissions successfully granted")
            viewModel.onPermissionsGranted()
        } else {
            Log.d("WorkoutHistory", "Lack of required permissions")
            viewModel.onPermissionsDenied()
        }
    }

    LaunchedEffect(uiState.permissionsGranted) {
        Log.d("WorkoutHistory", "Local permissions state changed to: ${uiState.permissionsGranted}")
        onPermissionsChecked(uiState.permissionsGranted)
    }
    LaunchedEffect(navController.currentBackStackEntry) { viewModel.refreshPermissionsState() }
    LaunchedEffect(Unit) { viewModel.checkPermissionsOnly() }

    LaunchedEffect(navController.currentBackStackEntry) {
        viewModel.loadWorkoutHistory()
    }

    WorkoutHistoryScreenContent(
        uiState = uiState.copy(permissionsGranted = effectivePermissionsGranted),
        navController = navController,
        onRequestPermissions = { requestPermissions.launch(HealthConnectManager.PERMISSIONS) },
        viewModel = viewModel
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutHistoryScreenContent(
    uiState: WorkoutHistoryUIState,
    navController: NavHostController,
    onRequestPermissions: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: WorkoutHistoryViewModel? = null
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Column { Text(stringResource(R.string.workout_history_title)) } },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (uiState.permissionsGranted) viewModel?.syncHistoricalWorkoutsToHealthConnect()
                            else onRequestPermissions()
                        }
                    ) {
                        Icon(
                            imageVector = if (uiState.permissionsGranted) Icons.Default.CloudSync else Icons.Outlined.CloudOff,
                            contentDescription = if (uiState.permissionsGranted)
                                stringResource(R.string.sync_to_health_connect)
                            else
                                stringResource(R.string.request_health_connect_permissions),
                            tint = if (uiState.permissionsGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.showHealthConnectDialog) {
                HealthConnectDataDialog(
                    sessions = uiState.healthConnectSessions ?: emptyList(),
                    onDismiss = { viewModel?.dismissHealthConnectDialog() }
                )
            }
            if (uiState.workouts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_workouts_to_show),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.workouts) { workout ->
                        WorkoutHistoryItem(
                            workout = workout,
                            isInHealthConnect = workout.isAndroidHealthConnectSynced
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutHistoryItem(
    workout: WorkoutHistoryItem,
    isInHealthConnect: Boolean = false
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Start
                ) {
                    workout.gymName?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    Text(text = workout.date, style = MaterialTheme.typography.titleMedium)
                }
                Icon(
                    imageVector = if (isInHealthConnect) Icons.Default.CloudDone else Icons.Default.CloudOff,
                    contentDescription = if (isInHealthConnect)
                        stringResource(R.string.synced_to_health_connect)
                    else
                        stringResource(R.string.not_synced_to_health_connect),
                    tint = if (isInHealthConnect)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = workout.startTime, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = stringResource(R.string.duration_with_value, workout.duration),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            workout.setGroups.groupBy { it.exerciseName }.forEach { (exerciseName, groups) ->
                ExerciseDetailCard(exerciseName, groups)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ExerciseDetailCard(
    exerciseName: String,
    groups: List<SetGroup>,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = exerciseName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            val allEntries = groups.flatMap { it.entries }
            allEntries.forEachIndexed { index, entry ->
                val reps = entry.reps.toIntOrNull() ?: 0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.set_number, index + 1),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "${entry.weight} x $reps",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            val totalVolume = allEntries.sumOf {
                val weight = it.weight.toFloatOrNull() ?: 0f
                val reps = it.reps.toIntOrNull() ?: 0
                (weight * reps).toInt()
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.total_volume_kg, totalVolume),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun HealthConnectDataDialog(
    sessions: List<HealthConnectSession>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.health_connect_data),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.found_sessions, sessions.size),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (sessions.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_sessions_found),
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        items(sessions) { session ->
                            HealthConnectSessionItem(session)
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

@Composable
fun HealthConnectSessionItem(session: HealthConnectSession) {
    val dateFormatter = SimpleDateFormat("MMM d, yyyy HH:mm:ss", Locale.getDefault())
    val startTimeFormatted = dateFormatter.format(Date.from(session.startTime))
    val endTimeFormatted = dateFormatter.format(Date.from(session.endTime))
    val durationSeconds = session.endTime.epochSecond - session.startTime.epochSecond
    val durationMinutes = durationSeconds / 60
    val durationHours = durationMinutes / 60

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = session.title ?: stringResource(R.string.untitled_workout),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        val exerciseTypeStr = when (session.exerciseType) {
            ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING -> stringResource(R.string.strength_training)
            else -> stringResource(R.string.other_exercise_type, session.exerciseType)
        }
        Text(
            text = stringResource(R.string.type_with_value, exerciseTypeStr),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = stringResource(R.string.duration_colon, if (durationHours > 0) "${durationHours}h " else "", durationMinutes % 60),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = stringResource(R.string.start_time, startTimeFormatted),
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = stringResource(R.string.end_time, endTimeFormatted),
            style = MaterialTheme.typography.bodySmall
        )
        if (session.segmentCount > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.segments_total_reps, session.segmentCount, session.totalReps),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
        session.clientRecordId?.let { clientId ->
            Text(
                text = stringResource(R.string.id_with_value, clientId),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

// --- Previews ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(name = "Permissions Granted", showBackground = true)
private fun WorkoutHistoryScreenPreview_PermissionsGranted() {
    val sampleSetEntries = listOf(
        SetEntry(weight = "60", reps = "10"),
        SetEntry(weight = "70", reps = "8")
    )
    val sampleSetGroups = listOf(
        SetGroup(
            setGroupId = 1,
            workoutId = 1,
            exerciseId = 1,
            name = "Bench Press",
            weightUnit = WeightUnit.KG,
            exerciseName = "Bench Press",
            entries = sampleSetEntries
        )
    )
    val sampleWorkout = WorkoutHistoryItem(
        id = 1L,
        date = "Oct 1, 2023",
        startTime = "10:00 AM",
        duration = "1h 30m",
        gymName = "Fitness Center",
        setGroups = sampleSetGroups,
        rawStartTimeMs = System.currentTimeMillis() - 5400000,
        rawEndTimeMs = System.currentTimeMillis()
    )
    val sampleState = WorkoutHistoryUIState(
        workouts = listOf(sampleWorkout),
        permissionsGranted = true,
        permissionsChecked = true
    )
    FitnessappTheme {
        WorkoutHistoryScreenContent(
            uiState = sampleState,
            navController = rememberNavController()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(name = "Permissions Not Granted", showBackground = true)
private fun WorkoutHistoryScreenPreview_PermissionsNotGranted() {
    val sampleState = WorkoutHistoryUIState(
        workouts = emptyList(),
        permissionsGranted = false,
        permissionsChecked = true
    )
    FitnessappTheme {
        WorkoutHistoryScreenContent(
            uiState = sampleState,
            navController = rememberNavController()
            )
    }
}

@Composable
@Preview(name = "Health Connect Dialog Preview", showBackground = true)
fun HealthConnectDataDialogPreview() {
    val sampleSessions = listOf(
        HealthConnectSession(
            id = "1",
            title = "Morning Workout",
            startTime = java.time.Instant.now().minusSeconds(3600),
            endTime = java.time.Instant.now(),
            exerciseType = ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING,
            segmentCount = 3,
            totalReps = 45,
            clientRecordId = "workout_123"
        ),
        HealthConnectSession(
            id = "2",
            title = "Evening Run",
            startTime = java.time.Instant.now().minusSeconds(7200),
            endTime = java.time.Instant.now().minusSeconds(5400),
            exerciseType = ExerciseSessionRecord.EXERCISE_TYPE_RUNNING,
            segmentCount = 1,
            totalReps = 0,
            clientRecordId = "workout_124"
        )
    )
    FitnessappTheme {
        Surface {
            HealthConnectDataDialog(
                sessions = sampleSessions,
                onDismiss = {}
            )
        }
    }
}
