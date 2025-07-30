package com.example.fitnessapp.views

import android.content.res.Configuration
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditLocationAlt
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.fitnessapp.data.Gym
import com.example.fitnessapp.data.SetGroup
import com.example.fitnessapp.data.WeightUnit
import com.example.fitnessapp.navigation.Screens
import com.example.fitnessapp.ui.theme.FitnessappTheme
import com.example.fitnessapp.viewmodel.CurrentWorkoutViewModel
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@Composable
fun CurrentWorkoutScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: CurrentWorkoutViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.currentWorkout == null) {
        EmptyWorkoutScreen(
            navController = navController,
            onStartWorkout = { viewModel.startNewWorkout() }
        )
        return
    }

    // Handle coming back from ExerciseListSelectionScreen
    LaunchedEffect(navController) {
        navController.currentBackStackEntry?.savedStateHandle?.let { handle ->
            handle.get<Long>("selectedExerciseId")?.let { id ->
                viewModel.addExerciseById(id)
                // Clear the value after consuming it
                handle.remove<Long>("selectedExerciseId")
            }
        }
    }

    CurrentWorkoutScreenContent(
        uiState = uiState,
        navController = navController,
        onExerciseWeightChange = viewModel::updateSetWeight,
        onExerciseRepsChange = viewModel::updateSetReps,
        onAddSetToExercise = viewModel::addSetToExercise,
        onRemoveSetFromExercise = viewModel::removeSetFromExercise,
        onAddExercise = { navController.navigate(Screens.ExerciseListSelectionScreen.route) },
        onRemoveExercise = viewModel::removeExercise,
        onNavigateToStats = { id -> navController.navigate(Screens.ExerciseStatsScreen.createRoute(id)) },
        onCompleteWorkout = viewModel::finishCurrentWorkout,
        onGymSelected = viewModel::selectGym,
        onCreateGym = viewModel::createNewGym,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmptyWorkoutScreen(
    onStartWorkout: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text="Empty Workout",
                        style = MaterialTheme.typography.headlineSmall)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ))
        },
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Nothing to See Here, Officer!")
                ElevatedButton(onClick = onStartWorkout, modifier = Modifier.padding(top = 16.dp)) {
                    Text("Start New Workout")
                }
            }
        }
    }
}

@Composable
fun CurrentWorkoutScreenContent(
    uiState: CurrentWorkoutUIState,
    navController: NavHostController,
    onExerciseWeightChange: (Int, Int, String) -> Unit,
    onExerciseRepsChange: (Int, Int, String) -> Unit,
    onAddSetToExercise: (Int) -> Unit,
    onRemoveSetFromExercise: (Int, Int) -> Unit,
    onAddExercise: () -> Unit,
    onRemoveExercise: (Int) -> Unit,
    onNavigateToStats: (Long) -> Unit,
    onCompleteWorkout: () -> Unit,
    onGymSelected: (Int) -> Unit,
    onCreateGym: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            WorkoutTopAppBar(
                startTime = uiState.currentWorkout?.startTime ?: System.currentTimeMillis(),
                selectedGym = uiState.selectedGym,
                gyms = uiState.gyms,
                onGymSelected = onGymSelected,
                onCreateGym = onCreateGym,
                onCompleteWorkout = onCompleteWorkout
            )
        },
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { paddingValues: PaddingValues ->
        CurrentWorkout(
            exercises = uiState.exerciseUiList,
            setGroups = uiState.setGroups,
            onExerciseWeightChange = onExerciseWeightChange,
            onExerciseRepsChange = onExerciseRepsChange,
            onAddSetToExercise = onAddSetToExercise,
            onRemoveSetFromExercise = onRemoveSetFromExercise,
            onAddExercise = onAddExercise,
            onRemoveExercise = onRemoveExercise,
            onNavigateToStats = onNavigateToStats,
            modifier = modifier.padding(paddingValues)
        )
    }
}

