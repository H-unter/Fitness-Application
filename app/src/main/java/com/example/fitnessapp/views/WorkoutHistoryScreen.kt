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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.fitnessapp.data.HealthConnectManager
import com.example.fitnessapp.data.SetEntry
import com.example.fitnessapp.data.SetGroup
import com.example.fitnessapp.data.WeightUnit
import com.example.fitnessapp.ui.theme.FitnessappTheme
import com.example.fitnessapp.viewmodel.WorkoutHistoryViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun WorkoutHistoryScreen(
    navController: NavHostController,
    viewModel: WorkoutHistoryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Create the permissions launcher (exactly like docs)
    val requestPermissions = rememberLauncherForActivityResult(
        viewModel.requestPermissionActivityContract
    ) { granted ->
        if (granted.containsAll(HealthConnectManager.PERMISSIONS)) {
            // Permissions successfully granted
            Log.d("WorkoutHistory", "Permissions successfully granted")
            viewModel.checkAndRequestPermissions()
        } else {
            // Lack of required permissions
            Log.d("WorkoutHistory", "Lack of required permissions")
        }
    }

    LaunchedEffect(Unit) {
        viewModel.checkAndRequestPermissions()
    }

    // If permissions not granted, request them
    if (uiState.permissionsChecked && !uiState.permissionsGranted) {
        LaunchedEffect(Unit) {
            Log.d("WorkoutHistory", "Launching permission request...")
            requestPermissions.launch(HealthConnectManager.PERMISSIONS)
        }
    }

    WorkoutHistoryScreenContent(
        uiState = uiState,
        navController = navController,
        onRequestPermissions = {
            requestPermissions.launch(HealthConnectManager.PERMISSIONS)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutHistoryScreenContent(
    uiState: WorkoutHistoryUIState,
    navController: NavHostController,
    onRequestPermissions: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Workout History")
                    }
                },
                navigationIcon = {
                    if (uiState.permissionsGranted) {
                        IconButton(
                            onClick = {},
                            enabled = false
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = "Health Connect synced",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { onRequestPermissions() },
                            enabled = true
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = "Request Health Connect permissions",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
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
        if (uiState.workouts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(
                    text = "No workouts to show, get lifting!",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.workouts) { workout ->
                    WorkoutHistoryItem(workout = workout)
                }
            }
        }
    }
}

@Composable
private fun WorkoutHistoryItem(
    workout: WorkoutHistoryItem) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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

                Text(
                    text = workout.date,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = workout.startTime,
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "Duration: ${workout.duration}",
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

            // Display each set entry for this exercise
            val allEntries = groups.flatMap { it.entries }
            allEntries.forEachIndexed { index, entry ->
                val weight = entry.weight.toFloatOrNull() ?: 0f
                val reps = entry.reps.toIntOrNull() ?: 0

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Set ${index + 1}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "${weight.toInt()}kg × $reps reps",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Calculate and display total volume
            val totalVolume = allEntries.sumOf {
                val weight = it.weight.toFloatOrNull() ?: 0f
                val reps = it.reps.toIntOrNull() ?: 0
                (weight * reps).toInt()
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Total volume: $totalVolume kg",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}


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