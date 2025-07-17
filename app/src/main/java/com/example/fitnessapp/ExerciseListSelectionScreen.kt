package com.example.fitnessapp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.fitnessapp.viewmodel.CurrentWorkoutViewModel
import com.example.fitnessapp.viewmodel.ExerciseListSelectionViewModel
import org.koin.androidx.compose.koinViewModel


@Composable
fun ExerciseListSelectionScreen(
    navController: NavHostController,
    viewModel: CurrentWorkoutViewModel = koinViewModel()
) {
    val exerciseListViewModel: ExerciseListSelectionViewModel = koinViewModel()
    val exercises by exerciseListViewModel.exercises.collectAsState()
    var newExerciseName by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = newExerciseName,
            onValueChange = { newExerciseName = it },
            label = { Text("New Exercise Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (newExerciseName.isNotBlank()) {
                    exerciseListViewModel.createExercise(newExerciseName)
                    newExerciseName = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Exercise")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(exercises.size) { index ->
                val exercise = exercises[index]
                Text(
                    text = exercise.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("selectedExerciseId", exercise.exerciseId)
                            navController.popBackStack()
                        }
                        .padding(vertical = 12.dp)
                )
                HorizontalDivider()
            }
        }
    }
}