@Composable
fun CurrentWorkout(
    exercises: List<Pair<String, List<Pair<String, String>>>>,
    setGroups: List<SetGroup>,
    onExerciseWeightChange: (exerciseIndex: Int, setIndex: Int, newWeight: String) -> Unit,
    onExerciseRepsChange: (exerciseIndex: Int, setIndex: Int, newReps: String) -> Unit,
    onAddSetToExercise: (exerciseIndex: Int) -> Unit,
    onRemoveSetFromExercise: (exerciseIndex: Int, setIndex: Int) -> Unit,
    onAddExercise: () -> Unit,
    onRemoveExercise: (exerciseIndex: Int) -> Unit,
    onNavigateToStats: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp, horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(exercises) { exerciseIndex, (exerciseName, sets) ->
            val exerciseId = setGroups[exerciseIndex].exerciseId.toLong()
            Exercise(
                index = exerciseIndex,
                name = exerciseName,
                sets = sets,
                onWeightChange = { setIndex, newWeight ->
                    onExerciseWeightChange(exerciseIndex, setIndex, newWeight)
                },
                onRepsChange = { setIndex, newReps ->
                    onExerciseRepsChange(exerciseIndex, setIndex, newReps)
                },
                onAddSet = { onAddSetToExercise(exerciseIndex) },
                onRemoveSet = { setIndex -> onRemoveSetFromExercise(exerciseIndex, setIndex) },
                onNavigateToStats = { onNavigateToStats(exerciseId) }, // Pass ID here
                onRemoveExercise = {onRemoveExercise(exerciseIndex)}
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ElevatedButton(onClick = onAddExercise) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Exercise")
                    Text("Add Exercise", modifier = Modifier.padding(start = 8.dp)) // TODO: make this a resource
                }
            }
        }
    }
}

