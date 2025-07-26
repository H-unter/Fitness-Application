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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.fitnessapp.viewmodel.WorkoutHistoryViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutHistoryScreen(
    navController: NavHostController,
    viewModel: WorkoutHistoryViewModel = koinViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()  // Remove 'by'

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
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.value.workouts) { workout ->  // Add .value here
                WorkoutHistoryItem(
                    workout = workout,
                    onItemClick = { /* Optional: Navigate to workout details */ }
                )
            }
        }
    }
}

@Composable
private fun WorkoutHistoryItem(
    workout: WorkoutHistoryItem,
    onItemClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick),
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
                    text = "${workout.date} ${workout.startTime}",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {

                Text(
                    text = "Duration: ${workout.duration}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Third row: Exercises
            Text(
                text = "Exercises: ${workout.exercises.joinToString(", ")}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
@Preview
private fun WorkoutHistoryItemPreview() {
    val sampleWorkout = WorkoutHistoryItem(
        id = 1L,
        date = "Oct 1, 2023",
        startTime = "10:00 AM",
        duration = "1h 30m",
        gymName = "Fitness Center",
        exercises = listOf("Bench Press", "Squats", "Deadlifts"),
        rawStartTimeMs = System.currentTimeMillis() - 5400000, // 1.5 hours ago
        rawEndTimeMs = System.currentTimeMillis()
    )

    WorkoutHistoryItem(
        workout = sampleWorkout,
        onItemClick = {}
    )
}