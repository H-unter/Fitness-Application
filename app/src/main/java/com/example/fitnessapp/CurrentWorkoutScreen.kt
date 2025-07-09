package com.example.fitnessapp

import android.content.res.Configuration
import android.util.Log
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.DrawModifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fitnessapp.ui.theme.FitnessappTheme
import com.example.fitnessapp.viewmodel.CurrentWorkoutViewModel
import kotlinx.coroutines.flow.map
import org.koin.androidx.compose.koinViewModel


@Composable
fun CurrentWorkoutScreen(
    onNavigateToStats: () -> Unit = {},
    onAddExercise:    () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val viewModel: CurrentWorkoutViewModel = koinViewModel()
    val currentWorkout by viewModel.currentWorkout.collectAsState(initial = null)
    val setGroups by viewModel.setGroups.collectAsState(initial = emptyList())

    // if there is no active workout, prompt user to start one
    if (currentWorkout == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            ElevatedButton(onClick = { viewModel.startNewWorkout() }) {
                Text(text = "Start Workout")
            }
        }
        return
    }

    // map domain SetGroup to UI exercise list
    val exercises = setGroups.map { group ->
        group.name to group.sets.map { it.weight to it.reps }
    }

    CurrentWorkoutScreenContent(
        exercises = exercises,
        onExerciseWeightChange  = viewModel::updateSetWeight,
        onExerciseRepsChange    = viewModel::updateSetReps,
        onAddSetToExercise      = viewModel::addSetToExercise,
        onRemoveSetFromExercise = { index ->
            exercises.getOrNull(index)?.let { (_, sets) ->
                if (sets.isNotEmpty()) {
                    viewModel.removeSetFromExercise(index, sets.lastIndex)
                }
            }
        },
        onAddExercise           = onAddExercise,
        onRemoveExercise        = { exerciseIndex ->
            viewModel.removeExercise(exerciseIndex)
        },
        onNavigateToStats       = onNavigateToStats,
        modifier                = modifier
    )
}

@Composable
fun CurrentWorkoutScreenContent(
    exercises: List<Pair<String, List<Pair<String, String>>>>,
    onExerciseWeightChange:     (exerciseIndex: Int, setIndex: Int, newWeight: String) -> Unit,
    onExerciseRepsChange:       (exerciseIndex: Int, setIndex: Int, newReps: String) -> Unit,
    onAddSetToExercise:         (exerciseIndex: Int) -> Unit,
    onRemoveSetFromExercise:    (exerciseIndex: Int) -> Unit,
    onAddExercise:              () -> Unit,
    onRemoveExercise:           (exerciseIndex: Int) -> Unit,
    onNavigateToStats:          () -> Unit,
    modifier:                   Modifier = Modifier
) {
    Scaffold(
        topBar    = { WorkoutTopAppBar() },
        bottomBar = { BottomNavigationBar() }
    ) { paddingValues: PaddingValues ->
        CurrentWorkout(
            exercises               = exercises,
            onExerciseWeightChange  = onExerciseWeightChange,
            onExerciseRepsChange    = onExerciseRepsChange,
            onAddSetToExercise      = onAddSetToExercise,
            onRemoveSetFromExercise = onRemoveSetFromExercise,
            onAddExercise           = onAddExercise,
            onRemoveExercise        = onRemoveExercise,
            onNavigateToStats       = onNavigateToStats,
            modifier                = modifier.padding(paddingValues)
        )
    }
}

@Composable
fun CurrentWorkout(
    exercises: List<Pair<String, List<Pair<String, String>>>>,
    onExerciseWeightChange: (exerciseIndex: Int, setIndex: Int, newWeight: String) -> Unit,
    onExerciseRepsChange: (exerciseIndex: Int, setIndex: Int, newReps: String) -> Unit,
    onAddSetToExercise: (exerciseIndex: Int) -> Unit,
    onRemoveSetFromExercise: (exerciseIndex: Int) -> Unit,
    onAddExercise: () -> Unit,
    onRemoveExercise: (exerciseIndex: Int) -> Unit,
    onNavigateToStats: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp, horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(exercises) { exerciseIndex, (exerciseName, sets) ->
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
                onRemoveSet = { onRemoveSetFromExercise(exerciseIndex)},
                onNavigateToStats = {onNavigateToStats()},
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
    onRemoveSet: () -> Unit,
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
            sets.forEachIndexed { index, (kg, reps) ->
                SetRow(
                    setIndex = index + 1,
                    weight = kg,
                    reps = reps,
                    weightUnits = selectedWeightUnit,
                    onWeightChange = { newKg -> onWeightChange(index, newKg) },
                    onRepsChange = { newReps -> onRepsChange(index, newReps) }
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
                        text = "Add Set", // TODO: make this into a resource
                        modifier = Modifier.padding(start = 8.dp)

                    )
                }
                ElevatedButton(
                    onClick = onRemoveSet,
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor   = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Add Set")
                    Text(
                        text = "Remove Set", // TODO: make this into a resource
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutTopAppBar() {
    var expanded by remember { mutableStateOf(false) }
    var selectedGym by remember { mutableStateOf("Gym 1") }
    val gymOptions = listOf("Gym 1", "Gym 2", "Gym 3", "Gym 4")

    MediumTopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        title = {Text(
            text = "Monday Workout",
            style = MaterialTheme.typography.headlineMedium
        )},
        actions = {
            Box(
                modifier = Modifier
                    .padding(end = 12.dp)
            ) {
                Button(
                    onClick = { expanded = true },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = selectedGym)
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select Gym"
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    gymOptions.forEach { gym ->
                        DropdownMenuItem(
                            text = { Text(gym) },
                            onClick = {
                                selectedGym = gym
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
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


@Composable
fun Preview_CurrentWorkoutScreenContent() {
    FitnessappTheme {
        val sampleExercises = listOf(
            "Bench Press" to listOf("50" to "8", "55" to "6"),
            "Squat" to listOf("80" to "8", "85" to "6")
        )
        CurrentWorkoutScreenContent(
            exercises               = sampleExercises,
            onExerciseWeightChange  = { _, _, _ -> },
            onExerciseRepsChange    = { _, _, _ -> },
            onAddSetToExercise      = { _ -> },
            onRemoveSetFromExercise = { _ -> },
            onAddExercise           = {},
            onRemoveExercise        = { _ -> },
            onNavigateToStats       = {}
        )
    }
}

// 2) Previews must also be zero-arg and call the above wrapper
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