@Composable
fun Exercise(
    index: Int,
    name: String,
    sets: List<Pair<String, String>>,
    onWeightChange: (Int, String) -> Unit,
    onRepsChange: (Int, String) -> Unit,
    onAddSet: () -> Unit,
    onRemoveSet: (Int) -> Unit,
    onNavigateToStats: () -> Unit,
    onRemoveExercise: (index: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedWeightUnit by remember { mutableStateOf("Kg") }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$name",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(100f)
                )

                UnitSelectorDropdown(
                    selectedUnit = selectedWeightUnit,
                    onUnitSelected = { selectedWeightUnit = it },
                    modifier = Modifier.weight(30f)
                )
                FilledIconButton(
                    onClick = {onNavigateToStats()},
                    modifier = Modifier.weight(22f)
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = "Exercise Stats"
                    )
                }
                FilledIconButton(
                    onClick = {onRemoveExercise(index)},
                    modifier = Modifier.weight(22f),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor   = MaterialTheme.colorScheme.onErrorContainer
                    )
                ){
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove Exercise"
                    )
                }
            }
            sets.forEachIndexed { setIdx, (kg, reps) ->
                SetRow(
                    setIndex = setIdx + 1,
                    weight = kg,
                    reps = reps,
                    weightUnits = selectedWeightUnit,
                    onWeightChange = { newKg -> onWeightChange(setIdx, newKg) },
                    onRepsChange = { newReps -> onRepsChange(setIdx, newReps) }
                )
            }

            Row {
                ElevatedButton(
                    onClick = onAddSet,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Set")
                    Text(
                        text = "Add Set",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                ElevatedButton(
                    onClick = { if (sets.isNotEmpty()) onRemoveSet(sets.size - 1) },
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor   = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Remove Set")
                    Text(
                        text = "Remove Set",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun ElapsedTimeDisplay(startTime: Long, modifier: Modifier = Modifier) {
    var elapsedTime by remember { mutableStateOf(0L) }

    // Update timer every second
    LaunchedEffect(startTime) {
        while (true) {
            elapsedTime = System.currentTimeMillis() - startTime
            delay(1000) // Update every second
        }
    }

    val hours = elapsedTime / (1000 * 60 * 60)
    val minutes = (elapsedTime % (1000 * 60 * 60)) / (1000 * 60)
    val seconds = (elapsedTime % (1000 * 60)) / 1000

    val formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    Text(
        text = formattedTime,
        style = MaterialTheme.typography.titleLarge,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutTopAppBar(
    startTime: Long,
    selectedGym: Gym?,
    gyms: List<Gym>,
    onGymSelected: (Int) -> Unit = {},
    onCreateGym: (String) -> Unit = {},
    onCompleteWorkout: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    var showAddGymDialog by remember { mutableStateOf(false) }
    var newGymName by remember { mutableStateOf("") }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    // Dialog to add a new gym
    if (showAddGymDialog) {
        AlertDialog(
            onDismissRequest = { showAddGymDialog = false },
            title = { Text("Add New Gym") },
            text = {
                OutlinedTextField(
                    value = newGymName,
                    onValueChange = { newGymName = it },
                    label = { Text("Gym Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newGymName.isNotBlank()) {
                            onCreateGym(newGymName)
                            newGymName = ""
                            showAddGymDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGymDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    TopAppBar(
        title = {
            Column {
                Text(
                    text = "Current Workout",
                    style = MaterialTheme.typography.headlineSmall
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = selectedGym?.name ?: "No Gym Selected",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    ElapsedTimeDisplay(
                        startTime = startTime,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = { expanded = true }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EditLocationAlt, contentDescription = "Select Gym")
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                if (gyms.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No gyms available") },
                        onClick = { }
                    )
                } else {
                    gyms.forEach { gym ->
                        DropdownMenuItem(
                            text = { Text(gym.name) },
                            onClick = {
                                onGymSelected(gym.id)
                                expanded = false
                            }
                        )
                    }
                }

                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text("Add New Gym")
                        }
                    },
                    onClick = {
                        showAddGymDialog = true
                        expanded = false
                    }
                )
            }
        },
        actions = {
            IconButton(onClick = onCompleteWorkout) {
                Icon(Icons.Default.Check, contentDescription = "Complete Workout")
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}





@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitSelectorDropdown(
    selectedUnit: String,
    onUnitSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val unitOptions = listOf("Kgs", "Lbs", "Units")

    Box(
        modifier = modifier
    ) {
        Button(
            onClick = { expanded = true },
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(text = selectedUnit)
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Select Unit"
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            unitOptions.forEach { unit ->
                DropdownMenuItem(
                    text = { Text(unit) },
                    onClick = {
                        onUnitSelected(unit)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun SetRow(
    setIndex: Int,
    weight: String,
    weightUnits: String,
    reps: String,
    onWeightChange: (String) -> Unit,
    onRepsChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 1.dp)
            .border(
                width = 2.75.dp,
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Row(
            modifier = modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "$setIndex",
                style = MaterialTheme.typography.bodyLarge,
                modifier = modifier.width(24.dp)
            )

            SetTextField(
                value = weight,
                onValueChange = onWeightChange,
                label = "Weight",
                modifier = modifier.weight(10f),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.FitnessCenter,
                        contentDescription = "weight"
                    )
                },
                trailingElement = {Text(weightUnits)}
            )

            SetTextField(
                value = reps,
                onValueChange = onRepsChange,
                label = "Reps",
                modifier = modifier.weight(10f)
            )
        }
    }
}

@Composable
fun SetTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingElement: @Composable (() -> Unit)? = null

) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = leadingIcon,
        trailingIcon = trailingElement,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}


@Preview
@Composable
fun Preview_CurrentWorkoutScreenContent() {
    FitnessappTheme {
        val sampleSetGroups = listOf(
            SetGroup(
                setGroupId = 1,
                workoutId = 1,
                exerciseId = 101,
                name = "Bench Press",
                weightUnit = WeightUnit.KG,
                exerciseName = "Bench Press",
                entries = listOf(
                    com.example.fitnessapp.data.SetEntry(weight = "50", reps = "8"),
                    com.example.fitnessapp.data.SetEntry(weight = "55", reps = "6")
                )
            )
        )
        val sampleUiState = CurrentWorkoutUIState(
            currentWorkout = com.example.fitnessapp.data.Workout(
                id = 1,
                locationId = 1,
                startTime = 0L,
                endTime = 0L,
                isInProgress = true,
                setGroups = sampleSetGroups
            ),
            setGroups = sampleSetGroups,
            exerciseUiList = sampleSetGroups.map {
                it.exerciseName to it.entries.map { entry -> entry.weight to entry.reps }
            },
            gyms = emptyList(),
            selectedGym = null
        )

        // Use MockNavController instead of casting
        val mockNavController = rememberNavController()

        CurrentWorkoutScreenContent(
            uiState = sampleUiState,
            navController = mockNavController,
            onExerciseWeightChange = { _, _, _ -> },
            onExerciseRepsChange = { _, _, _ -> },
            onAddSetToExercise = { _ -> },
            onRemoveSetFromExercise = { _, _ -> },
            onAddExercise = {},
            onRemoveExercise = { _ -> },
            onNavigateToStats = {},
            onCompleteWorkout = {},
            onGymSelected = {},
            onCreateGym = {}
        )
    }
}

@Preview
@Composable
fun Preview_EmptyWorkoutScreen() {
    FitnessappTheme {
        val mockNavController = rememberNavController()
        EmptyWorkoutScreen(
            navController = mockNavController,
            onStartWorkout = {}
        )
    }
}

@Preview(
    name = "Light Mode Preview",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true
)
@Composable
fun Preview_CurrentWorkoutScreenContent_Light() {
    Preview_CurrentWorkoutScreenContent()
}

@Preview(
    name = "Dark Mode Preview",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
fun Preview_CurrentWorkoutScreenContent_Dark() {
    Preview_CurrentWorkoutScreenContent()
}