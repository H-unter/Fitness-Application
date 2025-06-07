package com.example.fitnessapp
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fitnessapp.meterialcomponents.PacktBottomNavigationBar
import com.example.fitnessapp.ui.theme.FitnessappTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitnessappTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
@Preview(
//    name = "Dark Mode Preview",
//    uiMode = Configuration.UI_MODE_NIGHT_YES,
//    showBackground = true
)
@Composable
fun MainScreenPreview() {
    FitnessappTheme {
        MainScreen()
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    // Mutable state for all exercises (each has a name and list of sets)
    val exerciseList = remember {
        mutableStateListOf(
            "Exercise 1" to mutableStateListOf("0" to "0")
        )
    }

    Scaffold(
        topBar = { WorkoutTopAppBar() },
        bottomBar = { PacktBottomNavigationBar() },
        content = { paddingValues ->
            Workout(
                exercises = exerciseList,
                onExerciseWeightChange = { exerciseIndex, setIndex, newWeight ->
                    val oldSet = exerciseList[exerciseIndex].second[setIndex]
                    exerciseList[exerciseIndex] = exerciseList[exerciseIndex].copy(
                        second = exerciseList[exerciseIndex].second.also {
                            it[setIndex] = oldSet.copy(first = newWeight)
                        }
                    )
                },
                onExerciseRepsChange = { exerciseIndex, setIndex, newReps ->
                    val oldSet = exerciseList[exerciseIndex].second[setIndex]
                    exerciseList[exerciseIndex] = exerciseList[exerciseIndex].copy(
                        second = exerciseList[exerciseIndex].second.also {
                            it[setIndex] = oldSet.copy(second = newReps)
                        }
                    )
                },
                onAddSetToExercise = { exerciseIndex ->
                    exerciseList[exerciseIndex].second.add("0" to "0")
                },
                onRemoveSetFromExercise = { exerciseIndex ->
                    val sets = exerciseList[exerciseIndex].second
                    if (sets.isNotEmpty()) {
                        sets.removeAt(sets.lastIndex)
                    }
                },
                onAddExercise = {
                    exerciseList.add("Exercise ${exerciseList.size + 1}" to mutableStateListOf("0" to "0"))
                },
                onRemoveExercise = {
                    if (exerciseList.isNotEmpty()) {
                    exerciseList.removeAt(exerciseList.lastIndex)
                    }
                },
                modifier = modifier.padding(paddingValues)
            )
        }
    )
}

@Composable
fun Workout(
    exercises: List<Pair<String, List<Pair<String, String>>>>,
    onExerciseWeightChange: (exerciseIndex: Int, setIndex: Int, newWeight: String) -> Unit,
    onExerciseRepsChange: (exerciseIndex: Int, setIndex: Int, newReps: String) -> Unit,
    onAddSetToExercise: (exerciseIndex: Int) -> Unit,
    onRemoveSetFromExercise: (exerciseIndex: Int) -> Unit,
    onAddExercise: () -> Unit,
    onRemoveExercise: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp, horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(exercises) { exerciseIndex, (exerciseName, sets) ->
            Exercise(
                name = exerciseName,
                sets = sets,
                onWeightChange = { setIndex, newWeight ->
                    onExerciseWeightChange(exerciseIndex, setIndex, newWeight)
                },
                onRepsChange = { setIndex, newReps ->
                    onExerciseRepsChange(exerciseIndex, setIndex, newReps)
                },
                onAddSet = { onAddSetToExercise(exerciseIndex) },
                onRemoveSet = { onRemoveSetFromExercise(exerciseIndex) }
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

                ElevatedButton(
                    onClick = onRemoveExercise,
                    enabled = exercises.isNotEmpty()
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Remove Exercise")
                    Text("Remove Exercise", modifier = Modifier.padding(start = 8.dp)) // TODO: make this a resource
                }
            }
        }
    }
}

@Composable
fun Exercise(
    name: String,
    sets: List<Pair<String, String>>,
    onWeightChange: (Int, String) -> Unit,
    onRepsChange: (Int, String) -> Unit,
    onAddSet: () -> Unit,
    onRemoveSet: () -> Unit,
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
                    text = name,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(10f)
                )

                UnitSelectorDropdown(
                    selectedUnit = selectedWeightUnit,
                    onUnitSelected = { selectedWeightUnit = it },
                    modifier = Modifier.weight(4f)
                )
                FilledIconButton(
                    onClick = {
                        // TODO: Navigate to subpage or show info dialog
                    },
                    modifier = Modifier.weight(2f)
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = "Exercise Stats"
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
            titleContentColor = MaterialTheme.colorScheme.primary,
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
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 1.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.secondaryContainer,
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
        singleLine = true
    )
}

