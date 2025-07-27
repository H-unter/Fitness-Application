package com.example.fitnessapp.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items  // Add this import
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.fitnessapp.data.SetEntry
import com.example.fitnessapp.data.SetGroup
import com.example.fitnessapp.data.WeightUnit
import com.example.fitnessapp.data.WorkoutDao
import com.example.fitnessapp.ui.theme.FitnessappTheme
import com.example.fitnessapp.viewmodel.WorkoutHistoryViewModel
import kotlinx.coroutines.flow.StateFlow
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutHistoryScreen(
    navController: NavHostController,
    viewModel: WorkoutHistoryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    WorkoutHistoryScreenContent(
        uiState = uiState,
        navController = navController
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutHistoryScreenContent(
    uiState: WorkoutHistoryUIState,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workout History") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { paddingValues ->
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
@Preview(showBackground = true)
private fun WorkoutHistoryScreenPreview() {
    // Create sample data
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
        ),
        SetGroup(
            setGroupId = 2,
            workoutId = 1,
            exerciseId = 2,
            name = "Squats",
            weightUnit = WeightUnit.KG,
            exerciseName = "Squats",
            entries = listOf(SetEntry(weight = "100", reps = "8"))
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

    val sampleState = WorkoutHistoryUIState(workouts = listOf(sampleWorkout))

    FitnessappTheme {
        WorkoutHistoryScreenContent(
            uiState = sampleState,
            navController = rememberNavController()
        )
    }
}