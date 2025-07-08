package com.example.fitnessapp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fitnessapp.data.Exercise
import com.example.fitnessapp.ui.theme.FitnessappTheme
import com.example.fitnessapp.viewmodel.ExerciseListSelectionViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun ExerciseListSelectionScreen(
    onExerciseSelected: (Exercise) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExerciseListSelectionViewModel = koinViewModel()
) {
    // Holds the user-typed name for creating a new exercise
    var newExerciseName by remember { mutableStateOf("") }

    // Collect the live list of exercises from the ViewModel
    val exerciseList by viewModel.exercises.collectAsState()

    Scaffold { paddingValues: PaddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            Text(
                text = "Select or Create Exercise",
                style = MaterialTheme.typography.headlineSmall
            )

            // 1) List existing exercises
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(exerciseList) { exercise ->
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onExerciseSelected(exercise) }
                            .padding(vertical = 8.dp)
                    )
                    HorizontalDivider()
                }
            }

            // 2) Input + button to create a new one
            OutlinedTextField(
                value = newExerciseName,
                onValueChange = { newExerciseName = it },
                label = { Text("New exercise name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (newExerciseName.isNotBlank()) {
                        viewModel.createExercise(newExerciseName.trim())
                        newExerciseName = ""
                    }
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Create Exercise")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExerciseListSelectionScreenPreview() {
    FitnessappTheme {
        ExerciseListSelectionScreen(
            onExerciseSelected = {}
        )
    }
}
