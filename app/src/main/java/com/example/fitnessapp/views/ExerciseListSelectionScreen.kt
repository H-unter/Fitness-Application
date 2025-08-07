package com.example.fitnessapp.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.fitnessapp.R
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

        // Edit dialog state
        var showEditDialog by remember { mutableStateOf(false) }
        var editExerciseName by remember { mutableStateOf("") }
        var editExerciseId by remember { mutableStateOf<Long?>(null) }
        var editErrorResId by remember { mutableStateOf<Int?>(null) }

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
            onBack = { navController.popBackStack() },
            onShowEditDialog = { id, name ->
                editExerciseId = id
                editExerciseName = name
                editErrorResId = null
                showEditDialog = true
            },
            showEditDialog = showEditDialog,
            editExerciseName = editExerciseName,
            onEditExerciseNameChange = { editExerciseName = it },
            onEditExercise = {
                val id = editExerciseId
                if (id != null && editExerciseName.isNotBlank()) {
                    // Check for duplicate name
                    if (exercises.any { it.name.equals(editExerciseName, ignoreCase = true) }) {
                        editErrorResId = R.string.name_exists_error
                    } else {
                        exerciseListViewModel.updateExerciseName(id, editExerciseName)
                        showEditDialog = false
                        editExerciseId = null
                        editExerciseName = ""
                        editErrorResId = null
                    }
                }
            },
            onDismissEditDialog = {
                showEditDialog = false
                editExerciseId = null
                editExerciseName = ""
                editErrorResId = null
            },
            editErrorResId = editErrorResId
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
    onBack: () -> Unit = {},
    // Edit dialog props
    onShowEditDialog: (Long, String) -> Unit = { _, _ -> },
    showEditDialog: Boolean = false,
    editExerciseName: String = "",
    onEditExerciseNameChange: (String) -> Unit = {},
    onEditExercise: () -> Unit = {},
    onDismissEditDialog: () -> Unit = {},
    editErrorResId: Int? = null
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.select_exercise)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
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
                label = { Text(stringResource(R.string.search_exercises)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search))
                }
            )
            Button(
                onClick = onShowAddDialog,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_exercise))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.add_new_exercise))
            }
            LazyColumn(modifier = Modifier.padding(16.dp)) {
                items(exercises.size) { index ->
                    val exercise = exercises[index]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = exercise.name,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onExerciseSelected(exercise.exerciseId) }
                        )
                        IconButton(
                            onClick = { onShowEditDialog(exercise.exerciseId, exercise.name) }
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit))
                        }
                    }
                    HorizontalDivider()
                }
            }
            if (showAddDialog) {
                AlertDialog(
                    onDismissRequest = onDismissAddDialog,
                    title = { Text(stringResource(R.string.add_new_exercise)) },
                    text = {
                        OutlinedTextField(
                            value = newExerciseName,
                            onValueChange = onNewExerciseNameChange,
                            label = { Text(stringResource(R.string.exercise_name)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = onAddExercise
                        ) {
                            Text(stringResource(R.string.add))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = onDismissAddDialog) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }
            if (showEditDialog) {
                AlertDialog(
                    onDismissRequest = onDismissEditDialog,
                    title = { Text(stringResource(R.string.edit_exercise)) },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = editExerciseName,
                                onValueChange = onEditExerciseNameChange,
                                label = { Text(stringResource(R.string.exercise_name)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = stringResource(R.string.edit_exercise_warning),
                                color = colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            if (editErrorResId != null) {
                                Text(
                                    text = stringResource(editErrorResId),
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = onEditExercise
                        ) {
                            Text(stringResource(R.string.edit))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = onDismissEditDialog) {
                            Text(stringResource(R.string.cancel))
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