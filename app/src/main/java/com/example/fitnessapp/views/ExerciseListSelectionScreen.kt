package com.example.fitnessapp.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.fitnessapp.viewmodel.CurrentWorkoutViewModel
import com.example.fitnessapp.viewmodel.ExerciseListSelectionViewModel
import com.example.fitnessapp.ui.theme.FitnessappTheme
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseListSelectionScreen(
    navController: NavHostController,
    isDarkMode: Boolean,
    viewModel: CurrentWorkoutViewModel = koinViewModel()
) {
    FitnessappTheme(darkTheme = isDarkMode) {
        val exerciseListViewModel: ExerciseListSelectionViewModel = koinViewModel()
        val exercises by exerciseListViewModel.exercises.collectAsState()
        var newExerciseName by remember { mutableStateOf("") }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Select Exercise") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).padding(16.dp)) {
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
    }
}

@Preview
@Composable
fun ExerciseListSelectionScreenPreview() {
    ExerciseListSelectionScreen(navController = rememberNavController(), isDarkMode = false)
}