package com.example.fitnessapp.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.fitnessapp.data.Exercise
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
        var searchQuery by remember { mutableStateOf("") }
        var showAddDialog by remember { mutableStateOf(false) }
        var newExerciseName by remember { mutableStateOf("") }

        val filteredExercises = if (searchQuery.isBlank()) {
            exercises
        } else {
            exercises.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }

        ExerciseListSelectionContent(
            exercises = filteredExercises,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            showAddDialog = showAddDialog,
            onShowAddDialog = { showAddDialog = true },
            onDismissAddDialog = { showAddDialog = false },
            newExerciseName = newExerciseName,
            onNewExerciseNameChange = { newExerciseName = it },
            onAddExercise = {
                if (newExerciseName.isNotBlank()) {
                    exerciseListViewModel.createExercise(newExerciseName)
                    newExerciseName = ""
                    showAddDialog = false
                }
            },
            onExerciseSelected = { exerciseId ->
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("selectedExerciseId", exerciseId)
                navController.popBackStack()
            },
            onBack = { navController.popBackStack() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseListSelectionContent(
    exercises: List<Exercise>,
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    showAddDialog: Boolean = false,
    onShowAddDialog: () -> Unit = {},
    onDismissAddDialog: () -> Unit = {},
    newExerciseName: String = "",
    onNewExerciseNameChange: (String) -> Unit = {},
    onAddExercise: () -> Unit = {},
    onExerciseSelected: (Long) -> Unit = {},
    onBack: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Exercise") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.primaryContainer,
                    titleContentColor = colorScheme.onPrimaryContainer,
                    navigationIconContentColor = colorScheme.onPrimaryContainer,
                    actionIconContentColor = colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                label = { Text("Search Exercises") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            )
            Button(
                onClick = onShowAddDialog,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Exercise")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add New Exercise")
            }
            LazyColumn(modifier = Modifier.padding(16.dp)) {
                items(exercises.size) { index ->
                    val exercise = exercises[index]
                    Text(
                        text = exercise.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onExerciseSelected(exercise.exerciseId) }
                            .padding(vertical = 12.dp)
                    )
                    HorizontalDivider()
                }
            }
            if (showAddDialog) {
                AlertDialog(
                    onDismissRequest = onDismissAddDialog,
                    title = { Text("Add New Exercise") },
                    text = {
                        OutlinedTextField(
                            value = newExerciseName,
                            onValueChange = onNewExerciseNameChange,
                            label = { Text("Exercise Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = onAddExercise
                        ) {
                            Text("Add")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = onDismissAddDialog) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}


@Composable
fun ExerciseListSelectionScreenPreview(){
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var newExerciseName by remember { mutableStateOf("") }
    val sampleExercises = listOf(
        Exercise(1, "Push Up"),
        Exercise(2, "Squat"),
        Exercise(3, "Pull Up")
    )
    ExerciseListSelectionContent(
    exercises = sampleExercises,
    searchQuery = searchQuery,
    onSearchQueryChange = { searchQuery = it },
    showAddDialog = showAddDialog,
    onShowAddDialog = { showAddDialog = true },
    onDismissAddDialog = { showAddDialog = false },
    newExerciseName = newExerciseName,
    onNewExerciseNameChange = { newExerciseName = it },
    onAddExercise = {
        newExerciseName = ""
        showAddDialog = false
    },
    onExerciseSelected = {},
    onBack = {}
    )
}

@Preview(
    name = "Light Mode Preview",
    showBackground = true
)
@Composable
fun ExerciseListSelectionScreenPreview_Light() {
    FitnessappTheme(darkTheme = false) {
        ExerciseListSelectionScreenPreview()
    }
}

@Preview(
    name = "Dark Mode Preview",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun ExerciseListSelectionScreenPreview_Dark() {
    FitnessappTheme(darkTheme = true) {
        ExerciseListSelectionScreenPreview()
    }
}