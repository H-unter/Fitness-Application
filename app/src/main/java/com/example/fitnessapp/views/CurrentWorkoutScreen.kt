package com.example.fitnessapp.views

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Scale
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.runtime.mutableLongStateOf
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
import com.example.fitnessapp.data.SetEntry
import com.example.fitnessapp.data.SetGroup
import com.example.fitnessapp.data.WeightUnit
import com.example.fitnessapp.data.Workout
import com.example.fitnessapp.navigation.Screens
import com.example.fitnessapp.ui.theme.FitnessappTheme
import com.example.fitnessapp.viewmodel.CurrentWorkoutViewModel
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.fitnessapp.R

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
        onCancelWorkout = viewModel::cancelCurrentWorkout,
        onGymSelected = viewModel::selectGym,
        onCreateGym = viewModel::createNewGym,
        onToggleSetCompletion = viewModel::toggleSetCompletion,
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
                        text = stringResource(R.string.empty_workout_title),
                        style = MaterialTheme.typography.headlineSmall)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ))
        },
        bottomBar = { BottomNavigationBar(navController = navController) },
        containerColor =  MaterialTheme.colorScheme.surfaceContainerLow
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.empty_workout_message))
                ElevatedButton(onClick = onStartWorkout, modifier = Modifier.padding(top = 16.dp)) {
                    Text(stringResource(R.string.start_new_workout))
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
    onCancelWorkout: () -> Unit,
    onGymSelected: (Int) -> Unit,
    onCreateGym: (String) -> Unit,
    onToggleSetCompletion: (Int, Int, Boolean) -> Unit,
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
                onCompleteWorkout = onCompleteWorkout,
                onCancelWorkout = onCancelWorkout,
                validationState = uiState.validationState
            )
        },
        bottomBar = { BottomNavigationBar(navController = navController) },
        containerColor =  MaterialTheme.colorScheme.surfaceContainerLow
    )
    { paddingValues: PaddingValues ->
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
            onToggleSetCompletion = onToggleSetCompletion,
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
    onToggleSetCompletion: (exerciseIndex: Int, setIndex: Int, completed: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp, horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(exercises) { exerciseIndex, (exerciseName, sets) ->
            val exerciseId = setGroups[exerciseIndex].exerciseId.toLong()
            val setGroup = setGroups[exerciseIndex]
            SetGroupCard(
                index = exerciseIndex,
                name = exerciseName,
                sets = sets,
                setGroup = setGroup,
                onWeightChange = { setIndex, newWeight ->
                    onExerciseWeightChange(exerciseIndex, setIndex, newWeight)
                },
                onRepsChange = { setIndex, newReps ->
                    onExerciseRepsChange(exerciseIndex, setIndex, newReps)
                },
                onAddSet = { onAddSetToExercise(exerciseIndex) },
                onRemoveSet = { setIndex -> onRemoveSetFromExercise(exerciseIndex, setIndex) },
                onNavigateToStats = { onNavigateToStats(exerciseId) },
                onRemoveExercise = {onRemoveExercise(exerciseIndex)},
                onToggleCompletion = { setIndex, completed ->
                    onToggleSetCompletion(exerciseIndex, setIndex, completed)
                }
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
                    Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(R.string.add_exercise))
                    Text(stringResource(R.string.add_exercise), modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}

@Composable
fun SetGroupCard(
    index: Int,
    name: String,
    sets: List<Pair<String, String>>,
    setGroup: SetGroup,
    onWeightChange: (Int, String) -> Unit,
    onRepsChange: (Int, String) -> Unit,
    onAddSet: () -> Unit,
    onRemoveSet: (Int) -> Unit,
    onNavigateToStats: () -> Unit,
    onRemoveExercise: (index: Int) -> Unit,
    onToggleCompletion: (Int, Boolean) -> Unit,
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
                    modifier = Modifier
                        .weight(30f)
                        .height(32.dp)
                )
                FilledIconButton(
                    onClick = {onNavigateToStats()},
                    modifier = Modifier
                        .weight(22f)
                        .size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = stringResource(R.string.exercise_stats),
                        modifier = Modifier.size(16.dp)
                    )
                }
                FilledIconButton(
                    onClick = {onRemoveExercise(index)},
                    modifier = Modifier
                        .weight(22f)
                        .size(32.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor   = MaterialTheme.colorScheme.onErrorContainer
                    )
                ){
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.remove_exercise),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Column headings
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.width(24.dp)
                )
                Text(
                    text = stringResource(R.string.weight_with_unit, selectedWeightUnit),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = stringResource(R.string.reps),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = stringResource(R.string.done),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.width(40.dp)
                )
            }

            sets.forEachIndexed { setIdx, (kg, reps) ->
                val isCompleted = setGroup.entries.getOrNull(setIdx)?.completed ?: false
                SetRow(
                    setIndex = setIdx + 1,
                    weight = kg,
                    reps = reps,
                    weightUnits = selectedWeightUnit,
                    completed = isCompleted,
                    onWeightChange = { newKg -> onWeightChange(setIdx, newKg) },
                    onRepsChange = { newReps -> onRepsChange(setIdx, newReps) },
                    onCompletionChange = { completed -> onToggleCompletion(setIdx, completed) }
                )
            }

            Row {
                ElevatedButton(
                    onClick = onAddSet,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(R.string.add_set))
                    Text(
                        text = stringResource(R.string.add_set),
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
                    Icon(imageVector = Icons.Default.Close, contentDescription = stringResource(R.string.remove_set))
                    Text(
                        text = stringResource(R.string.remove_set),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun ElapsedTimeDisplay(startTime: Long, modifier: Modifier = Modifier) {
    var elapsedTime by remember { mutableLongStateOf(0L) }

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
    onCompleteWorkout: () -> Unit = {},
    onCancelWorkout: (() -> Unit)? = null,
    validationState: WorkoutValidationState = WorkoutValidationState.Valid
) {
    var expanded by remember { mutableStateOf(false) }
    var showAddGymDialog by remember { mutableStateOf(false) }
    var showFinishWorkoutDialog by remember { mutableStateOf(false) }
    var showCancelWorkoutDialog by remember { mutableStateOf(false) }
    var newGymName by remember { mutableStateOf("") }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    // Dialog to add a new gym
    if (showAddGymDialog) {
        AlertDialog(
            onDismissRequest = { showAddGymDialog = false },
            title = { Text(stringResource(R.string.add_new_gym)) },
            text = {
                OutlinedTextField(
                    value = newGymName,
                    onValueChange = { newGymName = it },
                    label = { Text(stringResource(R.string.gym_name)) },
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
                    Text(stringResource(R.string.add))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGymDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Finish workout confirmation dialog
    if (showFinishWorkoutDialog) {
        AlertDialog(
            onDismissRequest = { showFinishWorkoutDialog = false },
            title = { Text(stringResource(R.string.finish_workout)) },
            text = { Text(validationState.message) },
            confirmButton = {
                Button(
                    onClick = {
                        if (validationState.canFinish) {
                            onCompleteWorkout()
                            showFinishWorkoutDialog = false
                        }
                    },
                    enabled = validationState.canFinish
                ) {
                    Text(stringResource(R.string.finish))
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinishWorkoutDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showCancelWorkoutDialog) {
        AlertDialog(
            onDismissRequest = { showCancelWorkoutDialog = false },
            title = { Text(stringResource(R.string.cancel_workout)) },
            text = { Text(stringResource(R.string.cancel_workout_warning)) },
            confirmButton = {
                Button(
                    onClick = {
                        onCancelWorkout?.invoke()
                        showCancelWorkoutDialog = false
                    }
                ) {
                    Text(stringResource(R.string.cancel_workout))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelWorkoutDialog = false }) {
                    Text(stringResource(R.string.keep_workout))
                }
            }
        )
    }

    TopAppBar(
        title = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                Text(
                    text = stringResource(R.string.current_workout),
                    style = MaterialTheme.typography.headlineSmall
                )
                ElapsedTimeDisplay(
                    startTime = startTime,
                    modifier = Modifier.padding(top = 2.dp)
                )
                }
                Text(
                    text = selectedGym?.name ?: stringResource(R.string.no_gym_selected),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selectedGym == null) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    }
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = { expanded = true }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EditLocationAlt, contentDescription = stringResource(R.string.select_gym))
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                if (gyms.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.no_gyms_available)) },
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
                            Text(stringResource(R.string.add_new_gym))
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
            IconButton(onClick = { showFinishWorkoutDialog = true }) {
                Icon(Icons.Default.Check, contentDescription = stringResource(R.string.complete_workout))
            }
            IconButton(onClick = { showCancelWorkoutDialog = true }) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cancel_workout))
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
    val unitOptions = listOf(
        stringResource(R.string.unit_kgs),
        stringResource(R.string.unit_lbs),
        stringResource(R.string.unit_units)
    )

    Box(
        modifier = modifier
    ) {
        Button(
            onClick = { expanded = true },
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = selectedUnit,
                style = MaterialTheme.typography.bodySmall
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = stringResource(R.string.select_unit),
                modifier = Modifier.size(12.dp)
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
    completed: Boolean,
    onWeightChange: (String) -> Unit,
    onRepsChange: (String) -> Unit,
    onCompletionChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    // Local state for text fields to maintain cursor position
    var localWeight by remember(weight) { mutableStateOf(weight) }
    var localReps by remember(reps) { mutableStateOf(reps) }

    // Sync local state with external state when it changes
    LaunchedEffect(weight) {
        if (localWeight != weight) {
            localWeight = weight
        }
    }

    LaunchedEffect(reps) {
        if (localReps != reps) {
            localReps = reps
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "$setIndex",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.width(24.dp)
        )

        SetTextField(
            value = localWeight,
            onValueChange = { newValue ->
                localWeight = newValue
                onWeightChange(newValue)
            },
            label = "",
            modifier = Modifier.weight(1f),
            leadingIcon = {
                Icon(
                    modifier = modifier.size(12.dp),
                    imageVector = Icons.Outlined.Scale,
                    contentDescription = stringResource(R.string.weight)
                )
            },
            weightUnit = weightUnits
        )

        SetTextField(
            value = localReps,
            onValueChange = { newValue ->
                localReps = newValue
                onRepsChange(newValue)
            },
            label = "",
            leadingIcon = {
                Icon(
                    modifier = modifier.size(12.dp),
                    imageVector = Icons.Outlined.Repeat,
                    contentDescription = stringResource(R.string.reps)
                )
            },
            modifier = Modifier.weight(1f)
        )

        Checkbox(
            checked = completed,
            onCheckedChange = onCompletionChange,
            modifier = Modifier.width(40.dp)
        )
    }
}

@Composable
fun SetTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingElement: @Composable (() -> Unit)? = null,
    weightUnit: String? = null
) {
    OutlinedTextField(
        value = value,
        modifier = modifier,
        onValueChange = { newValue ->
            // Only allow digits, decimal points (for weight), and empty strings
            if (newValue.isEmpty() || newValue.all { ch ->
                ch.isDigit() || ch == '.'
            }) {
                // Allow only one decimal point
                if (newValue.count { it == '.' } <= 1) {
                    onValueChange(newValue)
                }
            }
        },
        label = if (label.isNotEmpty()) {
            {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else null,
        leadingIcon = leadingIcon,
        trailingIcon = trailingElement,
        suffix = if (weightUnit != null) {
            { Text(text = weightUnit) }
        } else null,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (weightUnit != null) KeyboardType.Decimal else KeyboardType.Number
        ),
        singleLine = true,
        isError = value == "0",
        textStyle = MaterialTheme.typography.bodySmall
    )
}


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
                    SetEntry(weight = "50", reps = "8", completed = true),
                    SetEntry(weight = "55", reps = "6")
                )
            )
        )
        val sampleUiState = CurrentWorkoutUIState(
            currentWorkout = Workout(
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
            selectedGym = null,
            validationState = WorkoutValidationState.NoGymSelected
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
            onCreateGym = {},
            onToggleSetCompletion = { _, _, _ -> },
            onCancelWorkout = {},
            modifier = Modifier
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